import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    kotlin("plugin.serialization") version "1.9.21"
}

//선언 및 키 값 가져오기
val localProperties = Properties()
localProperties.load(FileInputStream(rootProject.file("local.properties")))


android {
    namespace = "com.example.menupop"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.menupop"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "SERVER_IP", localProperties.getProperty("SERVER_IP"))
        buildConfigField("String", "TRANSLATION_SERVER_IP", localProperties.getProperty("TRANSLATION_SERVER_IP"))
        buildConfigField("String", "KAKAOPAY_ADMIN_KEY", localProperties.getProperty("KAKAOPAY_ADMIN_KEY"))
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", localProperties.getProperty("KAKAO_NATIVE_APP_KEY"))
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY_STELLA", localProperties.getProperty("KAKAO_NATIVE_APP_KEY_STELLA"))
        buildConfigField("String", "GOOGLE_API_KEY", localProperties.getProperty("GOOGLE_API_KEY"))
        buildConfigField("String", "GOOGLE_LOGIN_KEY", localProperties.getProperty("GOOGLE_LOGIN_KEY"))
        buildConfigField("String", "SOCIAL_LOGIN_INFO_NAVER_CLIENT_ID", localProperties.getProperty("SOCIAL_LOGIN_INFO_NAVER_CLIENT_ID"))
        buildConfigField("String", "SOCIAL_LOGIN_INFO_NAVER_CLIENT_SECRET", localProperties.getProperty("SOCIAL_LOGIN_INFO_NAVER_CLIENT_SECRET"))
        buildConfigField("String", "BANK_API_KEY", localProperties.getProperty("BANK_API_KEY"))
        buildConfigField("String", "GOOGLE_AD_ID", localProperties.getProperty("GOOGLE_AD_ID"))
    }

    buildFeatures{
        buildConfig = true
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    dataBinding{
        enable=true
    }

}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")

    implementation ("com.kakao.sdk:v2-user:2.18.0")

    implementation ("com.google.gms:google-services:4.3.15")
    implementation ("com.google.firebase:firebase-auth:22.0.0")
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation ("com.google.android.gms:play-services-auth:20.5.0")

    implementation ("com.navercorp.nid:oauth-jdk8:5.1.0") // jdk 8

    // Kotlin coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Retrofit coroutine adapter
    implementation ("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.3")

    implementation("com.google.android.gms:play-services-ads:23.5.0")

    implementation ("com.github.zynkware:Document-Scanning-Android-SDK:1.1.1")
    implementation ("com.github.bumptech.glide:glide:4.12.0")

    implementation ("com.github.fondesa:kpermissions:3.4.0")
    implementation ("com.github.fondesa:kpermissions-rx2:3.4.0")
    implementation ("com.github.fondesa:kpermissions-rx3:3.4.0")
    implementation ("com.github.fondesa:kpermissions-coroutines:3.4.0")

    // ML Kit
    implementation("com.google.mlkit:language-id:17.0.4")
    implementation("com.google.mlkit:text-recognition:16.0.1") // 라틴
    implementation ("com.google.mlkit:text-recognition-chinese:16.0.1")
    implementation ("com.google.mlkit:text-recognition-japanese:16.0.1")
}