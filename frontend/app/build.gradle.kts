import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    // secrets gradle plugin
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.cpen321.squadup"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cpen321.squadup"
        minSdk = 31
        //noinspection OldTargetApi
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        val localProperties = Properties()
        localProperties.load(FileInputStream(rootProject.file("local.properties")))
        buildConfigField("String", "GOOGLE_PLACES_API_KEY", "\"${localProperties.getProperty("GOOGLE_PLACES_API_KEY")}\"")

        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = localProperties.getProperty("GOOGLE_PLACES_API_KEY")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    flavorDimensions += "env" // Name of your flavor dimension
    productFlavors {

        create("local") {
            dimension = "env"
            // Local dev endpoints
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:3000/api/\"")
            buildConfigField("String", "IMAGE_BASE_URL", "\"http://10.0.2.2:3000/\"")
            buildConfigField("String", "NEWS_API_KEY", "\"e614e65892e045deb1d4ad50f2449ef0\"")
            buildConfigField(
                "String",
                "GOOGLE_CLIENT_ID",
                "\"401885055971-1jdbm4p5ferqrit0cbi73ie3664ejlpi.apps.googleusercontent.com\""
            )
        }

        create("staging") {
            dimension = "env"
            // Deployed test server endpoints
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"http://ec2-18-221-196-3.us-east-2.compute.amazonaws.com/api/\""
            )
            buildConfigField(
                "String",
                "IMAGE_BASE_URL",
                "\"http://ec2-18-221-196-3.us-east-2.compute.amazonaws.com/\""
            )
            buildConfigField(
                "String",
                "GOOGLE_CLIENT_ID",
                "\"282207727635-uqma630dg0ldl557l01es2h7uqhmtg9r.apps.googleusercontent.com\""
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true // need to build the app (no just sync)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended:1.6.8")

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // Google Sign-In
    implementation(libs.play.services.auth)

    // HTTP client
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Image loading
    implementation(libs.coil.compose)

    // Camera and Image handling
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Shared Preferences
    implementation(libs.androidx.datastore.preferences)

    // Material Design Components
    implementation(libs.material)

    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    implementation(libs.places) // maps places api
    implementation(libs.maps.compose)// map view with compose

    //map component
    implementation(libs.maps.compose.v433)
    implementation("com.google.maps.android:android-maps-utils:3.19.0")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
