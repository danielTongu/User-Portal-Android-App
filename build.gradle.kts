buildscript {
    dependencies {
        classpath(libs.google.services)
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false

    // Google services Gradle plugin to make the google-services.json config values accessible to Firebase SDKs
    id("com.google.gms.google-services") version "4.4.2" apply false


}