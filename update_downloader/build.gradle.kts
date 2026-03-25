plugins {
    kotlin("jvm") version libs.versions.kotlin

    application
}

group = "de.javaracing"
version = project.property("update_downloader.version") as String

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass = "de.javaracing.update_downloader.MainKt"
}

tasks.test {
    useJUnitPlatform()
}