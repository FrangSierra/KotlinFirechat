package frangsierra.kotlinfirechat.chat

import android.net.Uri
import com.google.firebase.auth.FirebaseUser
import durdinapps.rxfirebase2.RxFirebaseChildEvent
import frangsierra.kotlinfirechat.common.firebase.FirebaseConstants
import frangsierra.kotlinfirechat.common.firebase.FirebaseMockModels
import frangsierra.kotlinfirechat.common.firebase.Message
import frangsierra.kotlinfirechat.common.flux.Dispatcher
import frangsierra.kotlinfirechat.common.flux.app
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChatControllerFake @Inject constructor() : ChatController {

    private lateinit var disposable: Disposable

    private val dispatcher: Dispatcher = app.component.dispatcher()

    override fun onUserLogged(state: ChatState, loggedUser: FirebaseUser): ChatState {
        return state.copy(currentUser = loggedUser)
    }

    override fun startListeningChatData(state: ChatState): ChatState {
        var tick = 0
        disposable = Flowable.interval(1, TimeUnit.SECONDS)
            .map { tick++ }
            .take(5)
            .subscribe {
                val newMessage = FirebaseMockModels.mockMessageRetrieved
                when (tick) {
                    1 -> dispatcher.dispatchOnUi(MessageChildRetrievedAction(RxFirebaseChildEvent.EventType.ADDED, newMessage))
                    2 -> dispatcher.dispatchOnUi(MessageChildRetrievedAction(RxFirebaseChildEvent.EventType.ADDED, newMessage))
                    3 -> dispatcher.dispatchOnUi(MessageChildRetrievedAction(RxFirebaseChildEvent.EventType.REMOVED, newMessage))
                    4 -> dispatcher.dispatchOnUi(MessageChildRetrievedAction(RxFirebaseChildEvent.EventType.ADDED, newMessage))
                    5 -> dispatcher.dispatchOnUi(MessageChildRetrievedAction(RxFirebaseChildEvent.EventType.REMOVED, newMessage))
                }
            }
        return state.copy(listening = true)
    }

    override fun onMessageDataRetrieved(state: ChatState, type: RxFirebaseChildEvent.EventType, message: Pair<String, Message>): ChatState {
        val newMessageMap = LinkedHashMap(state.messagesData)
            .apply {
                when (type) {
                    RxFirebaseChildEvent.EventType.ADDED -> return@apply plusAssign(message)
                    RxFirebaseChildEvent.EventType.CHANGED -> return@apply plusAssign(message)
                    RxFirebaseChildEvent.EventType.REMOVED -> return@apply minusAssign(message.first)
                    RxFirebaseChildEvent.EventType.MOVED -> return@apply
                }
            }
        return state.copy(messagesData = newMessageMap)
    }

    override fun sendMessage(state: ChatState, messageText: String, url: Uri?): ChatState {
        val messageToSend = Message(messageText, state.currentUser!!.displayName)
        val newKeyForMessage = FirebaseConstants.MESSAGE_DATA_REFERENCE.push()
        dispatcher.dispatchOnUi(MessageChildRetrievedAction(RxFirebaseChildEvent.EventType.ADDED, newKeyForMessage.key to messageToSend))
        return state.copy(messagesData = state.messagesData.apply { plus(newKeyForMessage to messageToSend) })
    }

}