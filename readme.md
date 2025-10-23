# Gradle Plugin : Include Properties

The **Include Properties** plugin allows you to include external `.properties` files into your Gradle project,
optionally overriding existing properties.

- **License:** MIT
- **Author:** JTorleon Studios Team
- **Public Maven Repository:** https://jtorleon-studios-team-github-io.pages.dev/
- **Public Github Repository:** https://github.com/jtorleon-studios-team/gradle-include-properties

## Installation

Add the public Maven repository in your `settings.gradle`:

```groovy
pluginManagement {
  repositories {
    maven {
      url = 'https://jtorleon-studios-team-github-io.pages.dev/'
    }
  }
}
```

Then apply the plugin in your `build.gradle`:

```groovy
plugin {
  id 'gradle-include-properties' version '1.0.0'
}
```

## Setup

### 1. Configure expected properties (optional)

```groovy
expectedProperties {
  expected = [
    "my_key_1",
    "my_key_2",
    "my_key_3"
  ]
}
```

This block can be used after project evaluation to verify that certain keys exist.

### 2. Edit gradle.properties

```properties
org.gradle.jvmargs=-Xmx4G
# etc...
include.properties=extra.properties
include.properties.override=true
```

- `include.properties` can be a single file, multiple files (comma-separated), or directories.
- `include.properties.override` determines if properties in included files override existing ones.

### 3. Create included file(s)

`extra.properties`:

```properties
my_key_1=a
my_key_2=b
my_key_3=c
```

### 4. Access properties in Gradle tasks

```groovy
task printProperties {
  doLast {
    println "my_key_1 <- ${project.findProperty('my_key_1')}"
    println "my_key_2 <- ${project.findProperty('my_key_2')}"
    println "my_key_3 <- ${project.findProperty('my_key_3')}"
  }
}
```

### 5. Include multiple files or directories

````properties
include.properties=file1.properties,../file2.properties,my-directory
include.properties.override=true
````

All properties from the listed files/directories will be loaded, optionally overriding existing values.