package frangsierra.kotlinfirechat.common.log

import frangsierra.kotlinfirechat.common.flux.Action
import frangsierra.kotlinfirechat.common.flux.Chain
import frangsierra.kotlinfirechat.common.flux.Interceptor
import frangsierra.kotlinfirechat.common.flux.Store

internal class LoggerInterceptor constructor(stores: Collection<Store<*>>) : Interceptor {
    private var firstPass = true
    private val stores = stores.toList()
    private var states: Array<Any?> = arrayOfNulls(stores.size)
    private var lastActionTime = System.currentTimeMillis()
    @Volatile private var actionCounter: Long = 0

    override fun invoke(action: Action, chain: Chain): Action {
        val start = System.currentTimeMillis()
        val timeSinceLastAction = Math.min(start - lastActionTime, 9999)
        lastActionTime = start
        actionCounter++

        if (firstPass) { //First pass
            firstPass = false
            states = arrayOfNulls<Any>(stores.size)
            for (i in states.indices) {
                states[i] = stores[i].state
            }
        }

        //Process the action
        val out = chain.proceed(action)
        val processTime = System.currentTimeMillis() - start

        val sb = StringBuilder()
        sb.append("┌────────────────────────────────────────────\n")
        sb.append(String.format("├─> %s %dms [+%dms][%d] - %s",
                action.javaClass.simpleName, processTime, timeSinceLastAction, actionCounter % 10, action))
                .append("\n")


        stores.forEachIndexed { index, store ->
            val oldState = states[index]
            val newState = store.state
            val fn = store.properties.logFn ?: { store.state.toString() }
            if (oldState !== newState) {
                sb.append(String.format("│   %s", fn())).append("\n")
            }
            states[index] = newState
        }

        sb.append("└────────────────────────────────────────────").append("\n")
        Grove.tag("LoggerStore").i { sb.toString() }

        return out
    }
}