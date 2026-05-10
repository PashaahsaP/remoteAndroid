plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.wmsRemote"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.wmsRemote"
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
    buildFeatures{
        viewBinding = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation ("com.google.code.gson:gson:2.10")
    implementation ("androidx.room:room-ktx:2.6.1")
    implementation ("androidx.core:core-ktx:1.15.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-core:2.8.7")
    implementation(libs.androidx.swiperefreshlayout)
    kapt ("androidx.room:room-compiler:2.6.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.opencsv:opencsv:5.9")
    implementation("androidx.work:work-runtime-ktx:2.10.1")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    debugImplementation("androidx.fragment:fragment-testing:1.6.2")
    debugImplementation("androidx.test.ext:junit:1.2.1")
    debugImplementation("androidx.test:runner:1.6.2")
    debugImplementation("androidx.test:rules:1.6.1")
    debugImplementation("androidx.test.espresso:espresso-core:3.6.1")
    debugImplementation("androidx.test.espresso:espresso-contrib:3.6.1")
}