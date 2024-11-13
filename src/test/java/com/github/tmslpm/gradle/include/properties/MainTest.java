package com.github.tmslpm.gradle.include.properties;

import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainTest {
  @TempDir
  public File rootProjectDir;


  @BeforeEach
  public void setup() {
  }

  @Test
  public void testSetup() {
    assertEquals(TaskOutcome.SUCCESS, Optional.ofNullable(GradleRunner.create()
            .withProjectDir(rootProjectDir)
            .withArguments("help")
            .withPluginClasspath()
            .build().task(":help"))
        .map(BuildTask::getOutcome)
        .orElse(TaskOutcome.FAILED));
  }

  @Test
  public void testPluginIsAppliedAndWork() throws IOException {
    // Create a temporary build file
    Path buildFile = rootProjectDir.toPath().resolve("build.gradle");
    Files.write(buildFile, String.format("""
            plugins {
              id '%s'
            }
        """, Main.PLUGIN_IDENTIFIER).getBytes());

    assertEquals(TaskOutcome.SUCCESS, Optional.ofNullable(GradleRunner.create()
            .withProjectDir(rootProjectDir)
            .withArguments("help")
            .withPluginClasspath()
            .build().task(":help"))
        .map(BuildTask::getOutcome)
        .orElse(TaskOutcome.FAILED));
  }

}