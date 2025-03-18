package com.github.tmslpm.gradle.include.properties;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * <p>
 * This interface provides utility methods to help with loading, merging, and checking
 * project properties in a Gradle project. It supports recursively loading properties
 * files, merging the properties into the project, and verifying the existence of expected properties.
 * </p>
 * <p>
 * It includes methods for:
 * </p>
 * <ul>
 *     <li>Recursively loading .properties files from specified directories</li>
 *     <li>Checking if a file is a valid .properties file</li>
 *     <li>Loading properties from a file</li>
 *     <li>Merging the loaded properties into a Gradle project, with the option to override existing properties</li>
 *     <li>Checking whether all expected property keys exist in the project</li>
 * </ul>
 */
public interface ProjectPropertiesHelper {

  /**
   * <p>
   * Recursively loads properties files from the specified list of files and directories.
   * If a directory is encountered, it will be explored further to find additional properties files.
   * The properties are then merged into the Gradle project.
   * </p>
   *
   * @param project the Gradle project to load properties into.
   * @param canOverride specifies whether existing project properties can be overridden.
   * @param files a list of files and directories to explore for properties files.
   */
  static void recursiveLoadProperties(@NotNull Project project, boolean canOverride, @NotNull List<File> files) {
    if (files.isEmpty()) {
      return; // do nothing;
    }

    List<File> dirToExploreAfter = new ArrayList<>();

    for (File file : files) {
      if (file.isDirectory()) {
        dirToExploreAfter.add(file);
      } else if (isPropertiesFile(file)) {
        Properties properties = tryLoadPropertiesFile(file);
        mergePropertiesTo(project, properties, canOverride);
      }
    }

    for (File dir : dirToExploreAfter) {
      var fileList = dir.listFiles();
      if (fileList != null) {
        recursiveLoadProperties(
            project,
            canOverride,
            List.of(fileList)
        );
      }
    }
  }

  /**
   * <p>
   * Checks if the given file is a valid .properties file based on its name and extension.
   * </p>
   *
   * @param file the file to check.
   * @return {@code true} if the file is a valid .properties file, {@code false} otherwise.
   */
  static boolean isPropertiesFile(@NotNull File file) {
    return file.isFile() &&
           file.getName().endsWith(".properties");
  }

  /**
   * <p>
   * Attempts to load a .properties file from the given path. If the file is valid and
   * can be loaded, the properties will be returned. Otherwise, an empty properties object is returned.
   * </p>
   *
   * @param file the file to load properties from.
   * @return a {@link Properties} object containing the loaded properties.
   */
  static @NotNull Properties tryLoadPropertiesFile(@NotNull File file) {
    Properties properties = new Properties();

    if (!isPropertiesFile(file)) {
      Main.LOGGER.error(
        "Failed to load properties file: \"{}\"",
        file.getAbsolutePath()
      );

      return properties;
    }

    try (FileInputStream entry = new FileInputStream(file)) {
      properties.load(entry);
      if (properties.isEmpty()) {
        Main.LOGGER.warn(
          "Loaded .properties file is empty:  \"{}\"",
          file.getAbsolutePath()
        );
      }
    } catch (IOException err) {
      Main.LOGGER.error(
        "Failed to load properties file: \"{}\"",
        file.getAbsolutePath(),
        err
      );
    }

    return properties;
  }

  /**
   * <p>
   * Merges the provided properties into the Gradle project. If a property already exists,
   * it will be overwritten only if {@code canOverride} is {@code true}. Otherwise, it will not be overridden.
   * New properties will be added to the project.
   * </p>
   *
   * @param project the Gradle project to merge properties into.
   * @param properties the properties to merge into the project.
   * @param canOverride specifies whether existing properties can be overridden.
   */
  static void mergePropertiesTo(@NotNull Project project, @NotNull Properties properties, boolean canOverride) {
    Main.LOGGER.lifecycle("Include properties");

    var ext = project.getExtensions().getExtraProperties();
    properties.forEach((key, value) -> {
      if (project.hasProperty((String) key)) {
        if (canOverride) {
          project.setProperty((String) key, value);
          Main.LOGGER.warn(" \u001B[33m!\u001B[0m Overriding property: " + key);
        } else {
          Main.LOGGER.warn(" \u001B[31m!\u001B[0m Property already exists and will not be overridden: " + key);
        }
      } else {
        ext.set((String) key, value);
        Main.LOGGER.lifecycle(" \u001B[32m+\u001B[0m Adding property: " + key);
      }
    });
  }

  /**
   * <p>
   * Checks whether all expected property keys exist in the Gradle project.
   * If any keys are missing, an exception will be thrown, and a list of the missing keys will be logged.
   * </p>
   *
   * @param project the Gradle project to check.
   * @param expectedKeyList the list of expected property keys.
   * @throws GradleException if any expected property keys are missing.
   */
  static void checkExpectedKey(@NotNull Project project, @NotNull List<String> expectedKeyList) throws GradleException {
    List<String> missingKeys = new ArrayList<>();

    for (String key : expectedKeyList) {
      if (!project.hasProperty(key)) {
        Main.LOGGER.error("missing key {}", key);
        missingKeys.add(key);
      }
    }

    if (!missingKeys.isEmpty()) {
      throw new GradleException(String.format(
          "Missing %d key properties %s",
          missingKeys.size(),
          Arrays.toString(missingKeys.toArray())
      ));
    }
  }

}
