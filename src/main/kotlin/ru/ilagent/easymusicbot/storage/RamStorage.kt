package ru.ilagent.easymusicbot.storage

import ru.il_agent.easymusic.services.api.Audio
import java.lang.IllegalStateException

class RamStorage : Storage {

    private val chatSessionStates = mutableMapOf<Long, ChatSessionState>()

    override suspend fun getOrAddSession(chatId: Long): ChatSessionState {
        return chatSessionStates[chatId] ?: ChatSessionState(chatId).also {
            chatSessionStates[chatId] = it
        }
    }

    override suspend fun nextPage(chatId: Long) {
        val sessionState = getOrAddSession(chatId)
        if (!sessionState.hasNextPage()) {
            throw IllegalStateException("There is no the next page")
        }
        val nextPage = sessionState.copy(currentPage = sessionState.currentPage + 1)
        chatSessionStates[chatId] = nextPage
    }

    override suspend fun setAudios(chatId: Long, audios: List<Audio>) {
        val sessionState = ChatSessionState(chatId, audios, 0)
        chatSessionStates[chatId] = sessionState
    }
}