package ru.cororo.corasense.test.unit

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.runs
import io.mockk.mockk
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import ru.cororo.corasense.model.ml.data.MLScore
import ru.cororo.corasense.repo.ml.MLScoreRepo
import ru.cororo.corasense.service.MLScoreService
import java.util.UUID

// unit тесты написаны при помощи чата гпт
class MLScoreServiceTests : StringSpec({
    val mlScoreRepo = mockk<MLScoreRepo>()
    val mlScoreService = MLScoreService(mlScoreRepo)

    "should return MLScore for given advertiser and client" {
        val advertiserId = UUID.randomUUID()
        val clientId = UUID.randomUUID()
        val expectedScore = MLScore(advertiserId, clientId, 85)

        coEvery { mlScoreRepo.get(advertiserId to clientId) } returns expectedScore

        val result = mlScoreService.getMLScore(advertiserId, clientId)

        result shouldBe expectedScore
        coVerify { mlScoreRepo.get(advertiserId to clientId) }
    }

    "should save MLScore" {
        val score = MLScore(UUID.randomUUID(), UUID.randomUUID(), 9)

        coEvery { mlScoreRepo.save(score) } just runs

        mlScoreService.saveMLScore(score)

        coVerify { mlScoreRepo.save(score) }
    }
})
