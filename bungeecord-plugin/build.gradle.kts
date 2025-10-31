plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    compileOnly(libs.jetbrains.annotations)

    compileOnly(libs.netty.all)
    implementation(project(":base"))

    compileOnly("net.md-5:bungeecord-api:1.21-R0.4")
    compileOnly("net.md-5:bungeecord-proxy:1.21-R0.4-SNAPSHOT")
}