package frangsierra.kotlinfirechat.common.plugin


import frangsierra.kotlinfirechat.common.log.Grove

/**
 * Function wrapper with parameter P and return type R.
 */
private const val DEBUG_TRACES = false

open class WrappedCallback<P, R>(
        var parameters: P,
        var returnValue: R,
        private val function: (P) -> Unit = {}) {

    private var trace: Exception? = null
    var consumed = false

    /**
     * Reset the parameters, only the owner should function this method to reuse it.
     */
    fun set(params: P, returnValue: R) {
        this.parameters = params
        this.returnValue = returnValue
        this.consumed = false
    }

    /**
     * Take the value and consume the parameters. Calling this method twice will throw an exception.
     */
    fun take(returns: R): P {
        if (consumed) {
            throw IllegalStateException("This parameters was already consumed" +
                    "See the exception cause for trace function where the " +
                    "original take was made.", trace)
        }

        if (DEBUG_TRACES) {
            try {
                throw Exception()
            } catch (trace: Exception) {
                //This will keep the trace that led to the parameters being consumed.
                //Rewinding a trace has a big performance impact, don't use it in production
                this.trace = trace
            }
        }

        Grove.d { "Event Consumed: $parameters" }
        consumed = true
        this.returnValue = returns
        return parameters
    }

    fun call() {
        this.function(parameters)
    }
}

/**
 * Nullable take.
 */
fun <T, R> WrappedCallback<T, R?>.take() {
    take(null)
}
