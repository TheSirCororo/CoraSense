package ru.cororo.corasense.storage

import io.ktor.utils.io.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.cororo.corasense.service.ImageServiceImpl
import java.io.File
import java.util.*

class FileImageStorageProvider(private val uploadDir: File) : ImageStorageProvider {
    override suspend fun uploadImage(id: UUID, fileName: String, bytesFlow: Flow<ByteArray>): String? =
        withContext(Dispatchers.IO) {
            if (!ImageServiceImpl.allowedFileExtensions.any { fileName.endsWith(it) }) return@withContext null

            val file = getImageFile(id)
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }

            file.createNewFile()
            file.outputStream().buffered().use { output ->
                bytesFlow.collect {
                    output.asByteWriteChannel().writeByteArray(it)
                }
            }

            fileName
        }

    override suspend fun loadImage(id: UUID) = withContext(Dispatchers.IO) {
        getImageFile(id).readBytes()
    }

    override suspend fun deleteImage(id: UUID) {
        val file = getImageFile(id)
        if (file.exists()) {
            file.delete()
        }
    }

    fun getImageFile(id: UUID) = File(uploadDir, "$id.uploadfile")
}