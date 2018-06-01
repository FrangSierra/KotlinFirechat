package frangsierra.kotlinfirechat.session.model

class FirebaseUserNotFound : Exception() {
    override val message: String = "There is no authenticated user connected to this auth instance"
}

class ProviderNotLinkedException(provider: String) : Exception() {
    override val message: String = "The provider $provider is not linked to the given account"
}