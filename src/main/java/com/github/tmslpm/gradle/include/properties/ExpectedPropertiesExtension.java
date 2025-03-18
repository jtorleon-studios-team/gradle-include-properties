package com.github.tmslpm.gradle.include.properties;

import org.gradle.api.provider.ListProperty;

import java.util.List;

/**
 * <p>
 * This class represents a Gradle extension for handling expected properties
 * in your project configuration. It allows the specification of property keys
 * that are expected after including external property files.
 * </p>
 *
 * <p>
 * It provides functionality for both:
 * </p>
 *
 * <ul>
 *  <li>Managing the list of expected property keys</li>
 *  <li>Expanding properties into specified resource files</li>
 * </ul>
 */
public abstract class ExpectedPropertiesExtension {

  /**
   * <p>
   * The name of this extension when it is added to a Gradle project.
   * This name is used to access the extension from the Gradle project.
   * </p>
   */
  public final static String NAME = "expectedProperties";

  /**
   * <p>
   * Default constructor for ExpectedPropertiesExtension. Initializes the expected
   * properties list and the list of resource files to be expanded.
   * </p>
   * <ul>
   *     <li>
   *       {@code expected}: defaults to an empty list, indicating no specific keys
   *       are expected by default.
   *     </li>
   *     <li>
   *       {@code expandToResources}: defaults to an empty list, meaning no properties
   *       will be expanded to resources by default.
   *     </li>
   * </ul>
   * <p>
   * This constructor sets the initial values using Gradle's convention mechanism.
   * </p>
   */
  public ExpectedPropertiesExtension() {
    this.getExpected().convention(List.of());
    this.getExpandToResources().convention(List.of());
  }

  /**
   * <p>
   * Returns the list of expected property keys that are included after the external
   * property files are processed. These keys represent the properties that are expected
   * to be available after the inclusion process.
   * </p>
   *
   * @return a {@link ListProperty} of {@link String} objects representing the expected keys.
   * This list will be empty by default if not specified.
   */
  public abstract ListProperty<String> getExpected();

  /**
   * <p>
   * Returns the list of resource file paths where properties should be expanded.
   * These files will receive the expanded property values.
   * </p>
   * @return a {@link ListProperty} of {@link String} objects representing the paths of resource files.
   * These files will be populated with the expanded property values as needed.
   */
  public abstract ListProperty<String> getExpandToResources();

}
