plugins {
    id("com.google.gms.google-services") version "4.4.0" // Ganti dengan versi terbaru yang sesuai
    id("com.android.application") version "8.3.1" // Ganti dengan versi AGP yang kamu gunakan
}

android {
    namespace = "com.example.posyanduapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.posyanduapp"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation ("com.airbnb.android:lottie:6.0.0")
    implementation ("com.google.firebase:firebase-database:21.0.0")
    implementation ("com.google.firebase:firebase-core:21.1.1")
    implementation ("androidx.cardview:cardview:1.0.0")



    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}