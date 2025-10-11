

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlinCompose)
}

android {
    compileSdk = libs.versions.sdk.target.get().toInt()

    defaultConfig {
        applicationId = "io.github.fvrodas.jaml"
        minSdk = libs.versions.sdk.min.get().toInt()
        lint.targetSdk = libs.versions.sdk.target.get().toInt()
        versionCode = 2
        versionName = "1.0.0-beta2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        debug {
            isMinifyEnabled = false
        }

        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.ui.get()
    }

    namespace = "io.github.fvrodas.jaml"
}

dependencies {
    implementation(
        project(path = ":core")
    )

    implementation(libs.koin)
    implementation(libs.koin.compose)
    implementation(libs.androidx.ktx)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization)

    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.core.role)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.androidx.junit)
    androidTestImplementation(libs.test.androidx.espresso)
}