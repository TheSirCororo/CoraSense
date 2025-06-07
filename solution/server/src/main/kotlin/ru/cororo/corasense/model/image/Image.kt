package ru.cororo.corasense.model.image

import kotlinx.serialization.Serializable
import ru.cororo.corasense.util.UuidString

@Serializable
data class Image(
    val id: UuidString,
    val name: String
)
