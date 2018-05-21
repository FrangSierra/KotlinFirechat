package frangsierra.kotlinfirechat.common.log

import frangsierra.kotlinfirechat.BuildConfig
import frangsierra.kotlinfirechat.common.flux.Action
import frangsierra.kotlinfirechat.common.flux.Chain
import frangsierra.kotlinfirechat.common.flux.Interceptor
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import frangsierra.kotlinfirechat.common.flux.Store
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.kotlinProperty

/** Actions implementing this interface won't log anything */
interface SilentTag

private val LOG_IN_BACKGROUND = !BuildConfig.DEBUG

/** Actions implementing this interface won't log anything */
interface SilentAction

internal class LoggerInterceptor constructor(stores: Collection<Store<*>>) : Interceptor {
    private val stores = stores.toList()
    private var lastActionTime = System.currentTimeMillis()
    private var actionCounter: Long = 0
    //TODO: Remove the hardcoded package to reuse better in future projects
    private var diffFinder = DiffFinder("com.bq.camera3")

    override fun invoke(action: Action, chain: Chain): Action {
        if (action is SilentAction) return chain.proceed(action) //Do nothing

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
                    val fn = if (BuildConfig.DEBUG) {
                        {
                            val diff = diffFinder.diff(oldState, newState)
                            "${stores[i].javaClass.simpleName}: $diff"
                        }
                    } else {
                        { newState.toString() }
                    }
                    sb.append(String.format("│   %s", fn())).append("\n")
                }
            }

            sb.append("└────────────────────────────────────────────").append("\n")
            if (action !is SilentAction) {
                Grove.i { "LoggerStore $sb" }
            }
        }.let {
            if (LOG_IN_BACKGROUND) it.subscribeOn(Schedulers.single())
            else it
        }.subscribe()

        return out
    }
}

private data class DiffResult(val diffs: List<Diff>, val diffTime: Long) {
    override fun toString(): String {
        return "[${diffTime}ms] ${diffs.joinToString(separator = ", ")}"
    }
}

private data class Diff(val path: String, val a: Any?, val b: Any?) {
    override fun toString(): String {
        return "$path=($a ~> $b)"
    }
}

/**
 * Scan public properties and find differences for objects inside the application package.
 * Java / Kotlin types are ignored, only default equality is performed.
 */
private data class DiffFinder(private val packagePath: String) {

    /**
     * Diff the two objects, must have the same type.
     */
    fun <T : Any> diff(a: T?, b: T?): DiffResult {
        val start = System.nanoTime()
        val out = ArrayList<Diff>()
        diff(Stack(), out, a, b)
        val elapsed = System.nanoTime() - start
        return DiffResult(out, TimeUnit.NANOSECONDS.toMillis(elapsed))
    }

    private fun <T : Any> diff(crumbs: Stack<String>,
                               diffs: MutableList<Diff>,
                               a: T?, b: T?) {

        val anyIsNull = a == null || b == null
        //Enums blow up for some unknown reason
        val isEnum = a?.javaClass?.isEnum ?: false
        //Maps are a special case
        //val isMap = if (a == null) false else Map::class.java.isAssignableFrom(a.javaClass)
        //Need this to avoid performing reflection for external types
        val fromTargetPackage = a?.javaClass
                ?.`package`
                ?.name
                ?.startsWith(packagePath)
                ?: false
//        if (isMap) {
//            val oldMap = a as Map<*, *>
//            val newMap = b as Map<*, *>
//            newMap.forEach { (key, value) ->
//                if (oldMap.containsKey(key)) {
//                    diff(crumbs, diffs, oldMap[key]!!, value)
//                } else {
//                    diffs.add(Diff(crumbs.joinToString(separator = "."), a, b)) //CHECK
//                }
//            }
        if (anyIsNull || isEnum || !fromTargetPackage) {
            if (a != b) {
                diffs.add(Diff(crumbs.joinToString(separator = "."), a, b))
            }
        } else {
            for (field in a!!.javaClass.declaredFields) {
                val prop = try {
                    field.kotlinProperty
                } catch (ex: Throwable) {
                    null
                } ?: continue
                //No reason to inspect non-public props
                if (prop.getter.visibility != KVisibility.PUBLIC) continue
                crumbs.push(prop.name)
                diff(crumbs, diffs, prop.getter.call(a), prop.getter.call(b))
                crumbs.pop()
            }
        }
    }
}