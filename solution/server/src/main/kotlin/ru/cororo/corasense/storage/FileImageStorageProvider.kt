package ru.cororo.corasense.storage

import io.ktor.http.content.*
import io.ktor.utils.io.jvm.javaio.*
import ru.cororo.corasense.service.ImageService
import java.io.File
import java.util.*

class FileImageStorageProvider(private val uploadDir: File) : ImageStorageProvider {
    override suspend fun uploadImage(multiPartFile: PartData.FileItem, id: UUID): String? {
        val fileName = multiPartFile.originalFileName ?: return null
        if (!ImageService.allowedFileExtensions.any { fileName.endsWith(it) }) return null

        val file = getImageFile(id)
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        file.createNewFile()
        file.outputStream().buffered().use { output -> multiPartFile.provider().copyTo(output) }
        multiPartFile.dispose()

        return fileName
    }

    override suspend fun loadImage(id: UUID) = getImageFile(id).readBytes()

    override suspend fun deleteImage(id: UUID) {
        val file = getImageFile(id)
        if (file.exists()) {
            file.delete()
        }
    }

    fun getImageFile(id: UUID) = File(uploadDir, "$id.uploadfile")
}