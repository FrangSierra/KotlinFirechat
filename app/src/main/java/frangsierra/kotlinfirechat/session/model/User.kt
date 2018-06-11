package frangsierra.kotlinfirechat.session.model

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class User(val uid: String,
                val username: String,
                val photoUrl: String?,
                val email: String)

fun FirebaseUser.toUser(): User = User(
    uid = uid,
    username = displayName ?: "Anonymous",
    photoUrl = photoUrl?.toString(),
    email = email!!)

fun FirebaseUser.associatedProviders(): List<LoginProvider> =
    providerData.mapNotNull { userInfo ->
        LoginProvider.values().firstOrNull { it.value() == userInfo.providerId }
    }

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