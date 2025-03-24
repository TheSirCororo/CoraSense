package ru.cororo.corasense.service

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import ru.cororo.corasense.repo.image.ImageRepo
import ru.cororo.corasense.storage.FileImageStorageProvider
import ru.cororo.corasense.storage.ImageStorageProvider
import ru.cororo.corasense.storage.S3ImageStorageProvider
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class ImageService(private val imageRepo: ImageRepo, application: Application, storage: ImageStorageProvider? = null) {
    private val uploadDir = File(application.environment.config.tryGetString("image.upload_dir") ?: "images")
    private val s3Endpoint = application.environment.config.tryGetString("image.s3_endpoint") ?: ""
    private val s3KeyId = application.environment.config.tryGetString("image.s3_key_id") ?: ""
    private val s3KeyValue = application.environment.config.tryGetString("image.s3_key_value") ?: ""
    private val s3Bucket = application.environment.config.tryGetString("image.s3_bucket") ?: ""
    private val storageType = application.environment.config.tryGetString("image.storage_type")?.let {
        try {
            ImageStorageType.valueOf(it.uppercase())
        } catch (_: Exception) {
            ImageStorageType.FILE
        }
    } ?: ImageStorageType.FILE
    private val storage = storage ?: when (storageType) {
        ImageStorageType.S3 -> S3ImageStorageProvider(s3Endpoint, s3KeyId, s3KeyValue, s3Bucket)
        ImageStorageType.FILE -> FileImageStorageProvider(uploadDir)
    }

    val maxImageSize = 512L * 1024L // 512 KB

    suspend fun uploadImage(multipart: MultiPartData, id: UUID): String? {
        val part = multipart.readPart() ?: return null
        val fileName: String?
        if (part is PartData.FileItem) {
            fileName = storage.uploadImage(part, id)
        } else {
            return null
        }

        part.dispose()

        return fileName
    }

    suspend fun saveImage(id: UUID, name: String) = imageRepo.createNewImage(id, name)

    suspend fun getImage(id: UUID) = imageRepo.get(id)

    suspend fun deleteImage(id: UUID) {
        try {
            storage.deleteImage(id)
        } catch (_: Exception) {}
        imageRepo.delete(id)
    }

    suspend fun loadImageBytes(id: UUID) = try {
        storage.loadImage(id)
    } catch (_: NoSuchKeyException) {
        null
    } catch (_: FileNotFoundException) {
        null
    }

    companion object {
        val allowedFileExtensions = listOf(".png", ".bmp", ".jpg", ".jpeg", ".svg", ".webp", ".gif")
    }

    enum class ImageStorageType {
        FILE, S3
    }
}
