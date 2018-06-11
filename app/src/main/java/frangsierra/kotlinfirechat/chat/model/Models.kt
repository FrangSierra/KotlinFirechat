package frangsierra.kotlinfirechat.chat.model

import com.google.firebase.firestore.ServerTimestamp
import frangsierra.kotlinfirechat.session.model.UserData
import java.util.*

data class Message(val uid: String = "",
                   val author: UserData,
                   val message: String,
                   @ServerTimestamp val timestamp: Date? = null)