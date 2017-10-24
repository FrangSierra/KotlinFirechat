package frangsierra.kotlinfirechat.user

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.iid.FirebaseInstanceId
import durdinapps.rxfirebase2.RxFirebaseDatabase
import durdinapps.rxfirebase2.RxFirebaseUser
import frangsierra.kotlinfirechat.common.dagger.AppScope
import frangsierra.kotlinfirechat.common.firebase.FirebaseConstants
import frangsierra.kotlinfirechat.common.firebase.FirebaseConstants.PEOPLE_TABLE_LAST_LOGIN
import frangsierra.kotlinfirechat.common.firebase.FirebaseConstants.USER_PROFILE_DATA_REFERENCE
import frangsierra.kotlinfirechat.common.firebase.User
import frangsierra.kotlinfirechat.common.flux.Dispatcher
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@AppScope
class UserDatabaseControllerImpl @Inject constructor(val dispatcher: Dispatcher) : UserDatabaseController {

    override fun onUserLogged(state: UserDatabaseState, userId: String, instanceId: FirebaseInstanceId): UserDatabaseState {
        //Get the timestamp and update the lastLoginValue from the User data
        val currentTimeMillis = System.currentTimeMillis()
        RxFirebaseDatabase.updateChildren(USER_PROFILE_DATA_REFERENCE.child(userId), mapOf(PEOPLE_TABLE_LAST_LOGIN to currentTimeMillis)).subscribe()

        state.messagingToken?.let {
            RxFirebaseDatabase.updateChildren(FirebaseConstants.USER_PROFILE_DATA_REFERENCE.child(userId), mapOf( FirebaseConstants.MESSAGING_TOKEN to it) )
                .subscribe()
        }
        //TODO Retrieve user data if we are gonna use it
        return if (state.messagingToken == null) state.copy(userId = userId, messagingToken = instanceId.token)
        else state.copy(userId = userId)
    }

    override fun updateProfileDisplayName(state: UserDatabaseState, user: FirebaseUser?, newName: String): UserDatabaseState {

        //Generate a userProfileChangeRequest with the desired values
        val userProfileChangeRequest = UserProfileChangeRequest.Builder().setDisplayName(newName).build()
        //Throw a new updateProfileCallback to update the firebase user profile
        RxFirebaseUser.updateProfile(user!!, userProfileChangeRequest)
            .subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
            .subscribe()

        val user = state.user ?: User(newName, user.photoUrl.toString(), user.email)
        return state.copy(user = user)
    }

    override fun createProfileData(newState: UserDatabaseState, createdUser: FirebaseUser, user: User): UserDatabaseState {
        val newUser = user.copy(email = createdUser.email!!, photoUrl = createdUser.photoUrl.toString(),
            userName = createdUser.displayName, lastLogin = System.currentTimeMillis())

        //Push it to database and update the state with the local values
        RxFirebaseDatabase.setValue(USER_PROFILE_DATA_REFERENCE.child(createdUser.uid), newUser).subscribe()

        return if (createdUser.displayName == null) updateProfileDisplayName(newState, createdUser, user.userName!!)
            .copy(user = newUser)
        else newState.copy(user = newUser)
    }
}