package com.github.tmslpm.gradle.include.properties;

import org.gradle.api.provider.ListProperty;

import java.util.List;

public abstract class ExpandPropertiesExtension {

  public final static String NAME = "expandProperties";

  public ExpandPropertiesExtension() {
    this.getTo().convention(List.of());
  }

  /**
   * <p>
   *   Expand all properties to specified resources files
   * </p>
   * @return a {@link ListProperty} of {@link String} objects
   * representing the path of resources files
   */
  public abstract ListProperty<String> getTo();
}