dependencies {
    compileOnly(project(":common"))

    implementation(rootProject.libs.jdbc.sqlite)
    implementation(rootProject.libs.kotlinx.serialization.json)
}