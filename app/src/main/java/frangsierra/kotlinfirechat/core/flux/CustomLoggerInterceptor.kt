package frangsierra.kotlinfirechat.core.flux

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import mini.*

/** Actions implementing this interface won't log anything */
interface SilentActionTag

class CustomLoggerInterceptor constructor(stores: Collection<Store<*>>,
                                    private val logInBackground: Boolean = false) : Interceptor {

    private val stores = stores.toList()
    private var lastActionTime = System.currentTimeMillis()
    private var actionCounter: Long = 0

    override fun invoke(action: Action, chain: Chain): Action {
        if (action is SilentActionTag) return chain.proceed(action) //Do nothing

        val beforeStates: Array<Any> = Array(stores.size, { _ -> Unit })
        val afterStates: Array<Any> = Array(stores.size, { _ -> Unit })

        stores.forEachIndexed({ idx, store -> beforeStates[idx] = store.state })
        val start = System.currentTimeMillis()
        val timeSinceLastAction = Math.min(start - lastActionTime, 9999)
        lastActionTime = start
        actionCounter++
        val out = chain.proceed(action)
        val processTime = System.currentTimeMillis() - start
        stores.forEachIndexed({ idx, store -> afterStates[idx] = store.state })

        if (action is SilentActionTag) return out

        Completable.fromAction {
            val sb = StringBuilder()
            sb.append("┌────────────────────────────────────────────\n")
            sb.append(String.format("├─> %s %dms [+%dms][%d] - %s",
                action.javaClass.simpleName, processTime, timeSinceLastAction, actionCounter % 10, action))
                .append("\n")

            // Log whether an interceptor changed the action and display the resulting action
            if (out != action) {
                sb.append(String.format("│   %s", "=== Action has been intercepted, result: ===")).append("\n")
                sb.append(String.format("├─> %s %dms [+%dms][%d] - %s",
                    out.javaClass.simpleName, processTime, timeSinceLastAction, actionCounter % 10, out))
                    .append("\n")
            }

            for (i in beforeStates.indices) {
                val oldState = beforeStates[i]
                val newState = afterStates[i]
                if (oldState !== newState) {
                    //This operation is costly, don't do it in prod
                    val line = "${stores[i].javaClass.simpleName}: $newState"
                    sb.append(String.format("│   %s", line)).append("\n")
                }
            }

            sb.append("└────────────────────────────────────────────\n")
            if (action !is SilentActionTag) {
                Grove.i { "LoggerStore $sb" }
            }
        }.let {
            if (logInBackground) it.subscribeOn(Schedulers.single())
            else it
        }.subscribe()

        return out
    }
}