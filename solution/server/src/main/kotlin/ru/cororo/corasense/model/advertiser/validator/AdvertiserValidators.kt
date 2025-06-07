package ru.cororo.corasense.model.advertiser.validator

import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.minLength
import ru.cororo.corasense.shared.model.advertiser.Advertiser
import ru.cororo.corasense.validation.validator

fun advertiserValidator() = validator<Advertiser> {
    Advertiser::name {
        minLength(3)
        maxLength(128)
    }
}