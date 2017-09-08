package frangsierra.kotlinfirechat.session

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import frangsierra.kotlinfirechat.common.firebase.User
import frangsierra.kotlinfirechat.common.flux.Action

class SignOutAction : Action
data class CreateAccountWithEmailAction(val user: User, val password: String) : Action
data class AuthenticationErrorAction(var throwable: Throwable?) : Action
data class LoginWithEmailAndPasswordAction(val email: String, val password: String) : Action
data class AccountSuccessfullyCreatedAction(val createdUser: FirebaseUser, val user: User) : Action
data class AuthenticationStatusChangedAction(val loginStatus: LoginStatus = LoginStatus.UNLOGGED, val loggedUser: FirebaseUser?) : Action
data class ManageCredentialAction(val credential: AuthCredential, val user: User) : Action
