plugins {
    id("com.android.application")
}

android {
    namespace = "com.my.downloader"
    compileSdk = 30

    defaultConfig {
        applicationId = "com.my.downloader"
        minSdk = 24
        targetSdk = 30
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // Bắt buộc bật ViewBinding
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.activity:activity:1.3.1")
    
    // Thư viện tải ảnh
    implementation("com.github.bumptech.glide:glide:4.12.0")
    
    // Thư viện mạng & JSON
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation("org.json:json:20210307") // JSON parsing
}

repositories {
    google()
    mavenCentral()
}