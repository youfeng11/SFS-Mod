import java.util.Properties
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
    alias(libs.plugins.aboutlibraries)
}

val keystoreDir = "$rootDir/keystore"
val keystoreProps = Properties()
for (name in arrayOf("release.properties")) {
    val f = file("$keystoreDir/$name")
    if (!f.exists()) continue
    keystoreProps.load(f.inputStream())
    break
}

android {
    namespace = "com.youfeng.sfsmod"
    compileSdk = 35
    ndkVersion = "27.1.12297006"
    
    defaultConfig {
        applicationId = "com.StefMorojna.SpaceflightSimulator"
        minSdk = 23
        targetSdk = 35
        versionCode = 375
        versionName = "1.5.10.5-3.0.0.Beta03"
        
        vectorDrawables { 
            useSupportLibrary = true
        }
        
        ndk {
            abiFilters.clear()
            abiFilters.add("arm64-v8a")
            abiFilters.add("armeabi-v7a")
        }
    }

    signingConfigs {
        val keyAlias = keystoreProps.getProperty("keyAlias")
        val keyPassword = keystoreProps.getProperty("keyPassword")
        val storeFile = file("$keystoreDir/${keystoreProps.getProperty("storeFile")}")
        val storePassword = keystoreProps.getProperty("storePassword")

        create("release") {
            this.keyAlias = keyAlias
            this.keyPassword = keyPassword
            this.storeFile = storeFile
            this.storePassword = storePassword
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_17
        sourceCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        
        debug {
            val dateFormat = SimpleDateFormat("yyMMddHHmmss")
            val currentDateTime = dateFormat.format(Date())
            versionNameSuffix = ".build$currentDateTime" // 使用UTC时间
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    aboutLibraries {
        registerAndroidTasks = false
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.compose.material3)
    implementation(libs.compose.uiToolingPreview)
    
    implementation(libs.androidx.lifecycle)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    
    implementation(libs.androidx.core)
    implementation(libs.compose.ui)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.compose.uiTooling)
    implementation(libs.compose.materialIcons)
    
    implementation(libs.about.libraries.core)
    implementation(libs.about.libraries.ui)
    
    implementation(libs.square.okio)
}
