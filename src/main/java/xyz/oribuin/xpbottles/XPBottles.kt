package xyz.oribuin.xpbottles

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import xyz.oribuin.orilibrary.OriPlugin
import xyz.oribuin.orilibrary.util.HexUtils
import xyz.oribuin.orilibrary.util.StringPlaceholders
import xyz.oribuin.xpbottles.command.BottleCommand
import xyz.oribuin.xpbottles.listener.PickupListener
import xyz.oribuin.xpbottles.listener.ThrowListener
import xyz.oribuin.xpbottles.manager.MessageManager

class XPBottles : OriPlugin() {

    val key = NamespacedKey(this, "xp")

    override fun enablePlugin() {

        // Register Managers
        val msg = this.getManager(MessageManager::class.java)

        // Register Commands
        BottleCommand(this).register({ msg.send(it, "player-only") }) {
            msg.send(it, "no-perm")
        }

        // Register Listeners
        this.server.pluginManager.registerEvents(PickupListener(this), this)
        this.server.pluginManager.registerEvents(ThrowListener(this), this)
    }

    override fun disablePlugin() {

    }

    /**
     * Create the bottle itemstack with the required XP saved into it
     *
     * @param xp The amount of xp being saved
     * @sender The person who created the bottle
     * @return The bottle.
     */
    fun createBottle(xp: Int, sender: CommandSender): ItemStack {
        val itemStack = ItemStack(Material.EXPERIENCE_BOTTLE)
        val meta = itemStack.itemMeta ?: return ItemStack(Material.AIR)
        val container = meta.persistentDataContainer
        container.set(key, PersistentDataType.INTEGER, xp)

        val holder = StringPlaceholders.builder("xp", xp)
            .addPlaceholder("owner", sender.name)
            .build()

        meta.setDisplayName(HexUtils.colorify(holder.apply(this.config.getString("Item.Name") ?: "#f953c6XP Bottle (#12c2e9%xp%#f953c6)")))
        meta.lore = this.config.getStringList("Item.Lore").map { HexUtils.colorify(holder.apply(it)) }
        itemStack.itemMeta = meta
        return itemStack
    }

    fun formatNum(num: Int): String = String.format("%,d", num)

}