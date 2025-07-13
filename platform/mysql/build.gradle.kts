dependencies {
    compileOnly(project(":common"))

    implementation(rootProject.libs.jdbc.mysql)
    implementation(rootProject.libs.kotlinx.serialization.json)
}