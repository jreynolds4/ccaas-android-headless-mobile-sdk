plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "co.ccai.example.xml_example"
    compileSdk = 35

    defaultConfig {
        applicationId = "co.ccai.example.xml_example"
        minSdk = 21
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = false
        viewBinding = true
    }
}

dependencies {

    implementation(project(":Shared"))
    implementation(libs.ccai.kit)
    implementation(libs.ccai.chat)
    implementation(libs.ccai.chat.red)
    implementation(libs.ccai.ui)
    implementation(libs.ccai.screenshare) //Its optional module, you can include it if your app requires screen sharing feature.

    // CCAIUI has a transitive dependency on Compose material3 so including it here
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.material)
    implementation(libs.coil.compose)
    implementation(libs.gson)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.fragment.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.gson)
    implementation(libs.androidx.recyclerview)
    implementation(libs.coil.ktx)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.google.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
