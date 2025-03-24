package ru.cororo.corasense.model.advertiser.data

import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.minLength
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.cororo.corasense.util.UuidString
import ru.cororo.corasense.validation.validator

@Serializable
data class Advertiser(
    @SerialName("advertiser_id")
    val id: UuidString,
    val name: String
) {
    companion object {
        init {
            validator<Advertiser> {
                Advertiser::name {
                    minLength(3)
                    maxLength(128)
                }
            }
        }
    }
}