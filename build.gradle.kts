plugins {
    kotlin("jvm") version "2.4.0-RC"
    id("maven-publish")
}

group = "cat.emir"
version = "1.0.2"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

//    !! THESE ARE REQUIRED TO BE ADDED TO THE PLUGINS AS paperLibrary!!
    compileOnly("org.spongepowered:configurate-yaml:4.1.2")
    compileOnly("io.github.classgraph:classgraph:4.8.179")
    compileOnly("com.h2database:h2:2.3.232")
    compileOnly("com.zaxxer:HikariCP:7.0.2")
}

publishing {
    publications {
        create<MavenPublication>("publish") {
            from(components["java"])
        }
    }
}

kotlin {
    jvmToolchain(21)
}