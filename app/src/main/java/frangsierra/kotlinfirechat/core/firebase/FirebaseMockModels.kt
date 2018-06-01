package frangsierra.kotlinfirechat.common.firebase

import java.util.*

class FirebaseMockModels {

    companion object {

        val mockMessageKey get() = Random().nextLong().toString()
        val mockMessage = Message("Test", "User Test")
        val mockUser = User("Test", "User", "tests@testing.com")

        val mockMessageRetrieved get() = mockMessageKey to mockMessage
    }

}
