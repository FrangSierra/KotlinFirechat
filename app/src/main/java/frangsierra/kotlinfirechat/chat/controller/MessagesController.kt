package frangsierra.kotlinfirechat.chat.controller

import android.net.Uri
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import frangsierra.kotlinfirechat.chat.store.ListeningChatMessagesCompleteAction
import frangsierra.kotlinfirechat.chat.store.MessagesLoadedAction
import frangsierra.kotlinfirechat.chat.store.SendMessageCompleteAction
import frangsierra.kotlinfirechat.core.dagger.AppScope
import frangsierra.kotlinfirechat.core.firebase.*
import frangsierra.kotlinfirechat.core.firebase.FirebaseConstants.TOTAL_MESSAGES
import frangsierra.kotlinfirechat.core.flux.doAsync
import frangsierra.kotlinfirechat.core.service.buildUploadJob
import frangsierra.kotlinfirechat.profile.model.PublicProfile
import frangsierra.kotlinfirechat.util.taskFailure
import frangsierra.kotlinfirechat.util.taskSuccess
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import mini.Dispatcher
import javax.inject.Inject

interface ChatController {
    fun startListeningMessages()
    fun sendMessage(message: String, imageUri: Uri?, publicProfile: PublicProfile)
}

@AppScope
class ChatControllerImpl @Inject constructor(private val firestore: FirebaseFirestore,
                                             private val dispatcher: Dispatcher,
                                             private val firebaseJobDispatcher: FirebaseJobDispatcher) : ChatController {
    override fun startListeningMessages() {
        val disposable = listenMessagesFlowable()
                .map { it.documents.map { it.toMessage() } }
                .subscribeOn(Schedulers.io())
                .subscribe { dispatcher.dispatchOnUi(MessagesLoadedAction(it)) }
        dispatcher.dispatchOnUi(ListeningChatMessagesCompleteAction(disposable))
    }

    override fun sendMessage(message: String, imageUri: Uri?, publicProfile: PublicProfile) {
        doAsync {
            val newId = firestore.messages().document().id
            val firebaseMessage = FirebaseMessage(publicProfile.userData.toFirebaseUserData(), message, imageUri?.toString())

            if (imageUri != null) {
                val uploadJob = buildUploadJob(imageUri.toString(), publicProfile.userData.uid, newId, firebaseJobDispatcher.newJobBuilder())
                firebaseJobDispatcher.mustSchedule(uploadJob)
            }

            try {
                val batch = firestore.batch()
                batch.set(firestore.messageDoc(newId), firebaseMessage)
                batch.update(firestore.publicProfileDoc(publicProfile.userData.uid),
                        mapOf(TOTAL_MESSAGES to publicProfile.totalMessages.plus(1)))
                Tasks.await(batch.commit())

                dispatcher.dispatchOnUi(SendMessageCompleteAction(firebaseMessage.toMessage(newId), taskSuccess()))
            } catch (e: Throwable) {
                dispatcher.dispatchOnUi(SendMessageCompleteAction(null, taskFailure(e)))
            }
        }
    }

    private fun listenMessagesFlowable(): Flowable<QuerySnapshot> {
        return Flowable.create({ emitter ->
            val registration = firestore.messages().addSnapshotListener { documentSnapshot, e ->
                if (e != null && !emitter.isCancelled) {
                    emitter.onError(e)
                } else if (documentSnapshot != null) {
                    emitter.onNext(documentSnapshot)
                }
            }
            emitter.setCancellable { registration.remove() }
        }, BackpressureStrategy.BUFFER)
    }
}