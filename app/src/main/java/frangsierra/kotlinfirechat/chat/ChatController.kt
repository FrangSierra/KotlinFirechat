package frangsierra.kotlinfirechat.chat

import android.net.Uri
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import durdinapps.rxfirebase2.RxFirebaseChildEvent
import durdinapps.rxfirebase2.RxFirebaseChildEvent.EventType.*
import durdinapps.rxfirebase2.RxFirebaseDatabase
import durdinapps.rxfirebase2.RxFirebaseStorage
import frangsierra.kotlinfirechat.common.dagger.AppScope
import frangsierra.kotlinfirechat.common.firebase.FirebaseConstants
import frangsierra.kotlinfirechat.common.firebase.Message
import frangsierra.kotlinfirechat.common.flux.Dispatcher
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

interface ChatController {

    /**
     * Update the user data when the user is correctly logged.
     */
    fun onUserLogged(state: ChatState, loggedUser: FirebaseUser): ChatState

    /**
     * Start listening the live changes on the database for the chat reference.
     */
    fun startListeningChatData(state: ChatState): ChatState

    /**
     * Manages the state when a new message is retrieved.
     */
    fun onMessageDataRetrieved(state: ChatState,
                               type: RxFirebaseChildEvent.EventType,
                               message: Pair<String, Message>): ChatState

    /**
     * Upload the offline and cloud data with a new message, and if needed, upload an attached image.
     */
    fun sendMessage(state: ChatState, messageText: String, url: Uri?): ChatState
}

@AppScope
class ChatControllerImpl @Inject constructor(val dispatcher: Dispatcher) : ChatController {
    val firebaseStorage = FirebaseStorage.getInstance().reference

    override fun onUserLogged(state: ChatState, loggedUser: FirebaseUser): ChatState {
        return state.copy(currentUser = loggedUser)
    }

    override fun startListeningChatData(state: ChatState): ChatState {
        val newDisposables = state.dataDisposables.apply {

            add(RxFirebaseDatabase.observeChildEvent(FirebaseConstants.MESSAGE_DATA_REFERENCE, Message::class.java)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when (it.eventType) {
                        ADDED -> dispatcher.dispatch(MessageChildRetrievedAction(ADDED, it.key to it.value))
                        CHANGED -> dispatcher.dispatch(MessageChildRetrievedAction(CHANGED, it.key to it.value))
                        REMOVED -> dispatcher.dispatch(MessageChildRetrievedAction(REMOVED, it.key to it.value))
                        MOVED -> dispatcher.dispatch(MessageChildRetrievedAction(MOVED, it.key to it.value))
                    }
                })
        }
        return state.copy(dataDisposables = newDisposables, listening = true)
    }

    override fun onMessageDataRetrieved(state: ChatState, type: RxFirebaseChildEvent.EventType, message: Pair<String, Message>): ChatState {
        val newMessageMap = LinkedHashMap(state.messagesData)
            .apply {
                when (type) {
                    ADDED -> return@apply plusAssign(message)
                    CHANGED -> return@apply plusAssign(message)
                    REMOVED -> return@apply minusAssign(message.first)
                    MOVED -> return@apply
                }
            }
        return state.copy(messagesData = newMessageMap)
    }

    override fun sendMessage(state: ChatState, messageText: String, url: Uri?): ChatState {
        val messageToSend = Message(messageText, state.currentUser!!.displayName, url?.path)
        val newKeyForMessage = FirebaseConstants.MESSAGE_DATA_REFERENCE.push()

        //TODO We could move this to a JobDispatcher and locally save the device path to show the image instantly and upload the image in a service.
        if (url != null){
            val path = firebaseStorage
                .child("Users")
                .child(state.currentUser.uid)
                .child("ChatPictures")
            RxFirebaseStorage.putFile(path, url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapCompletable { task ->
                    RxFirebaseDatabase.setValue( newKeyForMessage, messageToSend.copy(photoUrl = task.downloadUrl.toString()))
                }.subscribe()
        } else {
            newKeyForMessage.setValue(messageToSend)
        }
        return state.copy(messagesData = state.messagesData.apply { plus(newKeyForMessage to messageToSend) })
    }

}