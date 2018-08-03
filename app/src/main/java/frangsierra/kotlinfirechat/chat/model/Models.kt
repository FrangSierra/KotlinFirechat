package frangsierra.kotlinfirechat.chat.model

import com.google.firebase.firestore.ServerTimestamp
import frangsierra.kotlinfirechat.profile.model.UserData
import java.util.*

data class Message(val uid: String = "",
                   val author: UserData,
                   val message: String,
                   val attachedImageUrl: String?,
                   @ServerTimestamp val timestamp: Date)