package frangsierra.kotlinfirechat.session

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import frangsierra.kotlinfirechat.common.firebase.User
import frangsierra.kotlinfirechat.common.flux.Action

/**
 * Action dispatched when the current user want to sign out his current session.
 */
class SignOutAction : Action

/**
 * Action dispatched when the current user wants to create an account with an email and a password.
 */
data class CreateAccountWithEmailAction(val user: User, val password: String) : Action

/**
 * Action dispatched when there is an authentication error.
 */
data class AuthenticationErrorAction(var throwable: Throwable?) : Action

/**
 * Action dispatched when the current user wants to login with an email and a password.
 */
data class LoginWithEmailAndPasswordAction(val email: String, val password: String) : Action

/**
 * Action dispatched when an account is successfully created.
 */
data class AccountSuccessfullyCreatedAction(val createdUser: FirebaseUser, val user: User) : Action

/**
 * Action dispatched when the authentication status changes.
 */
data class AuthenticationStatusChangedAction(val loginStatus: LoginStatus = LoginStatus.UNLOGGED, val loggedUser: FirebaseUser?) : Action

/**
 * Action dispatched when an user wants to create/login with an 3º party credential(Google, Facebook...)
 */
data class ManageCredentialAction(val credential: AuthCredential, val user: User) : Action
