plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":common"))
    implementation(libs.ksp)
    implementation(libs.kotlinx.serialization.json)
}
