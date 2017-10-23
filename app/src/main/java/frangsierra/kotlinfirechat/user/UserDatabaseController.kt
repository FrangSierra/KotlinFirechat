package frangsierra.kotlinfirechat.user

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.iid.FirebaseInstanceId
import frangsierra.kotlinfirechat.common.firebase.User

interface UserDatabaseController {

    fun createProfileData(newState: UserDatabaseState, createdUser: FirebaseUser, user: User): UserDatabaseState

    fun updateProfileDisplayName(state: UserDatabaseState, user: FirebaseUser?, newName: String): UserDatabaseState

    fun onUserLogged(state: UserDatabaseState, userId: String, instanceId: FirebaseInstanceId): UserDatabaseState

    fun onTokenRefreshedAction(state: UserDatabaseState, refreshedToken: String): UserDatabaseState =
        state.copy(messagingToken = refreshedToken)
}