plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.phonebookapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.phonebookapp"
        minSdk = 25
        targetSdk = 36
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(navigation_compose())

    // Accompanist
    implementation(accompanist_swiperefresh())

    // Lottie
    implementation(lottie_compose())

    // Coil
    implementation(coil_compose())

    // Retrofit & OkHttp
    implementation(retrofit())
    implementation(retrofit_gson())
    implementation(okhttp())

    // Room
    implementation(room_runtime())
    implementation(libs.androidx.compose.material)
    kapt(room_compiler())

    // Palette
    implementation(palette_ktx())

    // Coroutines
    implementation(coroutines_android())
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.navigation:navigation-compose:2.9.5") // navigation için
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.material:material-icons-extended:1.5.0")
    
}

// Fonksiyonları aşağıya ekleyebilirsin
fun core_ktx() = "androidx.core:core-ktx:1.12.0"
fun lifecycle_runtime_ktx() = "androidx.lifecycle:lifecycle-runtime-ktx:2.6.2"
fun activity_compose() = "androidx.activity:activity-compose:1.9.0"

fun compose_ui() = "androidx.compose.ui:ui:1.5.0"
fun compose_material() = "androidx.compose.material:material:1.5.0"
fun compose_ui_tooling_preview() = "androidx.compose.ui:ui-tooling-preview:1.5.0"
fun navigation_compose() = "androidx.navigation:navigation-compose:2.7.3"

fun accompanist_swiperefresh() = "com.google.accompanist:accompanist-swiperefresh:0.30.1"
fun lottie_compose() = "com.airbnb.android:lottie-compose:6.1.0"
fun coil_compose() = "io.coil-kt:coil-compose:2.4.0"

fun retrofit() = "com.squareup.retrofit2:retrofit:2.9.0"
fun retrofit_gson() = "com.squareup.retrofit2:converter-gson:2.9.0"
fun okhttp() = "com.squareup.okhttp3:okhttp:4.11.0"

fun room_runtime() = "androidx.room:room-runtime:2.6.0"
fun room_compiler() = "androidx.room:room-compiler:2.6.0"

fun palette_ktx() = "androidx.palette:palette-ktx:1.0.0"
fun coroutines_android() = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
