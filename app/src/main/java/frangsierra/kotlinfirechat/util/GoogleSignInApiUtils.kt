package frangsierra.kotlinfirechat.util

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import frangsierra.kotlinfirechat.session.model.User

object GoogleSignInApiUtils {

    private const val GOOGLE_ACCOUNT_DEFAULT_IMAGE_SIZE = "s96-c"
    private const val GOOGLE_ACCOUNT_DESIRED_IMAGE_SIZE = "s600-c"

    fun getUserData(account: GoogleSignInAccount): User {
        return User(email = account.email!!,
            photoUrl = retrieveResizedGoogleAccountPicture(account),
            username = "${account.displayName}",
            uid = "")
    }

    private fun retrieveResizedGoogleAccountPicture(account: GoogleSignInAccount): String? =
        account.photoUrl?.toString()?.replace(GOOGLE_ACCOUNT_DEFAULT_IMAGE_SIZE, GOOGLE_ACCOUNT_DESIRED_IMAGE_SIZE, true)
}
