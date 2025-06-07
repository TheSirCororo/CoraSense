plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    idea
}

subprojects {
    group = "ru.cororo"
    version = "1.0"

    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "kotlin")
    apply(plugin = "idea")

    repositories {
        mavenCentral()
    }

    idea {
        module {
            isDownloadSources = true
        }
    }
}