package ru.cororo.corasense.service

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ru.cororo.corasense.shared.model.ml.MLScore
import ru.cororo.corasense.repo.ml.MLScoreRepo
import ru.cororo.corasense.shared.service.MLScoreService
import java.util.UUID
import kotlin.coroutines.CoroutineContext

class MLScoreServiceImpl(private val mlScoreRepo: MLScoreRepo) : MLScoreService, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName("MLScoreServiceImpl")

    override suspend fun getMLScore(advertiserId: UUID, clientId: UUID) = mlScoreRepo.get(advertiserId to clientId)

    override suspend fun saveMLScore(score: MLScore) = mlScoreRepo.save(score)
}