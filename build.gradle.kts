// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.4.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22")
        classpath("com.google.gms:google-services:4.4.0")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.2")
        classpath(kotlin("gradle-plugin", version = "1.9.10"))
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

plugins {
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
        gradlePluginPortal()
    }
}



tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}