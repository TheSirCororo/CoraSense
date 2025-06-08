package ru.cororo.corasense.service

import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.cororo.corasense.repo.image.ImageRepo
import ru.cororo.corasense.shared.model.file.FileUploadResult
import ru.cororo.corasense.shared.service.ImageService
import ru.cororo.corasense.storage.FileImageStorageProvider
import ru.cororo.corasense.storage.ImageStorageProvider
import ru.cororo.corasense.storage.S3ImageStorageProvider
import java.io.File
import java.util.*
import kotlin.coroutines.CoroutineContext

class ImageServiceImpl(
    private val imageRepo: ImageRepo,
    application: Application,
    storage: ImageStorageProvider? = null
) : ImageService {

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName("ImageServiceImpl")

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

    override suspend fun maxImageSize(): Long = maxImageSize

    override fun uploadImage(id: UUID, fileName: String, bytesFlow: Flow<ByteArray>): Flow<FileUploadResult> = flow {
        val fileName = storage.uploadImage(id, fileName, bytesFlow)
        val result = fileName?.let { FileUploadResult.Success(it) } ?: FileUploadResult.Failure()
        emit(result)
    }

    override suspend fun saveImageData(id: UUID, name: String) = imageRepo.createNewImage(id, name)

    override suspend fun getImageData(id: UUID) = imageRepo.get(id)

    override suspend fun deleteImage(id: UUID) {
        try {
            storage.deleteImage(id)
        } catch (_: Exception) {
        }
        imageRepo.delete(id)
    }

    override fun loadImageBytes(id: UUID): Flow<ByteArray> =
        flow {
            emit(storage.loadImage(id))
        }

    companion object {
        val allowedFileExtensions = listOf(".png", ".bmp", ".jpg", ".jpeg", ".svg", ".webp", ".gif")
    }

    enum class ImageStorageType {
        FILE, S3
    }
}
