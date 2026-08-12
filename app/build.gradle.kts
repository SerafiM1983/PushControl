plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.serafimApp.pushcontrol"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.serafimApp.pushcontrol"
        minSdk = 26
        targetSdk = 36
        versionCode = 6
        versionName = "1.1"

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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.fragment.testing)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Обязательно для работы Mockito на эмуляторе/реальном устройстве
    androidTestImplementation ("org.mockito:mockito-android:5.11.0")
    androidTestImplementation ("androidx.test.ext:junit:1.2.1")
    androidTestImplementation ("androidx.test:runner:1.6.1")

    // Подключение рекламы
    implementation("com.yandex.android:mobileads:7.12.0")
}