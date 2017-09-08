package frangsierra.kotlinfirechat.common.firebase

import android.net.Uri
import com.google.android.gms.internal.zzdkw
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserInfo

class MockFirebaseUser(val uId: String, val userEmail: String?, val name: String?,
                       val number: String? = null, val pictureUrl: Uri? = null, val anonymous: Boolean = false,
                       val emailVerified: Boolean = false) : FirebaseUser() {
    override fun isAnonymous(): Boolean {
        return anonymous
    }

    override fun zzan(p0: MutableList<out UserInfo>): FirebaseUser {
        return this
    }

    override fun getEmail(): String? {
        return email
    }

    override fun zzbnz(): FirebaseApp {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun zza(p0: zzdkw) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getProviderData(): MutableList<out UserInfo> {
        return mutableListOf()
    }

    override fun zzbz(p0: Boolean): FirebaseUser {
        return this
    }

    override fun zzboa(): zzdkw {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPhoneNumber(): String? {
        return phoneNumber
    }

    override fun zzboc(): String {
        return "" //not used in mock
    }

    override fun getUid(): String {
        return uid
    }

    override fun isEmailVerified(): Boolean {
        return emailVerified
    }

    override fun getDisplayName(): String? {
        return name
    }

    override fun getPhotoUrl(): Uri? {
        return pictureUrl
    }

    override fun getProviders(): MutableList<String> {
        //TODO
        return mutableListOf()
    }

    override fun getProviderId(): String {
        return "firebase"
    }

    override fun zzbob(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}