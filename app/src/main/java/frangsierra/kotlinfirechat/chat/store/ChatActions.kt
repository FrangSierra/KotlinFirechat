package frangsierra.kotlinfirechat.chat.store

import android.net.Uri
import frangsierra.kotlinfirechat.chat.model.Message
import frangsierra.kotlinfirechat.util.Task
import io.reactivex.disposables.Disposable
import mini.Action

class StartListeningChatMessagesAction : Action

data class ListeningChatMessagesCompleteAction(val disposable: Disposable) : Action

class MessagesLoadedAction(val messages: List<Message>) : Action

class StopListeningChatMessagesAction : Action

data class SendMessageAction(val message: String, val attachedImageUri : Uri? = null) : Action

data class SendMessageCompleteAction(val message : Message?, val task: Task) : Action