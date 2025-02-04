import com.android.utils.TraceUtils.simpleId

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.qrcodescannerapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.qrcodescannerapp"
        minSdk = 26
        targetSdk = 34
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.camera.view)
    implementation(libs.camera.lifecycle)
    implementation(libs.barcode.scanning.common)
    implementation(libs.firebase.ml.vision.barcode.model)
    implementation(libs.play.services.mlkit.barcode.scanning)
    implementation(libs.play.services.mlkit.barcode.barcode)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}