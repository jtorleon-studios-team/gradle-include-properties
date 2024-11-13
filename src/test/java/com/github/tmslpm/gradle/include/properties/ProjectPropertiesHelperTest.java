package com.github.tmslpm.gradle.include.properties;

import org.gradle.testkit.runner.BuildResult;
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
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ProjectPropertiesHelperTest {
  @TempDir
  File rootProjectDir;
  Path includePropertiesFilePath;

  @BeforeEach
  void setup() throws IOException {
    var gradlePropertiesFileName = "gradle.properties";
    Path gradlePropertiesFilePath = rootProjectDir.toPath().resolve(gradlePropertiesFileName);
    Files.write(gradlePropertiesFilePath, """
        key1=value1
        key2=value2
        include.properties=file-include.properties
         """.getBytes());

    File buildFile = new File(rootProjectDir, "build.gradle");
    Files.writeString(buildFile.toPath(), String.format("""
        plugins {
            id '%s'
        }
         
        task printProperties {
            doLast {
                println "key1=${project.findProperty('key1')}"
                println "key2=${project.findProperty('key2')}"
            }
        }
        """, Main.PLUGIN_IDENTIFIER));

    var includePropertiesFileName = "file-include.properties";
    includePropertiesFilePath = rootProjectDir.toPath().resolve(includePropertiesFileName);
    Files.write(includePropertiesFilePath, """
        key1=new_value1
        key2=new_value2
        key3=new_value3
         """.getBytes());
  }

  @Test
  void testSetup() throws IOException {
    var gradlePropertiesFile = new File(rootProjectDir, "gradle.properties");

    Properties gradleProperties = new Properties();
    gradleProperties.load(Files.newInputStream(gradlePropertiesFile.toPath()));
    assertEquals("value1", gradleProperties.getProperty("key1"));
    assertEquals("value2", gradleProperties.getProperty("key2"));
    assertEquals("file-include.properties", gradleProperties.getProperty("include.properties"));
    assertEquals(3, gradleProperties.size(), "Should be two properties initially");

    BuildResult result = GradleRunner
        .create()
        .withProjectDir(rootProjectDir)
        .withArguments("printProperties")
        .withPluginClasspath()
        .build();

    assertEquals(TaskOutcome.SUCCESS, Optional.ofNullable(
        result.task(":" + "printProperties")
    ).map(BuildTask::getOutcome).orElse(TaskOutcome.FAILED));

    assertTrue(result.getOutput().contains("key2=value2"));
    assertTrue(result.getOutput().contains("key1=value1"));
  }

  @Test
  void mergePropertiesTo_overrideFalse() throws IOException {
    File buildFile = new File(rootProjectDir, "build.gradle");
    Files.writeString(buildFile.toPath(), String.format("""
        plugins {
            id '%s'
        }
                           
        expectedProperties {
          expected = []
        }
             
        task printProperties {
            doLast {
                println "key1=${project.findProperty('key1')}"
                println "key2=${project.findProperty('key2')}"
                println "key3=${project.findProperty('key3')}"
            }
        }
        """, Main.PLUGIN_IDENTIFIER));

    BuildResult result = GradleRunner
        .create()
        .withProjectDir(rootProjectDir)
        .withArguments("printProperties")
        .withPluginClasspath()
        .build();

    assertTrue(result.getOutput().contains("Adding property: key3"));
    assertTrue(result.getOutput().contains("key1=value1"));
    assertTrue(result.getOutput().contains("key2=value2"));
    assertTrue(result.getOutput().contains("key3=new_value3"));
  }

  @Test
  void mergePropertiesTo_overrideTrue() throws IOException {
    var gradlePropertiesFileName = "gradle.properties";
    Path gradlePropertiesFilePath = rootProjectDir.toPath().resolve(gradlePropertiesFileName);
    Files.write(gradlePropertiesFilePath, """
        key1=value1
        key2=value2
        include.properties=file-include.properties
        include.properties.override=true
         """.getBytes());

    File buildFile = new File(rootProjectDir, "build.gradle");
    Files.writeString(buildFile.toPath(), String.format("""
        plugins {
            id '%s'
        }
                  
        expectedProperties {
          expected = []
        }
                  
        task printProperties {
            doLast {
                println "key1=${project.findProperty('key1')}"
                println "key2=${project.findProperty('key2')}"
                println "key3=${project.findProperty('key3')}"
            }
        }
        """, Main.PLUGIN_IDENTIFIER));

    BuildResult result = GradleRunner
        .create()
        .withProjectDir(rootProjectDir)
        .withArguments("printProperties")
        .withPluginClasspath()
        .build();

    assertTrue(result.getOutput().contains("Adding property: key3"));
    assertTrue(result.getOutput().contains("key1=new_value1"));
    assertTrue(result.getOutput().contains("key2=new_value2"));
    assertTrue(result.getOutput().contains("key3=new_value3"));
  }

  @Test
  void mergePropertiesTo_expectedKey() throws IOException {
    File buildFile = new File(rootProjectDir, "build.gradle");
    Files.writeString(buildFile.toPath(), String.format("""
        plugins {
            id '%s'
        }
        expectedProperties {
          expected = ["alpha"]
        }""", Main.PLUGIN_IDENTIFIER));

    assertThrows(RuntimeException.class, () -> GradleRunner
        .create()
        .withProjectDir(rootProjectDir)
        .withArguments("help")
        .withPluginClasspath()
        .build());
  }

  @Test
  void mergePropertiesTo_hasExpectedKey () throws IOException {
    File buildFile = new File(rootProjectDir, "build.gradle");
    Files.writeString(buildFile.toPath(), String.format("""
        plugins {
            id '%s'
        }
        expectedProperties {
          expected = ["key1", "key2", "key3"]
        }""", Main.PLUGIN_IDENTIFIER));
    assertDoesNotThrow(() -> GradleRunner
        .create()
        .withProjectDir(rootProjectDir)
        .withArguments("help")
        .withPluginClasspath()
        .build());
  }

  @Test
  void isPropertiesFile_validPropertiesFile() throws IOException {
    // empty
    var v = rootProjectDir.toPath().resolve("tmp.properties");
    Files.write(v, "".getBytes());
    assertTrue(ProjectPropertiesHelper.isPropertiesFile(v.toFile()));
    // with value
    Files.write(v, "k=v".getBytes());
    assertTrue(ProjectPropertiesHelper.isPropertiesFile(v.toFile()));

    // txt
    v = rootProjectDir.toPath().resolve("test.txt");
    Files.write(v, "k=v".getBytes());
    assertFalse(ProjectPropertiesHelper.isPropertiesFile(v.toFile()));

    // non existent
    var vv = new File(rootProjectDir, "nonexistent.properties");
    assertFalse(ProjectPropertiesHelper.isPropertiesFile(vv));
  }

  @Test
  void tryLoadPropertiesFile_validFile() throws IOException {
    Properties properties = ProjectPropertiesHelper.tryLoadPropertiesFile(
        includePropertiesFilePath.toFile()
    );

    assertEquals(3, properties.size());
    assertEquals("new_value1", properties.getProperty("key1"));
    assertEquals("new_value2", properties.getProperty("key2"));

    // empty
    var v = rootProjectDir.toPath().resolve("empty.properties");
    Files.write(v, "".getBytes());
    assertTrue(ProjectPropertiesHelper.tryLoadPropertiesFile(v.toFile()).isEmpty());

    // text
    v = rootProjectDir.toPath().resolve("test.txt");
    Files.write(v, " text ".getBytes());
    assertTrue(ProjectPropertiesHelper.tryLoadPropertiesFile(v.toFile()).isEmpty());

    // non existent
    var vv = new File(rootProjectDir, "nonexistent.properties");
    assertTrue(ProjectPropertiesHelper.tryLoadPropertiesFile(vv).isEmpty());
  }

}