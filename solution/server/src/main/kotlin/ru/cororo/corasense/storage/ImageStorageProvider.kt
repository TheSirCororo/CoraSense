package ru.cororo.corasense.storage

import kotlinx.coroutines.flow.Flow
import java.util.*

interface ImageStorageProvider {
    suspend fun uploadImage(id: UUID, fileName: String, bytesFlow: Flow<ByteArray>): String?

    suspend fun loadImage(id: UUID): ByteArray

    suspend fun deleteImage(id: UUID)
}