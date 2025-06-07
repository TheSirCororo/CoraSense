plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.telegram.bot)
    alias(libs.plugins.shadowJar)
    idea
}

dependencies {
    implementation(projects.shared)
    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.krpc.client)
    implementation(libs.bundles.koin)
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
}
