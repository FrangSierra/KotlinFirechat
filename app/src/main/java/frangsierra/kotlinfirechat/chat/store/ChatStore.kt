package frangsierra.kotlinfirechat.chat.store

import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import frangsierra.kotlinfirechat.chat.controller.ChatController
import frangsierra.kotlinfirechat.chat.controller.ChatControllerImpl
import frangsierra.kotlinfirechat.core.dagger.AppScope
import frangsierra.kotlinfirechat.session.store.SignOutAction
import frangsierra.kotlinfirechat.util.taskRunning
import io.reactivex.disposables.CompositeDisposable
import mini.Reducer
import mini.Store
import javax.inject.Inject

@AppScope
class ChatStore @Inject constructor(val controller: ChatController) : Store<ChatState>() {

    @Reducer
    fun loadMessages(action: StartListeningChatMessagesAction, state: ChatState): ChatState {
        controller.startListeningMessages()
        return state
    }

    @Reducer
    fun messagesReceived(action: MessagesLoadedAction, state: ChatState): ChatState {
        return state.copy(messages = state.messages.plus(action.messages).distinctBy { it.uid })
    }

    @Reducer
    fun sendMessage(action: SendMessageAction, state: ChatState): ChatState {
        controller.sendMessage(action.message)
        return state.copy(sendMessageTask = taskRunning())
    }

    @Reducer
    fun messageSent(action: SendMessageCompleteAction, state: ChatState): ChatState {
        return state.copy(sendMessageTask = action.task, messages = if (action.task.isSuccessful()) state.messages.plus(action.message!!) else state.messages)
    }

    @Reducer
    fun stopListeningMessages(action: StopListeningChatMessagesAction, state: ChatState): ChatState {
        state.disposables.dispose()
        return state.copy(disposables = CompositeDisposable())
    }

    @Reducer
    fun signOut(action: SignOutAction, state: ChatState): ChatState {
        return initialState()
    }
}

@Module
abstract class ChatModule {
    @Binds
    @AppScope
    @IntoMap
    @ClassKey(ChatStore::class)
    abstract fun provideChatStore(store: ChatStore): Store<*>

    @Binds
    @AppScope
    abstract fun bindChatController(impl: ChatControllerImpl): ChatController
}
