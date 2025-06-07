package ru.cororo.corasense.plugin

import io.ktor.server.application.*
import kotlinx.coroutines.launch
import org.koin.ktor.ext.get
import ru.cororo.corasense.service.LlmService

fun Application.configureLLM() {
    get<LlmService>().apply {
        launch {
            init()
        }
    }
}