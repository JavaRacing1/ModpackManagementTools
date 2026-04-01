plugins {
    kotlin("jvm")
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
    runtimeOnly(files("src/dist/config"))
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass = "de.javaracing.modpackExporter.MainKt"
}

tasks.startScripts {
    //Replace the config path in the start scripts because Gradle assumes all classpath files are inside the lib dir (GRADLE-2991)
    doLast {
        val windowsScriptFile = file(windowsScript)
        windowsScriptFile.writeText(
            windowsScriptFile.readText().replace("%APP_HOME%\\lib\\config", "%APP_HOME%\\config")
        )
        val unixScriptFile = file(unixScript)
        unixScriptFile.writeText(
            unixScriptFile.readText().replace($$"$APP_HOME/lib/config", $$"$APP_HOME/config")
        )
    }
}

tasks.test {
    useJUnitPlatform()
}

detekt {
    config.setFrom(file("$rootDir/config/detekt.yml"))
    buildUponDefaultConfig = true
}
