package frangsierra.kotlinfirechat.session.model

/**
 * Represent the possible providers used in the application.
 */
enum class LoginProvider(private val providerName: String) {
    FIREBASE("firebase"),
    PASSWORD("password"),
    GOOGLE("google.com");

    companion object {
        fun withValue(providerName: String) = LoginProvider.values().first { it.providerName == providerName }
    }

    fun value(): String = this.providerName
}