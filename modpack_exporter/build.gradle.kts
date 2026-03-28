plugins {
    kotlin("jvm") version libs.versions.kotlin

    application
}

group = "de.javaracing"
version = project.property("modpack_exporter.version") as String

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation(libs.jgit)
    implementation(libs.kotlin.logging)
    implementation(libs.zt.zip)

    implementation(libs.hoplite.core)
    runtimeOnly(libs.hoplite.toml)

    runtimeOnly(libs.log4j.core)
    runtimeOnly(libs.log4j.slf4j2)
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass = "de.javaracing.modpack_exporter.MainKt"
}

tasks.test {
    useJUnitPlatform()
}