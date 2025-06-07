package ru.cororo.corasense.shared.model.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.cororo.corasense.shared.util.UuidString

@Serializable
data class Client(
    @SerialName("client_id")
    val id: UuidString,
    val login: String,
    val age: Int,
    val location: String,
    val gender: Gender
) {
    enum class Gender(val russianName: String) {
        MALE("Мужской"),
        FEMALE("Женский")
    }
}
