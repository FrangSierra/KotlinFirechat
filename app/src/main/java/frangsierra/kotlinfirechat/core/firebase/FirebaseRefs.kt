package frangsierra.kotlinfirechat.core.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

const val DEFAULT_QUERY_LIMIT = 10L
const val PUBLIC_PROFILE = "PublicProfile"
const val PRIVATE_DATA = "PrivateData"
const val MESSAGES = "Messages"
const val STORAGE_USER = "User"
const val STORAGE_MESSAGES = "Messages"

fun FirebaseFirestore.publicProfile() = collection(PUBLIC_PROFILE)

fun FirebaseFirestore.publicProfileDoc(uid: String) = publicProfile().document(uid)

fun FirebaseFirestore.privateData() = collection(PRIVATE_DATA)

fun FirebaseFirestore.privateDataDoc(uid: String) = privateData().document(uid)

fun FirebaseFirestore.messages() = collection(MESSAGES)

fun FirebaseFirestore.messageDoc(uid: String) = messages().document(uid)

fun FirebaseStorage.chatStorageMessageRef(userId: String, uid: String) = reference
        .child(STORAGE_USER)
        .child(userId)
        .child(STORAGE_MESSAGES)
        .child(uid)

