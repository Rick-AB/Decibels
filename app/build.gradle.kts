import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.rickinc.decibels"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rickinc.decibels"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.rickinc.decibels.CustomTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType(KotlinCompile::class.java) {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.toString()
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
//    composeOptions {
//        kotlinCompilerExtensionVersion = '1.4.0'
//    }
    testOptions.unitTests.all {
        it.testLogging {
            events = setOf(
                TestLogEvent.PASSED,
                TestLogEvent.FAILED,
                TestLogEvent.SKIPPED,
                TestLogEvent.STANDARD_OUT,
                TestLogEvent.STANDARD_ERROR
            )
        }
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.constraintlayout.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.material)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.google.material)

    // accompanist
    implementation(libs.accompanist.systemuicontroller)

    // hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)

    // koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.bundles.koin.compose)

    // image loading
    implementation(libs.coil.compose)

    implementation(libs.navigation.compose)

    // timber
    implementation(libs.timber)

    // media3
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.session)

    // palette
    implementation(libs.palette.ktx)

    // preferences
    implementation(libs.preference.ktx)

    // room
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    // data store
    implementation(libs.datastore)

    // kotlin serialization
    implementation(libs.kotlinx.serialization.json)

    // gson
    implementation(libs.converter.gson)

    // Moshi
    implementation(libs.moshi.kotlin)

    // jsoup
    implementation(libs.jsoup)

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.moshi)

    // lottie
    implementation(libs.lottie.compose)

    testImplementation(libs.junit)
    testImplementation(libs.coroutine.test)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}