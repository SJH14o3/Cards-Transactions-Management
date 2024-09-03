plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.sjh14o3.transactionsManager"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sjh14o3.transactionsManager"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    configurations {
        all {
            exclude("com.android.support", "support-compat")
        }
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
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation(files("C:\\permanently\\libraries\\android\\commons-codec-1.15.jar"))
    implementation(files("C:\\permanently\\libraries\\android\\commons-codec-1.15.pom"))
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.material3.android)
    implementation("com.github.prolificinteractive:material-calendarview:2.0.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}