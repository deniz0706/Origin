plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.deniz0706.origin"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.deniz0706.origin"

        minSdk = 26
        targetSdk = 37

        versionCode = 1
        versionName = "1.0.0-rc1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

tasks.register("printVersionName") {
    group = "help"
    description = "Prints the configured application version name for CI."
    doLast {
        println(android.defaultConfig.versionName)
    }
}

tasks.matching { it.name == "assembleDebug" }.configureEach {
    doLast {
        val outputDirectory = layout.buildDirectory.dir("outputs/apk/debug").get().asFile
        val defaultApk = outputDirectory.resolve("app-debug.apk")
        val versionedApk = outputDirectory.resolve(
            "Origin-v${android.defaultConfig.versionName}-debug.apk",
        )

        check(defaultApk.renameTo(versionedApk)) {
            "Could not rename ${defaultApk.name} to ${versionedApk.name}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.09.00")

    implementation(composeBom)

    implementation("androidx.activity:activity-compose:1.13.0")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
