package frangsierra.kotlinfirechat.common.firebase

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import durdinapps.rxfirebase2.RxFirebaseChildEvent
import frangsierra.kotlinfirechat.common.flux.Action
import frangsierra.kotlinfirechat.common.flux.Dispatcher

data class FirebaseChildRetrievedAction(val tag: String,
                                        val type: RxFirebaseChildEvent.EventType,
                                        val data: DataSnapshot) : Action {}

class FluxChildEventListener(val tag: String, val dispatcher: Dispatcher) : ChildEventListener {
    override fun onCancelled(error: DatabaseError) {
    }

    override fun onChildMoved(snapshot: DataSnapshot, p1: String?) {
        dispatcher.dispatch(FirebaseChildRetrievedAction(tag, RxFirebaseChildEvent.EventType.MOVED, snapshot))
    }

    override fun onChildChanged(snapshot: DataSnapshot, p1: String?) {
        dispatcher.dispatch(FirebaseChildRetrievedAction(tag, RxFirebaseChildEvent.EventType.CHANGED, snapshot))
    }

    override fun onChildAdded(snapshot: DataSnapshot, p1: String?) {
        dispatcher.dispatch(FirebaseChildRetrievedAction(tag, RxFirebaseChildEvent.EventType.ADDED, snapshot))
    }

    override fun onChildRemoved(snapshot: DataSnapshot) {
        dispatcher.dispatch(FirebaseChildRetrievedAction(tag, RxFirebaseChildEvent.EventType.REMOVED, snapshot))
    }

}