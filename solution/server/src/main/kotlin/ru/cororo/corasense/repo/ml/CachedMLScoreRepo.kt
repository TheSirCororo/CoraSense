package ru.cororo.corasense.repo.ml

import ru.cororo.corasense.shared.model.ml.MLScore
import ru.cororo.corasense.repo.CachedCrudRepo
import java.util.*

class CachedMLScoreRepo(backedRepo: MLScoreRepo) : CachedCrudRepo<Pair<UUID, UUID>, MLScore>(backedRepo), MLScoreRepo