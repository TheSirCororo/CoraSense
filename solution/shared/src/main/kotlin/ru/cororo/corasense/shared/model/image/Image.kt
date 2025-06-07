package ru.cororo.corasense.shared.model.image

import kotlinx.serialization.Serializable
import ru.cororo.corasense.shared.util.UuidString

@Serializable
data class Image(
    val id: UuidString,
    val name: String
)