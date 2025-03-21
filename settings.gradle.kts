@file:Suppress("UnstableApiUsage")

pluginManagement {
  repositories {
    mavenLocal()
    gradlePluginPortal()
    google()
    mavenCentral()
    maven { setUrl("https://maven.scijava.org/content/repositories/public/") }
    maven { setUrl("https://androidx.dev/storage/compose-compiler/repository/") }
  }
}

dependencyResolutionManagement {
  @Suppress("UnstableApiUsage")
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    mavenLocal()
    google()
    mavenCentral()
    maven("https://jitpack.io")
    maven { setUrl("https://maven.scijava.org/content/repositories/public/") }
    maven { setUrl("https://androidx.dev/storage/compose-compiler/repository/") }
  }
}

rootProject.name = "航天模拟器安装器"

include(":app")