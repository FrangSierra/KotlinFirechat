package frangsierra.kotlinfirechat.core.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ServerTimestamp
import frangsierra.kotlinfirechat.chat.model.Message
import frangsierra.kotlinfirechat.profile.model.PrivateData
import frangsierra.kotlinfirechat.profile.model.PublicProfile
import frangsierra.kotlinfirechat.profile.model.UserData
import java.util.*

fun UserData.toFirebaseUserData() = FirebaseUserData(username, photoUrl, uid)

data class FirebaseUserData(val username: String = "",
                            val photoUrl: String? = null,
                            var uid: String = "")

fun FirebaseUserData.toUserData() = UserData(username = username, photoUrl = photoUrl, uid = uid)

data class FirebaseMessage(val author: FirebaseUserData = FirebaseUserData(),
                           val message: String = "",
                           val attachedImageUrl : String? = null,
                           @ServerTimestamp val timestamp: Timestamp? = null)

fun DocumentSnapshot.toMessage() = toObject(FirebaseMessage::class.java)!!.toMessage(id)

fun FirebaseMessage.toMessage(id: String) = Message(uid = id,
        author = author.toUserData(),
        message = message,
        attachedImageUrl = attachedImageUrl,
        timestamp = timestamp?.toDate() ?: Timestamp.now().toDate())

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
        @ServerTimestamp val lastLogin: Timestamp? = null
)

fun DocumentSnapshot.toPublicProfile() = toObject(FirebasePublicProfile::class.java)!!.toPublicProfile()

fun FirebasePublicProfile.toPublicProfile() = PublicProfile(userData = userData.toUserData(),
        lowerCaseUsername = lowerCaseUsername,
        totalMessages = totalMessages,
        lastLogin = lastLogin?.toDate() ?: Timestamp.now().toDate())