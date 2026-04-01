import com.mikepenz.aboutlibraries.plugin.DuplicateMode
import com.mikepenz.aboutlibraries.plugin.DuplicateRule
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties
import java.util.UUID

plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.gradle)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.aboutlibraries.android)
}

/** 安装器版本 */
val installerVersionName = "3.2.1"

/**
 * 从 SFS 安装包读取版本名与版本号
 */
fun readApkVersionViaAapt2(apkFile: File): Pair<Int, String> {
    val defaultVersion = 1 to "error"

    if (!apkFile.exists()) {
        println("WARNING: ${apkFile.path} not found, using default version")
        return defaultVersion
    }

    val sdkDir = System.getenv("ANDROID_HOME")
        ?: System.getenv("ANDROID_SDK_ROOT")
        ?: "${System.getProperty("user.home")}/Android/Sdk"

    val buildToolsBase = File(sdkDir, "build-tools")
    if (!buildToolsBase.exists()) {
        println("WARNING: build-tools dir not found: ${buildToolsBase.path}, using default version")
        return defaultVersion
    }

    val latestBuildTools = buildToolsBase.listFiles()
        ?.filter { it.isDirectory }
        ?.maxByOrNull { it.name }
        ?: run {
            println("WARNING: no build-tools found, using default version")
            return defaultVersion
        }

    val isWindows = System.getProperty("os.name").lowercase().contains("win")
    val aapt2 = File(latestBuildTools, if (isWindows) "aapt2.exe" else "aapt2")

    if (!aapt2.exists()) {
        println("WARNING: aapt2 not found: ${aapt2.path}, using default version")
        return defaultVersion
    }

    return try {
        val process = ProcessBuilder(
            "sh", "-c",
            "\"${aapt2.absolutePath}\" dump badging \"${apkFile.absolutePath}\""
        )
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader(Charsets.UTF_8).readText()
        process.waitFor()

        println(">>> aapt2 output (first 3 lines):\n${output.lines().take(3).joinToString("\n")}")

        val versionCode = Regex("""versionCode='(\d+)'""").find(output)
            ?.groupValues?.get(1)?.toIntOrNull()
            ?: run {
                println("WARNING: versionCode not found in aapt2 output, using default")
                return defaultVersion
            }

        val versionName = Regex("""versionName='([^']+)'""").find(output)
            ?.groupValues?.get(1)
            ?: run {
                println("WARNING: versionName not found in aapt2 output, using default")
                return defaultVersion
            }

        val modVersionName = "$versionName-$installerVersionName"
        println("OK: version read from base.apk.1 -> versionName=$modVersionName, versionCode=$versionCode")
        Pair(versionCode, modVersionName)
    } catch (e: Exception) {
        println("WARNING: aapt2 execution failed: ${e.message}, using default version")
        defaultVersion
    }
}

val apkVersionInfo: Pair<Int, String> by lazy {
    val apkFile = File(projectDir, "src/main/assets/base.apk.1")
    readApkVersionViaAapt2(apkFile)
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

        versionCode = apkVersionInfo.first
        versionName = apkVersionInfo.second

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
            val randomSuffix = UUID.randomUUID().toString().take(6)
            val dateFormat = SimpleDateFormat("yyMMdd")
            val currentDateTime = dateFormat.format(Date())
            versionNameSuffix = ".$currentDateTime.$randomSuffix"
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
        collect {
            configPath = file("aboutlibs_config")
        }
        library {
            duplicationMode = DuplicateMode.MERGE
            duplicationRule = DuplicateRule.SIMPLE
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
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