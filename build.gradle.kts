plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinx.serialization)

    `java-library`
}

group = "me.santio"
version = "1.0-SNAPSHOT"

allprojects {
    group = rootProject.group.toString()
    version = rootProject.version as String

    apply(plugin = "java-library")

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = rootProject.libs.plugins.kotlin.get().pluginId)
    apply(plugin = rootProject.libs.plugins.kotlinx.serialization.get().pluginId)

    dependencies {
        implementation(rootProject.libs.slf4j.api)
    }

    kotlin {
        jvmToolchain(21)
    }
}
