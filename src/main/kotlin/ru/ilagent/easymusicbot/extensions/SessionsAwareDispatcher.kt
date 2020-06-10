package ru.ilagent.easymusicbot.extensions

import me.ivmg.telegram.Bot
import me.ivmg.telegram.HandleUpdate
import me.ivmg.telegram.dispatcher.Dispatcher
import me.ivmg.telegram.dispatcher.handlers.CallbackQueryHandler
import me.ivmg.telegram.dispatcher.handlers.CommandHandler
import me.ivmg.telegram.dispatcher.handlers.TextHandler
import me.ivmg.telegram.entities.Update
import ru.il_agent.easymusic.services.api.ApiService
import ru.il_agent.easymusic.services.api.ApiServiceImpl3
import ru.ilagent.easymusicbot.ChatSessionController
import ru.ilagent.easymusicbot.ChatSessionControllerImpl
import ru.ilagent.easymusicbot.Strings
import ru.ilagent.easymusicbot.storage.MongoStorage
import ru.ilagent.easymusicbot.storage.Storage

class SessionsAwareDispatcher(private val dispatcher: Dispatcher) {

    private val api: ApiService = ApiServiceImpl3()

    private val storage: Storage = MongoStorage()

    private val chatSessions = mutableMapOf<Long, ChatSessionControllerImpl>()

    private fun Update.strings(): Strings {
        val lang = message?.from?.languageCode
            ?: callbackQuery?.message?.from?.languageCode
        return Strings(lang)
    }

    private val Update.chatId: Long?
        get() = message?.chat?.id
            ?: callbackQuery?.message?.chat?.id

    private fun Bot.session(chatId: Long, strings: Strings): ChatSessionControllerImpl =
        chatSessions[chatId] ?: ChatSessionControllerImpl(chatId, this, api, strings, storage).also {
            chatSessions[chatId] = it
        }

    fun commandController(command: String, handler: suspend ChatSessionController.(String) -> Unit) {
        val body: HandleUpdate = { bot, update ->
            update.chatId?.let { chatId ->
                val sessionController = bot.session(chatId, update.strings())
                update.message?.text
                    ?.trimMargin("/$command")
                    ?.trimStart()
                    ?.let { text ->
                        sessionController.perform {
                            handler(text)
                        }
                    }
            }
        }
        dispatcher.addHandler(CommandHandler(command, body))
    }

    fun textController(handler: suspend ChatSessionController.(String) -> Unit) {
        val body: HandleUpdate = { bot, update ->
            update.chatId?.let { chatId ->
                val sessionController = bot.session(chatId, update.strings())
                update.message?.text
                    ?.let { text ->
                        sessionController.perform {
                            handler(text)
                        }
                    }
            }
        }
        dispatcher.addHandler(TextHandler(null, body))
    }

    fun callbackController(data: String, handler: suspend ChatSessionController.() -> Unit) {
        val body: HandleUpdate = { bot, update ->
            update.chatId?.let { chatId ->
                val sessionController = bot.session(chatId, update.strings())
                sessionController.perform {
                    handler()
                }
            }
        }
        dispatcher.addHandler(CallbackQueryHandler(callbackData = data, handler = body))
    }
}