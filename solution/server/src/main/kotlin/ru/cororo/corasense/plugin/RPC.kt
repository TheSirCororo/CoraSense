package ru.cororo.corasense.plugin

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.routing.*
import kotlinx.rpc.krpc.ktor.server.Krpc
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.serialization.json.json
import org.koin.core.component.get
import ru.cororo.corasense.inject.api
import ru.cororo.corasense.shared.service.*

fun Application.configureRpc() {
    install(Krpc)

    routing {
        rpc("/rpc") {
            log.info("Starting RPC...")
            rpcConfig {
                serialization {
                    json()
                }
            }

            api { api -> // api блок нужен, посколько в koin-ktor есть непофикшенная проблема конфликта классов при исп. extension
                registerService<ClientService> { api.get<ClientService>() }
                registerService<AdvertiserService> { api.get<AdvertiserService>() }
                registerService<CurrentDayService> { api.get<CurrentDayService>() }
                registerService<CampaignService> { api.get<CampaignService>() }
                registerService<MLScoreService> { api.get<MLScoreService>() }
                registerService<LlmService> { api.get<LlmService>() }
                registerService<ModerationService> { api.get<ModerationService>() }
                registerService<ImageService> { api.get<ImageService>() }
                registerService<AdActionService> { api.get<AdActionService>() }
            }

            log.info("Started RPC server")
        }
    }
}
