package frangsierra.kotlinfirechat.chat

import com.google.firebase.auth.FirebaseUser
import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import frangsierra.kotlinfirechat.common.dagger.AppScope
import frangsierra.kotlinfirechat.common.firebase.FirebaseChildRetrievedAction
import frangsierra.kotlinfirechat.common.firebase.Message
import frangsierra.kotlinfirechat.common.flux.Dispatcher
import frangsierra.kotlinfirechat.common.flux.OnActivityLifeCycleAction
import frangsierra.kotlinfirechat.common.flux.Store
import frangsierra.kotlinfirechat.session.LoginStatus
import frangsierra.kotlinfirechat.session.SessionStore
import javax.inject.Inject

@AppScope
class ChatStore @Inject constructor(val dispatcher: Dispatcher,
                                    val controller: ChatController,
                                    val sessionStore: SessionStore) : Store<ChatState>() {
    override fun init() {

        sessionStore.flowable()
            .filter { (status) -> status == LoginStatus.LOGGED }
            .subscribe { dispatcher.dispatchOnUi(OnUserLoggedAction(it.loggedUser!!)) }.track()

        dispatcher.subscribe(OnUserLoggedAction::class) { (loggedUser) ->
            state = controller.onUserLogged(state, loggedUser)
        }.track()

        dispatcher.subscribe(OnActivityLifeCycleAction::class)
            .flowable()
            .filter { it.activity is ChatActivity }
            .subscribe { (_, stage) ->
                when (stage) {
                    OnActivityLifeCycleAction.ActivityStage.STARTED -> state = controller.startListeningChatData(state)
                    OnActivityLifeCycleAction.ActivityStage.STOPPED -> state = controller.stopListeningChatData(state)
                }
            }.track()

        dispatcher.subscribe(FirebaseChildRetrievedAction::class) { (tag, type, message) ->
            state = controller.onMessageDataRetrieved(state, type, message)
        }.track()

        dispatcher.subscribe(SendMessageAction::class) { (messageText) ->
            state = controller.sendMessage(state, messageText)
        }.track()

    }
}

@Module
abstract class ChatModule {
    @Binds @AppScope @IntoMap @ClassKey(ChatStore::class)
    abstract fun provideChatStore(store: ChatStore): Store<*>

    @Binds @AppScope
    abstract fun provideChatController(chatControllerImpl: ChatControllerImpl): ChatController
}

data class ChatState(val currentUser: FirebaseUser? = null,
                     val messagesData: LinkedHashMap<String, Message> = linkedMapOf(),
                     val listening: Boolean = false)