package frangsierra.kotlinfirechat.user

import frangsierra.kotlinfirechat.common.firebase.User

data class UserDatabaseState(val userId: String? = null,
                             var messagingToken: String? = null,
                             val user: User? = null)