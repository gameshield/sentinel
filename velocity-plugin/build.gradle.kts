plugins {
    java
    id("com.gradleup.shadow") version "9.2.2"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    compileOnly(libs.netty.all)
    implementation(project(":base"))

    compileOnly(libs.velocity.api)
    compileOnly(libs.velocity.proxy)

    annotationProcessor(libs.velocity.api)
}