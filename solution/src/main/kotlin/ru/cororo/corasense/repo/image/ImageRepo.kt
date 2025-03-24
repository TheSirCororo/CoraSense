package ru.cororo.corasense.repo.image

import ru.cororo.corasense.model.image.Image
import ru.cororo.corasense.repo.CrudRepo
import java.util.UUID

interface ImageRepo : CrudRepo<UUID, Image> {
    suspend fun createNewImage(id: UUID, name: String): Image

    suspend fun needDeleting(id: UUID): Boolean
}