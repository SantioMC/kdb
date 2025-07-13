plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.objensis)

    implementation(kotlin("reflect"))

    api(libs.hikaricp)

    testImplementation(project(":test"))

    testApi(project(":processor"))
    testApi(project(":platform:mysql"))
    testApi(project(":platform:sqlite"))
    kspTest(project(":processor"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}