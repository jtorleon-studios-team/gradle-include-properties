package com.github.tmslpm.gradle.include.properties;

import org.gradle.api.provider.ListProperty;

import java.util.List;

public abstract class ExpectedPropertiesExtension {
  /**
   * <p>
   * The name of this extension when it is added to a Gradle project.
   * </p>
   */
  public final static String NAME = "expectedProperties";

  /**
   * <p>
   * Default constructor for ExpectedPropertiesExtension.
   * </p>
   * <ul>
   *     <li>
   *       {@code expected}: defaults to an empty list,
   *       indicating no specific keys are expected by default.
   *     </li>
   * </ul>
   */
  public ExpectedPropertiesExtension() {
    this.getExpected().convention(List.of());
  }

  /**
   * <p>
   * The list of keys expected after include
   * </p>
   *
   * @return a {@link ListProperty} of {@link String} objects
   * representing the expected keys.
   */
  public abstract ListProperty<String> getExpected();


}