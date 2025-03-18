# Gradle Plugin : Include Properties

Gradle plugin to easily include multiple property files into your build.

- License: MIT

## Setup

- clone the project
- execute the task publishToMavenLocal
- edit your "build.gradle" and add the plugin

```groovy
plugins {
  id 'gradle-include-properties' version '1.0.0'
}

// (optional) after evaluation, check if the keys exists
expectedProperties {
  expected = [
    "my_key_1",
    "my_key_2",
    "my_key_3" 
  ]
}
```

- edit your "gradle.properties"
````properties
org.gradle.jvmargs=-Xmx4G
# etc...

include.properties=extra.properties
include.properties.override=true
````

- create the file "extra.properties" to be included
````properties
my_key_1=a
my_key_2=b
my_key_3=c
````

````groovy
task printProperties {
  doLast {
    println "my_key_1 <- ${project.findProperty('my_key_1')}"
    println "my_key_2 <- ${project.findProperty('my_key_2')}"
    println "my_key_3 <- ${project.findProperty('my_key_3')}"
  }
}
````

With multiple file
````properties
include.properties=file1.properties,../file2.properties,my-directory
include.properties.override=true
````
