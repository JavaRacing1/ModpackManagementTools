dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

include(":modpackExporter")
include(":updateDownloader")

rootProject.name = "ModpackManagementTools"

