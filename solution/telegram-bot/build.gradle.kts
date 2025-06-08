plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.telegram.bot)
    alias(libs.plugins.shadowJar)
    alias(libs.plugins.krpc)
    application
    idea
}

dependencies {
    implementation(projects.shared)
    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.krpc.client)
    implementation(libs.bundles.koin)
}

application {
    mainClass.set("ru.cororo.corasense.telegram.TelegramBotAppKt")
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    listOf(distTar, distZip, shadowDistTar, shadowDistZip).forEach {
        it {
            enabled = false
        }
    }
}
