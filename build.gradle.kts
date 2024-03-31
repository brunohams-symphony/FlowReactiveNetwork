plugins {
    id("convention.detekt")
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath(libs.androidGradle)
        classpath(libs.kotlinGradle)
        classpath(libs.bintrayGradle)
    }
}
allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://dl.bintray.com/andreyberyukhov/FlowReactiveNetwork")
    }
}

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
