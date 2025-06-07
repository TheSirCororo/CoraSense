package ru.cororo.corasense.shared.service

import kotlinx.rpc.annotations.Rpc
import ru.cororo.corasense.shared.model.ml.MLScore
import ru.cororo.corasense.shared.util.UuidString

@Rpc
interface MLScoreService {
    suspend fun getMLScore(advertiserId: UuidString, clientId: UuidString
): MLScore?

    suspend fun saveMLScore(score: MLScore)
}