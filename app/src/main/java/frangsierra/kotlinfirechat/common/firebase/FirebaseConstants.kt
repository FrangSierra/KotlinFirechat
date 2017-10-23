package frangsierra.kotlinfirechat.common.firebase

import com.google.firebase.database.FirebaseDatabase

object FirebaseConstants {
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    const val PEOPLE_TABLE = "userProfiles"
    const val MESSAGES_TABLE = "messagesData"

    const val PEOPLE_TABLE_USERNAME = "userName"
    const val PEOPLE_TABLE_LAST_LOGIN = "lastLogin"

    val MESSAGE_DATA_REFERENCE = database.reference.child(MESSAGES_TABLE).apply { keepSynced(true)}
    val USER_PROFILE_DATA_REFERENCE = database.reference.child(PEOPLE_TABLE)

}
