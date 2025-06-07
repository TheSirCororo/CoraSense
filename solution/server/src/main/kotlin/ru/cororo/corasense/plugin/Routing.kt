package ru.cororo.corasense.plugin

import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import ru.cororo.corasense.route.advertiser.advertiserApi
import ru.cororo.corasense.route.advertiser.campaignApi
import ru.cororo.corasense.route.advertiser.clientAdsApi
import ru.cororo.corasense.route.moderation.blockedWordsApi
import ru.cororo.corasense.route.client.clientApi
import ru.cororo.corasense.route.image.imageApi
import ru.cororo.corasense.route.llm.llmApi
import ru.cororo.corasense.route.moderation.moderationManagementApi
import ru.cororo.corasense.route.ping
import ru.cororo.corasense.route.stats.statsApi
import ru.cororo.corasense.route.time.timeApi

fun Application.configureRouting() {
    install(Resources)

    routing {
        ping()
        clientApi()
        advertiserApi()
        campaignApi()
        clientAdsApi()
        statsApi()
        timeApi()
        imageApi()
        llmApi()
        blockedWordsApi()
        moderationManagementApi()
    }
}