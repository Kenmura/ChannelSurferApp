plugins {
    id("com.android.application")
}

android {
    namespace = "tv.channelsurfer.app"
    compileSdk = 30

    defaultConfig {
        applicationId = "tv.channelsurfer.app"
        minSdk = 21
        targetSdk = 30
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        val storePw = System.getenv("STORE_PASSWORD")
        if (storePw != null && storePw.isNotBlank()) {
            create("release") {
                storeFile = file("release-keystore.jks")
                storePassword = storePw
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (System.getenv("STORE_PASSWORD")?.isNotBlank() == true) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
}
