package ru.cororo.corasense.repo.ml

import ru.cororo.corasense.model.ml.data.MLScore
import ru.cororo.corasense.repo.CrudRepo
import java.util.UUID

interface MLScoreRepo : CrudRepo<Pair<UUID, UUID>, MLScore>