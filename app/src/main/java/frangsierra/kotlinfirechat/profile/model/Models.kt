package frangsierra.kotlinfirechat.profile.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.*


data class PrivateData(
        val uid: String = "",
        val email: String = "",
        val messagingTokens: List<String> = emptyList()
)

data class PublicProfile(
        val userData: UserData = UserData(),
        val lowerCaseUsername: String = "",
        val totalMessages: Int = 0,
        @ServerTimestamp val timestamp: Date? = null
)

data class UserData(val username: String = "",
                    val photoUrl: String? = null,
                    var uid: String = "") {
    val loaded = !uid.isEmpty()
}