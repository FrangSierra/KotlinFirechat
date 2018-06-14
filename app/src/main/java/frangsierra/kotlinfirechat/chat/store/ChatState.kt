package frangsierra.kotlinfirechat.chat.store

import frangsierra.kotlinfirechat.chat.model.Message
import frangsierra.kotlinfirechat.util.Task
import io.reactivex.disposables.CompositeDisposable

data class ChatState(val messages: Map<String, Message> = emptyMap(),
                     val sendMessageTask : Task = Task(),
                     val disposables: CompositeDisposable = CompositeDisposable())