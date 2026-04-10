plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.shadow)
    alias(libs.plugins.detekt)

    application
}

group = "de.javaracing"
version = project.property("updateDownloader.version") as String

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(libs.kotlin.logging)
    implementation(libs.zt.zip)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    implementation(libs.hoplite.core)
    runtimeOnly(libs.hoplite.toml)

    runtimeOnly(libs.log4j.core)
    runtimeOnly(libs.log4j.slf4j2)
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass = "de.javaracing.updateDownloader.MainKt"
}

tasks.test {
    useJUnitPlatform()
}

detekt {
    config.setFrom(file("$rootDir/config/detekt.yml"))
    buildUponDefaultConfig = true
}
