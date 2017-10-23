package frangsierra.kotlinfirechat.chat

import android.net.Uri
import com.google.firebase.auth.FirebaseUser
import durdinapps.rxfirebase2.RxFirebaseChildEvent
import frangsierra.kotlinfirechat.common.firebase.Message
import frangsierra.kotlinfirechat.common.flux.Action

data class MessageChildRetrievedAction(val type: RxFirebaseChildEvent.EventType,
                                       val data: Pair<String, Message>) : Action
data class OnUserLoggedAction(val loggedUser: FirebaseUser) : Action

data class OnTokenRefreshedAction(val refreshedToken: String) : Action

data class SendMessageAction(val messageText: String, val outputFileUri: Uri?) : Action