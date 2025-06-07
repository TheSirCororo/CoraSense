package ru.cororo.corasense.test.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import io.ktor.test.dispatcher.*
import kotlinx.serialization.json.Json
import ru.cororo.corasense.model.moderation.data.ModerationVerdict
import ru.cororo.corasense.service.LlmService

class LlmServiceTests : StringSpec({
    val json = Json

    "should return true when LLM is active" {
        testApplication {
            environment {
                config = MapApplicationConfig(
                    "llm.enabled" to "true"
                )
            }

            application {
                val mockClient = mockHttpClient(json.encodeToString(LlmService.GroqResponse(listOf(LlmService.GroqResponse.Choice(
                    LlmService.GroqRequest.Message("user", "Привет!")
                )))))
                val llmService = LlmService(this, mockClient)
                testSuspend {
                    llmService.init()
                    llmService.isActive() shouldBe true
                }
            }
        }
    }

    "should return moderation verdict" {
        testApplication {
            environment {
                config = MapApplicationConfig(
                    "llm.enabled" to "true"
                )
            }

            application {
                val mockClient = mockHttpClient(json.encodeToString(LlmService.GroqResponse(listOf(LlmService.GroqResponse.Choice(
                    LlmService.GroqRequest.Message("user", """{"reason": "test reason", "allowed": false}"""))))))
                val llmService = LlmService(this, mockClient)

                testSuspend {
                    val verdict = llmService.moderateText("some text")
                    verdict shouldBe ModerationVerdict("test reason", allowed = false)
                }
            }
        }
    }

    "should generate campaign text" {
        testApplication {
            environment {
                config = MapApplicationConfig(
                    "llm.enabled" to "true"
                )
            }

            application {
                val mockClient = mockHttpClient(json.encodeToString(LlmService.GroqResponse(listOf(LlmService.GroqResponse.Choice(
                    LlmService.GroqRequest.Message("user", """{"text": "Awesome campaign text"}"""))))))
                val llmService = LlmService(this, mockClient)

                testSuspend {
                    val text = llmService.generateCampaignText("Super Sale", "BestAds")
                    text shouldBe "Awesome campaign text"
                }
            }
        }
    }
})

fun mockHttpClient(responseBody: String = "{}") = HttpClient(MockEngine) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    engine {
        addHandler { request ->
            respond(
                content = responseBody,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
    }
}
