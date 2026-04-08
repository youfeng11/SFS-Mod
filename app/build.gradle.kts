import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.mikepenz.aboutlibraries.plugin.DuplicateMode
import com.mikepenz.aboutlibraries.plugin.DuplicateRule
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.gradle)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.aboutlibraries.android)
}

/** 安装器版本 */
val installerVersionName = "3.3.0"

/**
 * 获取SFS安装包版本信息
 */
fun getSfsVersionInfo(): Pair<Int, String> {
    val apkFile = project.file("src/main/assets/base.apk.1")

    /** 获取失败时使用的版本 */
    val fallbackVersion = 1 to "error"

    if (!apkFile.exists()) {
        println("[Build] WARNING: APK file not found at ${apkFile.absolutePath}. Using fallback version.")
        return fallbackVersion
    }

    try {
        val androidExtension = project.extensions.getByType<ApplicationExtension>()
        val androidComponents =
            project.extensions.getByType<ApplicationAndroidComponentsExtension>()

        val sdkDir = androidComponents.sdkComponents.sdkDirectory.get().asFile
        val buildToolsVersion = androidExtension.buildToolsVersion
        val aapt2Name = if (OperatingSystem.current().isWindows) "aapt2.exe" else "aapt2"
        val aapt2 = File(sdkDir, "build-tools/$buildToolsVersion/$aapt2Name")

        if (!aapt2.exists()) {
            println("[Build] ERROR: aapt2 utility not found at ${aapt2.absolutePath}. Using fallback version.")
            return fallbackVersion
        }

        val output = project.providers.exec {
            commandLine(aapt2.absolutePath, "dump", "badging", apkFile.absolutePath)
        }.standardOutput.asText.get()

        val vCode = Regex("versionCode='(\\d+)'").find(output)?.groupValues?.get(1)?.toInt() ?: 1
        val vName = Regex("versionName='([^']+)'").find(output)?.groupValues?.get(1) ?: "unknown"

        val result = vCode to vName
        println("[Build] SUCCESS: Read version from APK -> versionCode=${result.first}, versionName=${result.second}")
        return result
    } catch (e: Exception) {
        println("[Build] EXCEPTION: Failed to read APK version (${e.message}). Using fallback version.")
        return fallbackVersion
    }
}

val versionInfo by lazy {
    val (sfsVersionCode, sfsVersionName) = getSfsVersionInfo()
    (sfsVersionCode to "$sfsVersionName-$installerVersionName").also { (vCode, vName) ->
        println("[Build] INFO: Using versionCode=$vCode, versionName=$vName")
    }
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
    compileSdk = 36

    defaultConfig {
        applicationId = "com.StefMorojna.SpaceflightSimulator"
        minSdk = 23
        targetSdk = 36

        versionCode = versionInfo.first
        versionName = versionInfo.second

        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
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
        targetCompatibility = JavaVersion.VERSION_21
        sourceCompatibility = JavaVersion.VERSION_21
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            versionNameSuffix = "-debug"
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
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

aboutLibraries {
    collect {
        configPath = file("aboutlibs_config")
    }
    library {
        duplicationMode = DuplicateMode.MERGE
        duplicationRule = DuplicateRule.SIMPLE
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.material3)
    implementation(libs.compose.materialIcons)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.androidx.core)
    implementation(libs.compose.ui)

    implementation(libs.androidx.lifecycle)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.compose.uiTooling)

    implementation(libs.about.libraries.ui)

    implementation(libs.okio)
}
