plugins {
    id("com.android.library")
    kotlin("android")
    `maven-publish`
}

android {
    namespace = "ru.beryukhov.reactivenetwork"

    compileSdk = libs.versions.compileSdk.get().toInt()
    testOptions.unitTests.isIncludeAndroidResources = true

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_17
        sourceCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }

    kotlin {
        explicitApi()
    }
}

dependencies {

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    implementation(libs.androidx.annotation)


    testImplementation(libs.kotlinTest.common)
    testImplementation(libs.kotlinTest.annotations)

    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockk)

    testImplementation(libs.androidx.test)
}


//apply {from("${rootProject.projectDir}/scripts/publish-root.gradle")}
//apply {from("${rootProject.projectDir}/scripts/publish-module.gradle")}
