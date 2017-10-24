package frangsierra.kotlinfirechat.user

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.iid.FirebaseInstanceId
import frangsierra.kotlinfirechat.common.firebase.User

interface UserDatabaseController {

    /**
     * Upload to the cloud the basic data of an user when a new account is created.
     */
    fun createProfileData(newState: UserDatabaseState, createdUser: FirebaseUser, user: User): UserDatabaseState

    /**
     * Update the FirebaseUser data and the database with a new username.
     */
    fun updateProfileDisplayName(state: UserDatabaseState, user: FirebaseUser?, newName: String): UserDatabaseState

    /**
     * Update the state with the right data when the an user is successfully logged.
     */
    fun onUserLogged(state: UserDatabaseState, userId: String, instanceId: FirebaseInstanceId): UserDatabaseState

    /**
     * Modify the state when a new messaging token is retrieved.
     */
    fun onTokenRefreshedAction(state: UserDatabaseState, refreshedToken: String): UserDatabaseState =
        state.copy(messagingToken = refreshedToken)
}