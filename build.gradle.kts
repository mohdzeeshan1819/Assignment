buildscript {

    repositories {
        google()
        mavenCentral()

    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        classpath ("com.android.tools.build:gradle:4.2.2") // or the latest version
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.0") // or the latest version
        classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.6") // or the latest version
        classpath ("com.google.dagger:hilt-android-gradle-plugin:2.38.1") // or the latest version
        // Add this line for data binding
        classpath ("com.android.tools.build:gradle:7.0.4") // or the latest version
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}