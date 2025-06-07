import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.ksp)
    alias(libs.plugins.telegram.bot)
    idea
    jacoco
}

dependencies {
    implementation(libs.bundles.swagger)
    implementation(libs.bundles.database)
    implementation(libs.ktor.serialization)
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.client)
    implementation(libs.micrometer.prometheus)
    implementation(libs.logback)
    implementation(libs.bundles.koin)
    implementation(libs.konform)
    implementation(libs.aedile.core)

    implementation(platform(libs.aws.sdk.bom))
    implementation(libs.aws.s3) {
        exclude("software.amazon.awssdk:netty-nio-client")
        exclude("software.amazon.awssdk:apache-client")
    }
    implementation(libs.aws.sso)
    implementation(libs.aws.ssooidc)
    implementation(libs.aws.apache.client) {
        exclude("commons-logging:commons-logging")
    }

    testImplementation(libs.bundles.test.server)
}

application {
    mainClass.set("ru.cororo.corasense.CoraSenseApplicationKt")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }

    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
        onlyIf {
            System.getProperty("skipTests") == null
        }

        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required = false
            csv.required = false
            html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
        }

        classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    exclude("ru/cororo/corasense/service/ModerationService*")
                    exclude("ru/cororo/corasense/service/PrometheusMicrometerService*")
                    exclude("ru/cororo/corasense/service/TelegramBotService*")
                    exclude("ru/cororo/corasense/telegram/*")
                    exclude("eu/vendeli/tgbot/generated/*")
                }
            })
        )
    }
}