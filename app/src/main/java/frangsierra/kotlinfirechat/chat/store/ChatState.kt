package frangsierra.kotlinfirechat.chat.store

import frangsierra.kotlinfirechat.chat.model.Message
import frangsierra.kotlinfirechat.util.Task
import io.reactivex.disposables.CompositeDisposable

data class ChatState(val messages: List<Message> = emptyList(),
                     val sendMessageTask : Task = Task(),
                     val disposables: CompositeDisposable = CompositeDisposable())