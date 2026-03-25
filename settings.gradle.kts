dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

include(":update_assembler")
include(":update_downloader")

rootProject.name = "ModpackManagementTools"

