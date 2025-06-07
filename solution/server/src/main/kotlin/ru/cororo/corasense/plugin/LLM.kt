package ru.cororo.corasense.plugin

import io.ktor.server.application.*
import kotlinx.coroutines.launch
import org.koin.ktor.ext.get
import ru.cororo.corasense.service.LlmServiceImpl
import ru.cororo.corasense.shared.service.LlmService

fun Application.configureLLM() {
    get<LlmService>().apply {
        launch {
            (this@apply as LlmServiceImpl).init()
        }
    }
}