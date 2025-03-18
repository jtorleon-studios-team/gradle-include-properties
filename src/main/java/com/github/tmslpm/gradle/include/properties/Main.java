package com.github.tmslpm.gradle.include.properties;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.AbstractCopyTask;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * The main Gradle plugin for including and managing properties in a Gradle project.
 * This plugin loads properties files, merges them with the project's properties,
 * and checks if all the expected keys are present.
 *
 * <p>It also provides the option to configure resources to expand according
 * to the project's properties.</p>
 */
public class Main implements Plugin<Object> {

  /**
   * The unique identifier for the plugin.
   */
  public static final String PLUGIN_IDENTIFIER = "io.github.jtorleonstudiosteam.gradleincludeproperties";

  /**
   * The logger used for logging information and errors.
   */
  public static final Logger LOGGER = Logging.getLogger(Main.class);

  /**
   * Applies the plugin to the target object. Checks if the target object is a Gradle project.
   *
   * @param target The object to which the plugin is applied.
   */
  @Override
  public void apply(@NotNull Object target) {
    if (target instanceof Project project) {
      this.applyFrom(project);
    } else {
      LOGGER.error(
        "Failed to apply plugin '{}': Unsupported target type '{}'. " +
        "This plugin can only be applied to a Gradle Project (build.gradle).",
        PLUGIN_IDENTIFIER, target.getClass().getName()
      );
    }
  }

  /**
   * Applies the plugin to a Gradle project.
   *
   * @param project The Gradle project to which the plugin is applied.
   */
  private void applyFrom(@NotNull Project project) {
    this.beforeEvaluateFrom(project);

    project.getExtensions()
      .create(ExpectedPropertiesExtension.NAME, ExpectedPropertiesExtension.class);

    project.afterEvaluate(this::applyAfterEvaluateFrom);
  }

  /**
   * Executes actions before evaluating the Gradle project.
   *
   * <p>
   * This includes loading properties from files specified in the
   * `include.properties` property of the project.
   * </p>
   *
   * @param project The Gradle project.
   */
  private void beforeEvaluateFrom(@NotNull Project project) {
    if (!project.hasProperty("include.properties")) {
      return;
    }

    if (project.property("include.properties") instanceof String includeProperties) {
      List<String> paths = Arrays.stream(includeProperties.split(","))
        .map(String::trim).toList();

      ProjectPropertiesHelper.recursiveLoadProperties(
          project,
          project.hasProperty("include.properties.override") &&
          "true".equals(project.property("include.properties.override")),
          paths.stream().map(project::file).toList()
      );
    }
  }

  /**
   * Applies actions after evaluating the Gradle project. This includes checking expected keys
   * and configuring the `processResources` task for the properties to expand.
   *
   * @param project The Gradle project.
   */
  private void applyAfterEvaluateFrom(@NotNull Project project) {
    var ext = project.getExtensions()
      .getByType(ExpectedPropertiesExtension.class);

    if (!ext.getExpected().get().isEmpty()) {
      ProjectPropertiesHelper.checkExpectedKey(project, ext.getExpected().get());
    }

    if (!ext.getExpandToResources().get().isEmpty()) {
      this.configureProcessResources(project, ext.getExpandToResources().get());
    }
  }

  /**
   * Configures the `processResources` task to expand the project's properties into the specified resource files.
   *
   * @param project  The Gradle project.
   * @param expandTo The list of resource files to expand.
   */
  private void configureProcessResources(@NotNull Project project, List<String> expandTo) {
    project.getTasks()
      .named("processResources", AbstractCopyTask.class)
      .configure(v -> v
        .filesMatching(expandTo, act -> act.expand(project.getProperties()))
      );
  }

}
