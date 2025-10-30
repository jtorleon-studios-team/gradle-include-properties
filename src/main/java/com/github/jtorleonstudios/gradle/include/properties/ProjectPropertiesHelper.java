package com.github.jtorleonstudios.gradle.include.properties;

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

public interface ProjectPropertiesHelper {
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

  static boolean isPropertiesFile(@NotNull File file) {
    return file.isFile() && file.getName().endsWith(".properties");
  }

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
      Main.LOGGER.error(String.format(
          "Failed to load properties file: \"%s\"",
          file.getAbsolutePath()
      ), err);
    }

    return properties;
  }

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

