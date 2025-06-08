package ru.cororo.corasense.shared.model.file

import kotlinx.serialization.Serializable

@Serializable
sealed interface FileUploadResult {
    val resultFileName: String?

    @Serializable
    data class Success(override val resultFileName: String) : FileUploadResult

    @Serializable
    data class Failure(override val resultFileName: String? = null) : FileUploadResult
}