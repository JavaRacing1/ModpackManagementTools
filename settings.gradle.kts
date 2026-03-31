dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

include(":modpackExporter")
include(":update_downloader")

rootProject.name = "ModpackManagementTools"

