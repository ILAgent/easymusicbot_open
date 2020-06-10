package ru.il_agent.easymusic.services.api

data class Audio(
    val id: String,
    val ownerId: String,
    val artist: String,
    val title: String,
    var duration: Int,
    var url: String,
    val lyricsId: String,
    val albumId: String,
    val authorId: String,
    val hashes: String,
    val images: List<String>
)