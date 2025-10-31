rootProject.name = "gameshield-sentinel"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("lombok", "1.18.36")
            version("annotations", "23.0.0")
            version("junit", "5.10.2")
            version("velocity", "3.3.0-SNAPSHOT")
            version("netty", "4.1.91.Final")

            library("lombok", "org.projectlombok", "lombok").versionRef("lombok")
            library("jetbrains-annotations", "org.jetbrains", "annotations").versionRef("annotations")

            library("junit-jupiter-api", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit")
            library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef("junit")

            library("velocity-api", "com.velocitypowered", "velocity-api").versionRef("velocity")
            library("velocity-proxy", "com.velocitypowered", "velocity-proxy").versionRef("velocity")

            library("netty-all", "io.netty", "netty-all").versionRef("netty")
        }
    }
}

include("base")
include("velocity-plugin")
include("bungeecord-plugin")