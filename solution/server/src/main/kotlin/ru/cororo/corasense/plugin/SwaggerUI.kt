package ru.cororo.corasense.plugin

import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.SchemaGenerator
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureSwagger() {
    install(OpenApi) {
        info {
            title = "CoraSense API"
            version = "latest"
            description = "Агрегатор рекламных объявлений."
        }

        server {
            url = "http://localhost:8080"
            description = "Server"
        }

        schemas {
            generator = SchemaGenerator.reflection {
                overwrite(SchemaGenerator.TypeOverwrites.JavaUuid())
            }
        }
    }

    routing {
        route("/openapi/api.json") {
            openApi()
        }

        route("/swagger") {
            swaggerUI("/openapi/api.json")
        }
    }
}