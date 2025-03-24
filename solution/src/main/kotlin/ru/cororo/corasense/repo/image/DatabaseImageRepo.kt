package ru.cororo.corasense.repo.image

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import ru.cororo.corasense.model.image.Image
import ru.cororo.corasense.plugin.sql
import ru.cororo.corasense.repo.campaign.CampaignTable
import ru.cororo.corasense.util.deleteById
import ru.cororo.corasense.util.getById
import java.util.*

object DatabaseImageRepo : ImageRepo {
    override suspend fun createNewImage(id: UUID, name: String): Image = sql {
        ImageTable.insert {
            it[this.id] = id
            it[this.name] = name
        }

        Image(
            id,
            name
        )
    }

    override suspend fun Image.getId(): UUID = id

    override suspend fun save(entity: Image): Unit = sql {
        ImageTable.update(where = { ImageTable.id eq entity.id }) {
            it[name] = entity.name
        }
    }

    override suspend fun saveAll(entities: Iterable<Image>) = sql {
        entities.forEach {
            save(it)
        }
    }

    override suspend fun delete(id: UUID): Unit = sql {
        ImageTable.deleteById(id)
    }

    override suspend fun get(id: UUID): Image? = sql {
        ImageTable.getById(id)?.let { Image(it[ImageTable.id].value, it[ImageTable.name]) }
    }

    override suspend fun needDeleting(id: UUID): Boolean = sql {
        CampaignTable.select(CampaignTable.id).where { CampaignTable.imageId eq id }.count() == 0L
    }
}