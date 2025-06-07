package ru.cororo.corasense.model.time.dto

import io.konform.validation.constraints.minimum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.cororo.corasense.validation.validator

@Serializable
data class TimeUpdateRequest(
    @SerialName("current_date")
    val currentDate: Int
) {
    companion object {
        init {
            validator<TimeUpdateRequest> {
                TimeUpdateRequest::currentDate {
                    minimum(0)
                }
            }
        }
    }
}