//import org.jetbrains.kotlin.gradle.dsl.JvmTarget
//
//plugins {
//    alias(libs.plugins.kotlinMultiplatform)
//    alias(libs.plugins.androidMultiplatformLibrary)
//    alias(libs.plugins.kotlinxSerialization)
//}
//
//kotlin {
//    listOf(
//        iosArm64(),
//        iosSimulatorArm64()
//    ).forEach { iosTarget ->
//        iosTarget.binaries.framework {
//            baseName = "SharedLogic"
//            isStatic = true
//        }
//    }
//
//    androidLibrary {
//       namespace = "com.example.convene.sharedLogic"
//       compileSdk = libs.versions.android.compileSdk.get().toInt()
//       minSdk = libs.versions.android.minSdk.get().toInt()
//
//       compilerOptions {
//           jvmTarget = JvmTarget.JVM_11
//       }
//    }
//
//    sourceSets {
//        commonMain.dependencies {
//            implementation(libs.kotlinx.serialization.json)
//        }
//        androidMain.dependencies {
//            implementation(libs.androidx.security.crypto)
//        }
//        commonTest.dependencies {
//            implementation(libs.kotlin.test)
//        }
//    }
//}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidLibrary {
        namespace = "com.example.convene.sharedLogic"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SharedLogic"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.androidx.security.crypto)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}