plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinx.serialization)

    `java-library`
    `maven-publish`
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
    apply(plugin = "maven-publish")
    apply(plugin = rootProject.libs.plugins.kotlin.get().pluginId)
    apply(plugin = rootProject.libs.plugins.kotlinx.serialization.get().pluginId)

    dependencies {
        implementation(rootProject.libs.slf4j.api)
    }

    kotlin {
        jvmToolchain(21)
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])

                groupId = rootProject.group.toString()
                version = rootProject.version.toString()
                artifactId = artifactId(project)
            }
        }
    }
}

fun artifactId(project: Project): String =
    project.parent?.takeIf { it != rootProject }?.let { artifactId(it) }?.plus("-${project.name}") ?: project.name