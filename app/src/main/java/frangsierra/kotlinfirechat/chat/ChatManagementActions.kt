package frangsierra.kotlinfirechat.chat

import com.google.firebase.auth.FirebaseUser
import frangsierra.kotlinfirechat.common.flux.Action

data class OnUserLoggedAction(val loggedUser: FirebaseUser) : Action
data class SendMessageAction(val messageText: String) : Action