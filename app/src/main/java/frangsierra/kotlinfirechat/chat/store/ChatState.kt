package frangsierra.kotlinfirechat.chat.store

import frangsierra.kotlinfirechat.chat.model.Message
import mini.Task
import io.reactivex.disposables.CompositeDisposable
import mini.taskIdle

data class ChatState(val messages: Map<String, Message> = emptyMap(),
                     val sendMessageTask : Task = taskIdle(),
                     val disposables: CompositeDisposable = CompositeDisposable())