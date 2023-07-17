package xyz.oribuin.xpbottles.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import xyz.oribuin.orilibrary.util.StringPlaceholders
import xyz.oribuin.xpbottles.XPBottles
import xyz.oribuin.xpbottles.manager.MessageManager

class ThrowListener(private val plugin: XPBottles) : Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun PlayerInteractEvent.onThrow() {

        if (this.action != Action.RIGHT_CLICK_BLOCK && this.action != Action.RIGHT_CLICK_AIR)
            return

        val item = this.item ?: return

        val itemMeta = item.itemMeta ?: return
        val container = itemMeta.persistentDataContainer

        val xpAmount = container.get(plugin.key, PersistentDataType.INTEGER) ?: return
        plugin.setXp(this.player, this.player.totalExperience + xpAmount)
        item.amount = item.amount - 1
        plugin.getManager(MessageManager::class.java).send(player, "gained-xp", StringPlaceholders.single("xp", plugin.formatNum(xpAmount)))
        this.isCancelled = true
    }
}