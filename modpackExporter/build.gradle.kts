plugins {
    kotlin("jvm")
    alias(libs.plugins.shadow)
    alias(libs.plugins.detekt)

    application
}

group = "de.javaracing"
version = project.property("modpackExporter.version") as String

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
    mainClass = "de.javaracing.modpackExporter.MainKt"
}

tasks.test {
    useJUnitPlatform()
}

detekt {
    config.setFrom(file("$rootDir/config/detekt.yml"))
    buildUponDefaultConfig = true
}
