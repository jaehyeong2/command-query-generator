# Command-Query-Generator

## Overview

This product is designed to automate the generation of command / response classes in Kotlin based on custom annotations. 
It simplifies the process of creating data classes that represent commands for various operations, enhancing productivity and reducing boilerplate code.

## Features

- Automatically generates command / response classes with default and custom properties.

<!--

## Getting Started

### Prerequisites

- Kotlin 1.9.0 or higher
- Gradle 7.0 or higher

### Installation

To include this annotation processor in your project, add the following dependency in your `build.gradle` file:

```groovy
dependencies {
    implementation 'your.group.id:your-artifact-id:version'
    kapt 'your.group.id:your-annotation-processor-artifact-id:version'
}

-->

## Example
Given a class with the @GenerateCommand annotation:
```
@GenerateInfo
@GenerateCommand(additionalInnerClasses = ["Test"])
class User(
    val id: Long,
    val name: String,
    var phone: String
) {
}
```

The processor will generate a command class named UserCommand with properties corresponding to the fields in the User class.
</br> </br>
![image](https://github.com/user-attachments/assets/0bb4a31f-0614-47a6-9672-7d19e532a63d)


