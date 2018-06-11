package frangsierra.kotlinfirechat.core.firebase

import com.google.firebase.firestore.FirebaseFirestore

const val DEFAULT_QUERY_LIMIT = 10L
const val PUBLIC_PROFILE = "PublicProfile"
const val PRIVATE_DATA = "PrivateData"
const val MESSAGES = "Messages"

fun FirebaseFirestore.publicProfile() = collection(PUBLIC_PROFILE)

fun FirebaseFirestore.publicProfileDoc(uid: String) = publicProfile().document(uid)

fun FirebaseFirestore.privateData() = collection(PRIVATE_DATA)

fun FirebaseFirestore.privateDataDoc(uid: String) = privateData().document(uid)

fun FirebaseFirestore.messages() = collection(MESSAGES)

fun FirebaseFirestore.messageDoc(uid: String) = messages().document(uid)
