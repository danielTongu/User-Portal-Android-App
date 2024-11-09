plugins {
    alias(libs.plugins.androidApplication)

    //------ firebase setup ---------//
    id("com.google.gms.google-services")// Add the Google services Gradle plugin
    //-------------------------------//
}

android {
    namespace = "com.example.chatandroidapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.chatandroidapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        viewBinding = true; // connects directly to Java code instead of assigning ids
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.roundedimageview)

    //------ firebase setup ---------//
    implementation(libs.firebase.bom)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.firestore) // Import the Firebase BoM
    //-------------------------------//

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}