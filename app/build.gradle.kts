plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "ru.mirus"
    compileSdk = 34

    defaultConfig {
        applicationId = "ru.mirus.donsol"
        minSdk = 25
        targetSdk = 33
        versionCode = 2
        versionName = "1.1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        addManifestPlaceholders(mapOf(
            "VKIDRedirectHost" to "vk.com", // Обычно vk.com.
            "VKIDRedirectScheme" to "vk51987085", // Строго в формате vk{ID приложения}.
            "VKIDClientID" to "51987085",
            "VKIDClientSecret" to "r3i2KZZYQ3rZJmFnD3Ry"
        ))
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
        isCoreLibraryDesugaringEnabled = true

    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {
    implementation("com.vk.id:onetap-xml:2.0.0")
    implementation("androidx.transition:transition:1.5.0")
    implementation("com.vk.id:vkid:2.0.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.0")
    implementation("androidx.activity:activity-ktx:1.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}