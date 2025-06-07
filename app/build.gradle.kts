plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.nutrifit4"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.nutrifit4"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // 여기서 JavaCompile 태스크에 -Xlint:deprecation을 추가합니다.
    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:deprecation")
    }
}

dependencies {
    val FOODLENS_SDK_VERSION by extra("1.0.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.activity.ktx)
    implementation(libs.commons.math3)
    implementation(libs.opencsv)
    implementation("com.doinglab.foodlens:FoodLensSDK-core:3.0.9") {
        exclude(group = "com.android.support", module = "appcompat")
        exclude(group = "com.android.support", module = "design")
        exclude(group = "com.android.support", module = "support-v4")
        exclude(group = "com.android.support.constraint", module = "constraint-layout")
    }
    implementation(libs.okhttp)
    implementation(libs.gson)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}