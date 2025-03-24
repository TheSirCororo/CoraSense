package ru.cororo.corasense.repo.image

import ru.cororo.corasense.model.image.Image
import ru.cororo.corasense.repo.CachedCrudRepo
import java.util.UUID

class CachedImageRepo(backedRepo: ImageRepo) : CachedCrudRepo<UUID, Image>(backedRepo), ImageRepo {
    override suspend fun createNewImage(id: UUID, name: String): Image {
        val image = (backedRepo as ImageRepo).createNewImage(id, name)
        cacheById.put(image.id, image)
        return image
    }

    override suspend fun needDeleting(id: UUID): Boolean = (backedRepo as ImageRepo).needDeleting(id)
}