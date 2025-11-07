// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kspp) apply false
    id("com.google.dagger.hilt.android") version "2.55" apply false
    id("androidx.navigation.safeargs") version "2.9.6" apply false

}