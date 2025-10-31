allprojects {
    group = "io.gameshield"
    version = "1.0.0"
}

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven(url = "https://repo.abelix.club/repository/public")
    }
}