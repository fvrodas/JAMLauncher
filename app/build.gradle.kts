plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlinCompose)
}

android {
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.fvrodas.jaml"
        minSdk = 24
        lint.targetSdk = 36
        versionCode = 6
        versionName = "1.0.0"

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
        kotlinCompilerExtensionVersion = "1.5.10"
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
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.androidx.junit)
    androidTestImplementation(libs.test.androidx.espresso)
}