package frangsierra.kotlinfirechat.core.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ServerTimestamp
import frangsierra.kotlinfirechat.chat.model.Message
import frangsierra.kotlinfirechat.session.model.PrivateData
import frangsierra.kotlinfirechat.session.model.PublicProfile
import frangsierra.kotlinfirechat.session.model.UserData
import java.util.*

data class FirebaseUserData(val username: String = "",
                            val photoUrl: String? = null,
                            var uid: String = "")

fun FirebaseUserData.toUserData() = UserData(username = username, photoUrl = photoUrl, uid = uid)

data class FirebaseMessage(val author: FirebaseUserData = FirebaseUserData(),
                           val message: String = "",
                           @ServerTimestamp val timestamp: Date? = null)

fun DocumentSnapshot.toMessage() = toObject(FirebaseMessage::class.java)!!.toMessage(id)

fun FirebaseMessage.toMessage(id: String) = Message(uid = id,
    author = author.toUserData(),
    message = message,
    timestamp = timestamp)

data class FirebasePrivateData(
    val email: String = "",
    val messagingTokens: List<String> = emptyList()
)

fun DocumentSnapshot.toPrivateData() = toObject(FirebasePrivateData::class.java)!!.toPrivateData(id)

fun FirebasePrivateData.toPrivateData(id: String) = PrivateData(uid = id,
    email = email,
    messagingTokens = messagingTokens)

data class FirebasePublicProfile(
    val userData: FirebaseUserData = FirebaseUserData(),
    val lowerCaseUsername: String = "",
    val totalMessages: Int = 0,
    @ServerTimestamp val timestamp: Date? = null
)

fun DocumentSnapshot.toPublicProfile() = toObject(FirebasePublicProfile::class.java)!!.toPublicProfile()

fun FirebasePublicProfile.toPublicProfile() = PublicProfile(userData = userData.toUserData(),
    lowerCaseUsername = lowerCaseUsername,
    totalMessages = totalMessages,
    timestamp = timestamp)