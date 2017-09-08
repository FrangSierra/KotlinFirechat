package frangsierra.kotlinfirechat.chat

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import durdinapps.rxfirebase2.RxFirebaseChildEvent
import frangsierra.kotlinfirechat.common.dagger.AppScope
import frangsierra.kotlinfirechat.common.firebase.FirebaseConstants
import frangsierra.kotlinfirechat.common.firebase.FluxChildEventListener
import frangsierra.kotlinfirechat.common.firebase.Message
import frangsierra.kotlinfirechat.common.flux.Dispatcher
import javax.inject.Inject

interface ChatController {
    fun onUserLogged(state: ChatState, loggedUser: FirebaseUser): ChatState

    fun startListeningChatData(state: ChatState): ChatState

    fun stopListeningChatData(state: ChatState): ChatState

    fun onMessageDataRetrieved(state: ChatState,
                               type: RxFirebaseChildEvent.EventType,
                               message: DataSnapshot): ChatState

    fun sendMessage(state: ChatState, messageText: String): ChatState
}

@AppScope
class ChatControllerImpl @Inject constructor(val dispatcher: Dispatcher) : ChatController {
    private val chatEventListener: ChildEventListener = FluxChildEventListener("CHAT", dispatcher)

    override fun onUserLogged(state: ChatState, loggedUser: FirebaseUser): ChatState {
        return state.copy(currentUser = loggedUser)
    }

    override fun startListeningChatData(state: ChatState): ChatState {
        FirebaseConstants.MESSAGE_DATA_REFERENCE.addChildEventListener(chatEventListener)
        return state.copy(listening = true)
    }

    override fun stopListeningChatData(state: ChatState): ChatState {
        FirebaseConstants.MESSAGE_DATA_REFERENCE.removeEventListener(chatEventListener)
        return state.copy(listening = false)
    }

    override fun onMessageDataRetrieved(state: ChatState, type: RxFirebaseChildEvent.EventType, message: DataSnapshot): ChatState {
        val newMessage = message.getValue(Message::class.java)
        val newMessageMap = LinkedHashMap(state.messagesData)
            .apply {
                when (type) {
                    RxFirebaseChildEvent.EventType.ADDED -> return@apply plusAssign(message.key to newMessage)
                    RxFirebaseChildEvent.EventType.CHANGED -> return@apply plusAssign(message.key to newMessage)
                    RxFirebaseChildEvent.EventType.REMOVED -> return@apply minusAssign(message.key)
                    RxFirebaseChildEvent.EventType.MOVED -> return@apply
                }
            }
        return state.copy(messagesData = newMessageMap)
    }

    override fun sendMessage(state: ChatState, messageText: String): ChatState {
        val messageToSend = Message(messageText, state.currentUser!!.displayName)
        val newKeyForMessage = FirebaseConstants.MESSAGE_DATA_REFERENCE.push()
        FirebaseConstants.MESSAGE_DATA_REFERENCE.child(newKeyForMessage.key).setValue(messageToSend)
        return state.copy(messagesData = state.messagesData.apply { plus(newKeyForMessage to messageToSend) })
    }

}