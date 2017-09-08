package frangsierra.kotlinfirechat.common.plugin

/**
 * Runtime properties of a plugin.
 */
data class PluginProperties constructor(
        val willHandleTouch: Boolean,
        val lifecyclePriority: Int,
        val backPriority: Int,
        val touchPriority: Int) {

    companion object {
        val DEFAULT_PRIORITY = 50
        val MAX = PluginProperties(false, 0, 0, 0)
        val HIGH = PluginProperties(false, 25, 25, 25)
        val DEFAULT = PluginProperties(false, DEFAULT_PRIORITY, DEFAULT_PRIORITY, DEFAULT_PRIORITY)
        val LOW = PluginProperties(false, 75, 75, 75)
        val MIN = PluginProperties(false, 100, 100, 100)
    }
}
