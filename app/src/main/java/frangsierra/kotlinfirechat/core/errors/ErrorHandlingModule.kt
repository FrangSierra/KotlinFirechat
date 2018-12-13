package frangsierra.kotlinfirechat.core.errors

import android.content.Context
import com.crashlytics.android.Crashlytics
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.Binds
import dagger.Module
import frangsierra.kotlinfirechat.R
import frangsierra.kotlinfirechat.core.dagger.AppScope
import frangsierra.kotlinfirechat.session.model.FirebaseUserNotFound
import mini.Dispatcher
import mini.log.Grove
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.net.ssl.SSLPeerUnverifiedException

@Module
@Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")
interface ErrorHandlingModule {
    @Binds
    @AppScope
    fun provideErrorHandler(errorHandler: DefaultErrorHandler): ErrorHandler
}

/**
 * A basic application generated error with a message.
 */
class GenericError(message: String?, cause: Throwable?) : Exception(message, cause)

/**
 * Interface that exposes methods to handle app errors.
 */
interface ErrorHandler {
    /** Generate an user friendly message for known errors. */
    fun getMessageForError(e: Throwable?): String

    /** Handle the error, may result in a new activity being launched. */
    fun handle(e: Throwable?)

    /**
     * Unwrap an error into the underlying http code for manual handling,
     * or null if the error is not http related
     * */
    fun unwrapCode(e: Throwable?): Int?
}

/**
 * Class that implements the main handler of the application. It maps errors and exceptions to strings
 * or starts a new activity.
 */
class DefaultErrorHandler @Inject constructor(private val context: Context,
                                              private val dispatcher: Dispatcher) : ErrorHandler {

    @Suppress("UndocumentedPublicFunction")
    override fun getMessageForError(e: Throwable?): String {
        return when (e) {
            is GenericError -> e.message ?: context.getString(R.string.error_unknown)
            is UnknownHostException -> context.getString(R.string.error_no_connection)
            is SSLPeerUnverifiedException -> context.getString(R.string.error_invalid_certificate)
            is TimeoutException -> context.getString(R.string.error_weak_password)
            is FirebaseAuthWeakPasswordException -> context.getString(R.string.error_weak_password)
            is FirebaseAuthInvalidCredentialsException -> context.getString(R.string.error_invalid_password)
            is FirebaseAuthUserCollisionException -> context.getString(R.string.error_email_already_exist)
            is FirebaseAuthInvalidUserException -> context.getString(R.string.error_invalid_account)
            is FirebaseUserNotFound -> context.getString(R.string.error_invalid_account)
            is ApiException -> {
                "${context.getString(R.string.error_google)} ${GoogleSignInStatusCodes.getStatusCodeString(e.statusCode)}"
            }
            is FirebaseFirestoreException -> retrieveFirebaseErrorMessage(e)
            else -> {
                Grove.e { "Unexpected error: $e" }
                context.getString(R.string.error_unexpected)
            }
        }
    }

    @Suppress("UndocumentedPublicFunction")
    override fun handle(e: Throwable?) {
        val exception = e as? Exception ?: Exception(e)
        Crashlytics.logException(exception)
        val errorCode = unwrapCode(e)
        if (errorCode == 401) {
            // Unauthorized
        }
    }

    @Suppress("UndocumentedPublicFunction")
    override fun unwrapCode(e: Throwable?): Int? {
        return null
    }

    private fun retrieveFirebaseErrorMessage(error: FirebaseFirestoreException): String {
        when (error.code) {
            //OK -> TODO()
            //CANCELLED -> TODO()
            //UNKNOWN -> TODO()
            //INVALID_ARGUMENT -> TODO()
            //DEADLINE_EXCEEDED -> TODO()
            //NOT_FOUND -> TODO()
            //ALREADY_EXISTS -> TODO()
            //PERMISSION_DENIED -> TODO()
            //RESOURCE_EXHAUSTED -> TODO()
            //FAILED_PRECONDITION -> TODO()
            //ABORTED -> TODO()
            //OUT_OF_RANGE -> TODO()
            //UNIMPLEMENTED -> TODO()
            //INTERNAL -> TODO()
            //UNAVAILABLE -> TODO()
            //DATA_LOSS -> TODO()
            //UNAUTHENTICATED -> TODO()
        }
        return context.getString(R.string.error_unknown)
    }
}

