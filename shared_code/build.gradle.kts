import org.jetbrains.kotlin.gradle.tasks.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


plugins {
    kotlin("multiplatform")
    id("com.android.library")

}

apply(plugin = "org.jetbrains.kotlin.multiplatform")


android{
    compileSdkVersion(29)
    buildToolsVersion = "29.0.3"
    defaultConfig{
        minSdkVersion(19)
        targetSdkVersion(29)
    }
}




kotlin {
    //select iOS target platform depending on the Xcode environment variables
    jvm()
    android()
    val iosArm32 = iosArm32("iosArm32")
    //val iosArm64 = iosArm64("iosArm64")
    //val iosX64 = iosX64("iosX64")
    val iOSTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget =
        if (System.getenv("SDK_NAME")?.startsWith("iphoneos") == true)
            ::iosArm64
        else
            ::iosX64

    iOSTarget("ios") {
        binaries {
            framework {
                baseName = "shared_code"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
            }
        }

        val jvmMain by getting {
            dependencies {

            }
        }

        val androidMain by getting {
            dependsOn(jvmMain)

            dependencies {

            }
        }


        sourceSets["commonMain"].dependencies {
            implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
        }

        sourceSets["androidMain"].dependencies {
            implementation("androidx.appcompat:appcompat:1.1.0")
            implementation("org.jetbrains.kotlin:kotlin-stdlib")

        }


    }
}



val packForXcode by tasks.creating(Sync::class) {
    val targetDir = File(buildDir, "xcode-frameworks")

    /// selecting the right configuration for the iOS
    /// framework depending on the environment
    /// variables set by Xcode build
    val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
    val framework = kotlin.targets
        .getByName<KotlinNativeTarget>("ios")
        .binaries.getFramework(mode)
    inputs.property("mode", mode)
    dependsOn(framework.linkTask)

    from({ framework.outputDirectory })
    into(targetDir)

    /// generate a helpful ./gradlew wrapper with embedded Java path
    doLast {
        val gradlew = File(targetDir, "gradlew")
        gradlew.writeText("#!/bin/bash\n"
                + "export 'JAVA_HOME=${System.getProperty("java.home")}'\n"
                + "cd '${rootProject.rootDir}'\n"
                + "./gradlew \$@\n")
        gradlew.setExecutable(true)
    }
}



tasks.getByName("build").dependsOn(packForXcode)