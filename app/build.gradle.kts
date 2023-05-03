@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.camerapreviewtest"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.camerapreviewtest"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }
    kotlin {
        jvmToolchain(17)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.6"
    }

}

dependencies {
// defines a stable set of compose versions, no more compose versions needed
    implementation(platform(libs.androidx.compose.bom))

    // compose
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3.window.sizes)
    implementation("androidx.compose.material3:material3:1.1.0-rc01")
    //implementation(libs.androidx.compose.material3) TODO wait for 1.1.0 to be part of bom
    debugImplementation(libs.androidx.compose.ui.tooling)

    // androidx extensions
    implementation(libs.androidx.activity.compose)

    // barcode reading
    implementation(libs.mlkit.barcode.scanning)

    // camera
    implementation(libs.camera.core)
    implementation(libs.camera.view)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.video)
    implementation(libs.camera.extensions)
}