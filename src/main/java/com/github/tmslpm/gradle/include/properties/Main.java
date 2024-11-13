package com.github.tmslpm.gradle.include.properties;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class Main implements Plugin<Object> {
  public static final String PLUGIN_IDENTIFIER = "gradle-include-properties";
  public static final Logger LOGGER = Logging.getLogger(Main.class);

  @Override
  public void apply(@NotNull Object target) {
    if (target instanceof Project project) {
      this.applyFrom(project);
    } else {
      LOGGER.error(String.format(
          "Failed to apply plugin '%s': Unsupported target type '%s'. "
          + "This plugin can only be applied to a Gradle Project (build.gradle).",
          PLUGIN_IDENTIFIER,
          target.getClass().getName()
      ));
    }
  }

  private void applyFrom(@NotNull Project project) {
    this.beforeEvaluateFrom(project);
    project.getExtensions().create(ExpectedPropertiesExtension.NAME, ExpectedPropertiesExtension.class);
    project.afterEvaluate(this::applyAfterEvaluateFrom);
  }

  private void beforeEvaluateFrom(@NotNull Project project) {
    if (!project.hasProperty("include.properties")) {
      return;
    }

    if (project.property("include.properties") instanceof String includeProperties) {
      List<String> paths = Arrays.stream(includeProperties.split(",")).map(String::trim).toList();
      ProjectPropertiesHelper.recursiveLoadProperties(
          project,
          project.hasProperty("include.properties.override") &&
          "true".equals(project.property("include.properties.override")),
          paths.stream().map(project::file).toList()
      );
    }
  }

  private void applyAfterEvaluateFrom(@NotNull Project project) {
    var ext = project.getExtensions().getByType(ExpectedPropertiesExtension.class);
    if (!ext.getExpected().get().isEmpty()) {
      ProjectPropertiesHelper.checkExpectedKey(project, ext.getExpected().get());
    }
  }

}
