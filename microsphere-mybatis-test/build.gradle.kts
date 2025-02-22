
plugins {
    id("buildlogic.java-library-conventions")
}

dependencies {
    // BOM
    implementation(platform(libs.microsphere.java.dependencies))

    // Microsphere Java Code
    "optionalApi"("io.github.microsphere-projects:microsphere-java-core")

    // MyBatis
    "optionalApi"(libs.mybatis)

    // Testing
    api(libs.junit.jupiter.engine)

    // H2 DataBase
    api(libs.h2)

    // Logback
    api(libs.logback.classic)
}