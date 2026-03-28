dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

include(":modpack_exporter")
include(":update_downloader")

rootProject.name = "ModpackManagementTools"

