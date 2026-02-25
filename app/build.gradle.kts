plugins {
    id("com.android.application")
}

android {
    namespace = "com.my.downloader"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.my.downloader"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
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
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Thư viện tải ảnh
    implementation("com.github.bumptech.glide:glide:4.15.1")
    
    // Thư viện mạng & JSON
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.json:json:20231013") // Thêm JSON parsing
}