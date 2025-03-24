package ru.cororo.corasense.moderation

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import ru.cororo.corasense.model.moderation.data.ModerationVerdict
import ru.cororo.corasense.repo.moderation.BlockedWordRepo

object BlacklistModerationProvider : ModerationProvider, KoinComponent, CoroutineScope {
    private val blockedWordRepo = get<BlockedWordRepo>()
    override val coroutineContext = Dispatchers.IO + CoroutineName("BlacklistModerationProvider")

    init {
        launch {
            getBlockedWords() // заполняем кэш
        }
    }

    suspend fun getBlockedWords() = blockedWordRepo.getAll()

    override suspend fun moderate(text: String): ModerationVerdict {
        val blockedWords = getBlockedWords()
        val splitText = text.trim().replace("\n", " ").split(" ")
        for (word in splitText) {
            if (word.lowercase() in blockedWords) {
                return ModerationVerdict("Слово $word не проходит фильтр модерации.", false)
            }
        }

        return ModerationVerdict.Allowed
    }

}