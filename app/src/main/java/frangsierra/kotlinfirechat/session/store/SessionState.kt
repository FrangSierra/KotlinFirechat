package frangsierra.kotlinfirechat.session.store

import frangsierra.kotlinfirechat.session.model.LoginProvider
import frangsierra.kotlinfirechat.session.model.User
import mini.Task
import mini.taskIdle

data class SessionState(val verified: Boolean = false,
                        val createAccountTask: Task = taskIdle(),
                        val loginTask: Task = taskIdle(),
                        val verifyUserTask: Task = taskIdle(),
                        val verificationEmailTask: Task = taskIdle(),
                        val refreshUserTask: Task = taskIdle(),
                        val providers: List<LoginProvider> = listOf(),
                        val loggedUser: User? = null)