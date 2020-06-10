package ru.ilagent.easymusicbot.storage

import org.litote.kmongo.reactivestreams.*  //NEEDED! import KMongo reactivestreams extensions
import org.litote.kmongo.coroutine.* //NEEDED! import KMongo coroutine extensions
import org.litote.kmongo.eq
import org.litote.kmongo.inc
import org.litote.kmongo.set

import ru.il_agent.easymusic.services.api.Audio
import ru.ilagent.easymusicbot.MONGODB_URI
import ru.ilagent.easymusicbot.MONGO_DB_NAME
import java.lang.IllegalStateException

class MongoStorage : Storage {

    val database: CoroutineDatabase = KMongo
        .createClient(MONGODB_URI)
        .coroutine
        .getDatabase(MONGO_DB_NAME)

    override suspend fun getOrAddSession(chatId: Long): ChatSessionState {
        val collection = database.getCollection<ChatSessionState>()
        val session = collection
            .findOne(ChatSessionState::chatId eq chatId)
            ?: ChatSessionState(chatId)
                .also {
                    collection.insertOne(it)
                }
        return session
    }

    override suspend fun nextPage(chatId: Long) {
        val collection = database.getCollection<ChatSessionState>()
        collection.updateOne(
            filter = ChatSessionState::chatId eq chatId,
            update = inc(ChatSessionState::currentPage, 1)
        )
    }

    override suspend fun setAudios(chatId: Long, audios: List<Audio>) {
        val collection = database.getCollection<ChatSessionState>()
        collection.replaceOne(
            filter = ChatSessionState::chatId eq chatId,
            replacement = ChatSessionState(chatId = chatId, offeredAudios = audios)
        )
    }
}