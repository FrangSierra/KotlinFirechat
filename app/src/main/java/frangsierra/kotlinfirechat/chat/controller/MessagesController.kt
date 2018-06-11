package frangsierra.kotlinfirechat.chat.controller

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import frangsierra.kotlinfirechat.chat.store.ListeningChatMessagesCompleteAction
import frangsierra.kotlinfirechat.chat.store.MessagesLoadedAction
import frangsierra.kotlinfirechat.chat.store.SendMessageCompleteAction
import frangsierra.kotlinfirechat.core.dagger.AppScope
import frangsierra.kotlinfirechat.core.firebase.*
import frangsierra.kotlinfirechat.session.model.toUser
import frangsierra.kotlinfirechat.util.taskFailure
import frangsierra.kotlinfirechat.util.taskSuccess
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import mini.Dispatcher
import javax.inject.Inject

interface ChatController {
    fun startListeningMessages()
    fun sendMessage(message: String)
}

@AppScope
class ChatControllerImpl @Inject constructor(private val auth: FirebaseAuth,
                                             private val firestore: FirebaseFirestore,
                                             private val dispatcher: Dispatcher) : ChatController {
    override fun startListeningMessages() {
        val disposable = listenMessagesFlowable()
            .map { it.documents.map { it.toMessage() } }
            .subscribeOn(Schedulers.io())
            .subscribe { dispatcher.dispatchOnUi(MessagesLoadedAction(it)) }
        dispatcher.dispatchOnUi(ListeningChatMessagesCompleteAction(disposable))
    }

    override fun sendMessage(message: String) {
        val newId = firestore.messages().document().id
        val user = auth.currentUser!!.toUser() //TODO get user from other place when profile data will be added
        val author = FirebaseUserData(user.username, user.photoUrl, user.uid)
        val firebaseMessage = FirebaseMessage(author, message)

        firestore.messageDoc(newId).set(firebaseMessage)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    dispatcher.dispatchOnUi(SendMessageCompleteAction(firebaseMessage.toMessage(newId), taskSuccess()))
                } else {
                    dispatcher.dispatchOnUi(SendMessageCompleteAction(null, taskFailure(it.exception)))
                }
            }
    }

    private fun listenMessagesFlowable(): Flowable<QuerySnapshot> {
        return Flowable.create({ emitter ->
            val registration = firestore.messages().addSnapshotListener({ documentSnapshot, e ->
                if (e != null && !emitter.isCancelled) {
                    emitter.onError(e)
                } else if (documentSnapshot != null) {
                    emitter.onNext(documentSnapshot)
                }
            })
            emitter.setCancellable { registration.remove() }
        }, BackpressureStrategy.BUFFER)
    }
}