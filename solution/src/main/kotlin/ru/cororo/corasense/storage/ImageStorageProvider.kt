package ru.cororo.corasense.storage

import io.ktor.http.content.*
import java.util.*

interface ImageStorageProvider {
    suspend fun uploadImage(multiPartFile: PartData.FileItem, id: UUID): String?

    suspend fun loadImage(id: UUID): ByteArray

    suspend fun deleteImage(id: UUID)
}