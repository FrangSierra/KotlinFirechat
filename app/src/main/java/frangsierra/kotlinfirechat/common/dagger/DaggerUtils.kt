package frangsierra.kotlinfirechat.common.dagger

/**
 * Look for an injection method in the component and invoke it.
 */
fun inject(component: Any, target: Any) {
    val javaClass = target.javaClass
    try {
        val injectMethod = component.javaClass.getMethod("inject", javaClass)
        injectMethod.invoke(component, target)
    } catch (e: NoSuchMethodException) {
        throw UnsupportedOperationException(
                """No injection point for $javaClass in: ${component.javaClass}.
                Expected a method in the component with signature:
                fun inject($javaClass)""".trimMargin())
    }
}