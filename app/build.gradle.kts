plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.ezt.video.instasaver"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ezt.video.instasaver"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }

    base {
        archivesName.set("InstaSaver_${defaultConfig.versionName}")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.leanback.paging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit 2
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    //UI dimen dp generator
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${libs.versions.coroutines.get()}")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    implementation("androidx.navigation:navigation-fragment-ktx:2.9.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.5")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.5")

    implementation("com.squareup.picasso:picasso:2.71828")

    // Hilt 2.55
    implementation("com.google.dagger:hilt-android:2.55")
    kapt("com.google.dagger:hilt-compiler:2.55")
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")

    // Room + KSP
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Glide + KSP
    implementation("com.github.bumptech.glide:glide:4.15.1")
    ksp("com.github.bumptech.glide:compiler:4.15.1")

    //advertisement
//    implementation("com.github.thienlp201097:DktechLib:2.1.5")
//    implementation("com.facebook.android:facebook-android-sdk:18.0.2")
//    implementation("com.github.thienlp201097:smart-app-rate:1.0.7")
//
//    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
//    implementation("com.google.firebase:firebase-analytics-ktx")
//    implementation("com.google.firebase:firebase-messaging")
//
//    implementation(libs.adjust.android)
//    implementation("com.android.installreferrer:installreferrer:2.2")
//
//    implementation("com.google.ads.mediation:pangle:7.2.0.6.0")
//    implementation("com.google.ads.mediation:applovin:13.5.0.0")
//    implementation("com.google.ads.mediation:facebook:6.20.0.2")
//    implementation("com.google.ads.mediation:vungle:7.6.0.0")
//    implementation("com.google.ads.mediation:mintegral:16.9.91.2")
//
//    implementation("com.google.android.gms:play-services-ads:24.7.0")
//    implementation ("com.airbnb.android:lottie:6.4.0")
    //check update
//    implementation("com.google.android.play:app-update:2.1.0")
//    implementation("com.google.android.play:app-update-ktx:2.1.0")


}