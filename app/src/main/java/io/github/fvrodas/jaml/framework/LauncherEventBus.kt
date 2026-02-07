package io.github.fvrodas.jaml.framework

object LauncherEventBus {

    private val listeners = mutableListOf<LauncherEventListener>()

    fun postEvent(event: LauncherEvents) {
        for (listener in listeners) {
            when (event) {
                is LauncherEvents.OnPackageChanged -> listener.onPackageChanged()
                is LauncherEvents.OnNotificationChanged -> listener.onNotificationChanged(
                    event.packageName, event.message
                )
            }
        }
    }

    fun registerListener(listener: LauncherEventListener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: LauncherEventListener) {
        listeners.remove(listener)
    }
}

sealed interface LauncherEvents {
    object OnPackageChanged : LauncherEvents
    data class OnNotificationChanged(
        val packageName: String?,
        val message: String?
    ) : LauncherEvents
}

interface LauncherEventListener {
    fun onPackageChanged()
    fun onNotificationChanged(
        packageName: String?,
        message: String? = null
    )
}
