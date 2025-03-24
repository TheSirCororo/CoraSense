package ru.cororo.corasense.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.cororo.corasense.model.moderation.data.ModerationVerdict
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

class LlmService(private val application: Application, client: HttpClient? = null) : CoroutineScope {
    private val groqKey = application.environment.config.tryGetString("llm.key") ?: ""
    private var llmEnabled =
        application.environment.config.tryGetString("llm.enabled")?.toBooleanStrictOrNull() == true
    private val groqBaseUrl =
        application.environment.config.tryGetString("llm.base_url") ?: ""
    private val groqModel =
        application.environment.config.tryGetString("llm.model_id") ?: "gemma2-9b-it"
    internal val client = client ?: HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }

    override val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineName("LLM Service")

    suspend fun init() {
        if (llmEnabled) {
            var successful = false
            for (i in 0..<5) {
                if (test()) {
                    successful = true
                    break
                }

                application.log.warn("#${i + 1} Не удалось совершить запрос к LLM. Повторяем попытку через 1 секунду.")
                delay(1.seconds)
            }

            if (!successful) {
                application.log.warn("Не удалось подключиться к LLM за 5 попыток. Отключаем LLM.")
                llmEnabled = false
            }
        }
    }

    private suspend fun test(): Boolean {
        return try {
            prompt("Привет! Это тестовое сообщение, ответь на него просто Привет.")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isActive() = llmEnabled

    suspend fun prompt(text: String) = this@LlmService.client.post(groqBaseUrl) {
        setBody(GroqRequest(groqModel, listOf(GroqRequest.Message(content = text))))
        bearerAuth(groqKey)
        contentType(ContentType.Application.Json)
    }.also {
        if (!it.status.isSuccess()) {
            application.log.warn("Got status ${it.status}. Body is: ${it.bodyAsText()}")
        }
    }.body<GroqResponse>().choices.first().message.content

    suspend fun moderateText(text: String) =
        prompt("$MODERATION_TEXT_PROMPT\nФраза: $text").let {
            Json.decodeFromString<ModerationVerdict>(it)
        }

    suspend fun generateCampaignText(campaignTitle: String, advertiserName: String) =
        prompt("$GENERATE_CAMPAIGN_TEXT_PROMPT\nНазвание объявления: $campaignTitle\nИмя рекламодателя: $advertiserName").let {
            Json.decodeFromString<CampaignTextResponse>(it).text
        }

    companion object {
        private const val MODERATION_TEXT_PROMPT = """
            Ты модератор оскорбительных и грубых фраз в рекламах. Модерируй приведённую ниже фразу и выдавай ответ в таком формате:

{
    "reason": "Причина триггера на плохую фразу (null если всё хорошо)",
    "allowed": true/false
}
            Пожалуйста, возвращай текст без сопровождающих кавычек (`) и без надписи json, но в формате валидного json.
        """

        private const val GENERATE_CAMPAIGN_TEXT_PROMPT = """
            Ты - PR менеджер компании. Твоя задача - придумать текст для рекламного объявления, основываясь на названии объявления и имени рекламодателя. Текст не должен содержать ссылок или указателей на ссылок типа [Ссылка], также текст не должен содержать переносов строк. Придумай текст и верни его в таком формате:
{
    "text": "Текст объявления"
}
            Пожалуйста, возвращай текст без сопровождающих кавычек (`) и без надписи json, но в формате валидного json.
        """
    }

    @Serializable
    data class CampaignTextResponse(
        val text: String
    )

    @Serializable
    data class GroqRequest(
        val model: String,
        val messages: List<Message>
    ) {
        @Serializable
        data class Message(
            val role: String = "user",
            val content: String
        )
    }

    @Serializable
    data class GroqResponse(
        val choices: List<Choice>
    ) {
        @Serializable
        data class Choice(
            val message: GroqRequest.Message
        )
    }
}