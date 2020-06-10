package ru.il_agent.easymusic.services.api


interface ApiService {
    suspend fun search(query: String, offset: Int): SearchResult
    suspend fun getAudio(poorAudio: Audio): Audio
}

class SearchResult(val audios: List<Audio>, val hasMore: Boolean)
