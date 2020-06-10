package ru.ilagent.easymusicbot.storage

import ru.il_agent.easymusic.services.api.Audio

interface Storage {
    suspend fun getOrAddSession(chatId: Long): ChatSessionState
    suspend fun nextPage(chatId: Long)
    suspend fun setAudios(chatId: Long, audios: List<Audio>)
}

data class ChatSessionState(
    val chatId: Long,
    val offeredAudios: List<Audio> = emptyList(),
    val currentPage: Int = 0
)

fun ChatSessionState.hasNextPage(): Boolean =
    (currentPage + 1) * PAGE_SIZE < offeredAudios.size

const val PAGE_SIZE = 10