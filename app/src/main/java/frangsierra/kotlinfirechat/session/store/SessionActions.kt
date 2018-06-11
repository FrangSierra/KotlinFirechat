package frangsierra.kotlinfirechat.session.store

import com.google.firebase.auth.AuthCredential
import frangsierra.kotlinfirechat.session.model.LoginProvider
import frangsierra.kotlinfirechat.session.model.User
import frangsierra.kotlinfirechat.util.Task
import mini.Action

/**
 * Action dispatched to sign out the current account and reset all the data.
 */
class SignOutAction : Action

/**
 * Action dispatched to refresh the user data.
 */
class RefreshUserAction : Action

/**
 * Action dispatched when the user token is refresheed.
 */
class UserRefreshedCompleteAction(val task: Task,
                                  val user: User?,
                                  val associatedProviders: List<LoginProvider> = listOf()) : Action

/**
 * Action dispatched to start the process for verify the email of the current user.
 */
class VerifyUserEmailAction : Action

/**
 * Action dispatched when the verification refresh is complete.
 */
class VerifyUserEmailCompleteAction(val task: Task,
                                    val verified: Boolean = false) : Action

/**
 * Action dispatched to reset the password of the current user.
 */
data class ResetPasswordAction(val email: String) : Action

/**
 * Action dispatched when the reset password email as been sent.
 */
data class ResetPasswordEmailSentAction(val task: Task) : Action

/**
 * Action dispatched to send a verification email when a new account is created or a
 * email provider is attached to an user.
 */
class SendVerificationEmailAction : Action

/**
 * Action dispatched when the verification email has been sent.
 */
data class VerificationEmailSentAction(val task: Task) : Action

/**
 * Action dispatched on Appstart to try to log the user if the credentials still on the cache.
 */
class TryToLoginInFirstInstanceAction : Action

/**
 * Action dispatched to login in the app with an email and a password.
 */
data class LoginWithCredentials(val email: String, val password: String) : Action

/**
 * Action dispatched to login with an external provider credential.
 */
data class LoginWithProviderCredentials(val credential: AuthCredential,
                                        val email: String) : Action

/**
 * Action dispatched when login process as finished.
 */
data class LoginCompleteAction(val user: User? = null,
                               val emailVerified: Boolean = false,
                               val task: Task,
                               val associatedProviders: List<LoginProvider> = listOf()) : Action

/**
 * Action dispatched to create an account with an user, password and a new username.
 */
data class CreateAccountWithCredentialsAction(val email: String,
                                              val password: String,
                                              val username: String) : Action

/**
 * Action dispatched to create an account with an external provider credential.
 */
data class CreateAccountWithProviderCredentialsAction(val credential: AuthCredential,
                                                      val user: User) : Action

/**
 * Action dispatched when the create account process as finished.
 */
data class CreateAccountCompleteAction(val user: User?,
                                       val alreadyExisted: Boolean = false,
                                       val emailVerified: Boolean = false,
                                       val task: Task,
                                       val associatedProviders: List<LoginProvider> = listOf()) : Action