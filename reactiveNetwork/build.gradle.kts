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
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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


    testImplementation(libs.kotlin.test)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.test)
}


//apply {from("${rootProject.projectDir}/scripts/publish-root.gradle")}
//apply {from("${rootProject.projectDir}/scripts/publish-module.gradle")}
