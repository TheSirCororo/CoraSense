package ru.cororo.corasense.shared.service

import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc
import ru.cororo.corasense.shared.model.file.FileUploadResult
import ru.cororo.corasense.shared.model.image.Image
import ru.cororo.corasense.shared.util.UuidString

@Rpc
interface ImageService : RemoteService {
    suspend fun maxImageSize(): Long

    fun uploadImage(id: UuidString, fileName: String, bytesFlow: Flow<ByteArray>): Flow<FileUploadResult>

    suspend fun saveImageData(id: UuidString, name: String): Image

    suspend fun getImageData(id: UuidString): Image?

    suspend fun deleteImage(id: UuidString)

    fun loadImageBytes(id: UuidString): Flow<ByteArray>
}