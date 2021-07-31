package xyz.oribuin.xpbottles.listener

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.persistence.PersistentDataType
import xyz.oribuin.xpbottles.XPBottles
import java.util.*

class PickupListener(private val plugin: XPBottles) : Listener {

    @EventHandler(priority = EventPriority.LOW)
    fun EntityPickupItemEvent.onPickup() {
        if (this.entity !is Player)
            return

        val container = item.persistentDataContainer
        val key = NamespacedKey(plugin, "bottleOwner")
        if (!container.has(key, PersistentDataType.STRING))
            return

        val bottleOwner = UUID.fromString(container.get(key, PersistentDataType.STRING))
        if (this.entity.uniqueId != bottleOwner)
            this.isCancelled = true
    }

}