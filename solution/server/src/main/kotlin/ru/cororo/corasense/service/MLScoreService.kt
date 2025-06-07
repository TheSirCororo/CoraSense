package ru.cororo.corasense.service

import ru.cororo.corasense.model.ml.data.MLScore
import ru.cororo.corasense.repo.ml.MLScoreRepo
import java.util.UUID

class MLScoreService(private val mlScoreRepo: MLScoreRepo) {
    suspend fun getMLScore(advertiserId: UUID, clientId: UUID) = mlScoreRepo.get(advertiserId to clientId)

    suspend fun saveMLScore(score: MLScore) = mlScoreRepo.save(score)
}