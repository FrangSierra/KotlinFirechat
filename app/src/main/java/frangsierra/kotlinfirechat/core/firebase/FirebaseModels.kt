package frangsierra.kotlinfirechat.core.firebase

import com.google.firebase.auth.FirebaseUser
import frangsierra.kotlinfirechat.session.model.LoginProvider

data class Message(val text: String? = null,
                   val name: String? = null,
                   val photoUrl: String? = null,
                   val timeStamp: Long = System.currentTimeMillis())

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

