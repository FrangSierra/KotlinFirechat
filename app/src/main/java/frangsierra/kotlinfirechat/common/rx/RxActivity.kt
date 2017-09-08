package frangsierra.kotlinfirechat.common.rx

import android.app.Activity
import android.support.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class RxActivity : Activity() {

    private val disposables = CompositeDisposable()

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    fun Disposable.track() {
        disposables.add(this)
    }
}