package ru.ilagent.easymusicbot

interface ChatSessionController {
    suspend fun handleRequest(request: String)

    suspend fun showNextPage()

    suspend fun handleSongRequest(audioIndex: Int)

    fun sendHelloMessage()
}