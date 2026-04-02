plugins {
    kotlin("jvm")

    application
}

group = "de.javaracing"
version = project.property("updateDownloader.version") as String

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
    mainClass = "de.javaracing.updateDownloader.MainKt"
}

tasks.test {
    useJUnitPlatform()
}
