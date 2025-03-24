package ru.cororo.corasense.model.client.data

import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.maximum
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.minimum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.cororo.corasense.util.UuidString
import ru.cororo.corasense.validation.validator

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

    companion object {
        init {
            validator<Client> {
                Client::login {
                    minLength(3)
                    maxLength(128)
                }

                Client::age {
                    minimum(0)
                    maximum(130)
                }

                Client::location {
                    minLength(3)
                    maxLength(128)
                }
            }
        }
    }
}
