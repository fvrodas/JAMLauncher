plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        lint.targetSdk = 36

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    namespace = "io.github.fvrodas.jaml.core"
}

dependencies {
    implementation(libs.koin)
    implementation(libs.androidx.ktx)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization)

//    testImplementation "org.mockito:mockito-core:3.11.1"
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.androidx.junit)
    androidTestImplementation(libs.test.androidx.espresso)
}