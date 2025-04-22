plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.voicecontrolledinterface"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.voicecontrolledinterface"
        minSdk = 24
        targetSdk = 35
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
    implementation ("androidx.core:core:1.13.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")  // For API calls
    implementation ("com.google.code.gson:gson:2.8.8")  // For JSON parsing
    implementation ("androidx.core:core-ktx:1.6.0") // For modern Android compatibility
    implementation ("com.google.android.material:material:1.10.0")
    implementation ("androidx.cardview:cardview:1.0.0")


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}