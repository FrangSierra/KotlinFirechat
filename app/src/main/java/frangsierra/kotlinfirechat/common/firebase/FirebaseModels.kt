package frangsierra.kotlinfirechat.common.firebase

data class Message(val text: String? = null, val name: String? = null, val photoUrl: String? = null, val timeStamp: Long = System.currentTimeMillis())
data class User(val secondName: String? = null, val userName: String? = null, val email: String? = null)