package frangsierra.kotlinfirechat.chat

import com.google.firebase.auth.FirebaseUser
import dagger.Module
import dagger.Provides
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import frangsierra.kotlinfirechat.common.dagger.AppScope
import frangsierra.kotlinfirechat.common.firebase.FirebaseMockModels
import frangsierra.kotlinfirechat.common.firebase.Message
import frangsierra.kotlinfirechat.common.flux.Dispatcher
import frangsierra.kotlinfirechat.common.flux.OnActivityLifeCycleAction
import frangsierra.kotlinfirechat.common.flux.Store
import frangsierra.kotlinfirechat.session.LoginStatus
import frangsierra.kotlinfirechat.session.SessionStore
import frangsierra.kotlinfirechat.session.SignOutAction
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@AppScope
class ChatStore @Inject constructor(val dispatcher: Dispatcher,
                                    val controller: ChatController,
                                    val sessionStore: SessionStore) : Store<ChatState>() {
    override fun init() {

        dispatcher.subscribe(dispatcher.HIGH_PRIORITY, SignOutAction::class) {
            state.dataDisposables.clear(); state = initialState()
        }.track()

        sessionStore.flowable()
            .filter { (status) -> status == LoginStatus.LOGGED }
            .subscribe { dispatcher.dispatchOnUi(OnUserLoggedAction(it.loggedUser!!)) }
            .track()

        dispatcher.subscribe(OnUserLoggedAction::class) { (loggedUser) ->
            state = controller.onUserLogged(state, loggedUser)
        }.track()

        dispatcher.subscribe(OnActivityLifeCycleAction::class)
            .flowable()
            .filter { it.activity is ChatActivity }
            .subscribe { (_, stage) ->
                when (stage) {
                    OnActivityLifeCycleAction.ActivityStage.STARTED -> state = controller.startListeningChatData(state)
                }
            }.track()

        dispatcher.subscribe(MessageChildRetrievedAction::class) { (type, data) ->
            state = controller.onMessageDataRetrieved(state, type, data)
        }.track()

        dispatcher.subscribe(SendMessageAction::class) { (messageText) ->
            state = controller.sendMessage(state, messageText)
        }.track()
    }

    override fun cancelSubscriptions() {
        state.dataDisposables.clear()
        super.cancelSubscriptions()
    }
}


@Module
class ChatModule {
    @Provides
    @AppScope
    @IntoMap
    @ClassKey(ChatStore::class)
    fun provideChatStore(store: ChatStore): Store<*> = store

    @Provides
    @AppScope
    fun provideChatController(impl: ChatControllerImpl,
                                 fake: ChatControllerFake): ChatController =
        if (FirebaseMockModels.USE_FIREBASE_MOCK) fake else impl

}

data class ChatState(val currentUser: FirebaseUser? = null,
                     val messagesData: LinkedHashMap<String, Message> = linkedMapOf(),
                     val listening: Boolean = false,
                     val dataDisposables: CompositeDisposable = CompositeDisposable())