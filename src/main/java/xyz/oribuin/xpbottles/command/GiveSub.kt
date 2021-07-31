package xyz.oribuin.xpbottles.command

import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Item
import org.bukkit.persistence.PersistentDataType
import xyz.oribuin.orilibrary.command.SubCommand
import xyz.oribuin.orilibrary.util.StringPlaceholders
import xyz.oribuin.xpbottles.XPBottles
import xyz.oribuin.xpbottles.manager.MessageManager

@SubCommand.Info(
    names = ["give"],
    usage = "/bottles give <player> <xp>",
    permission = "xpbottles.give"
)
class GiveSub(private val plugin: XPBottles, cmd: BottleCommand) : SubCommand(plugin, cmd) {

    private val msg = this.plugin.getManager(MessageManager::class.java)

    override fun executeArgument(sender: CommandSender, args: Array<String>) {

        // Check arg length
        if (args.size != 3) {
            this.msg.send(sender, "invalid-args", StringPlaceholders.single("usage", this.info.usage))
            return
        }

        // Check player
        val player = Bukkit.getPlayer(args[1])
        if (player == null) {
            this.msg.send(sender, "invalid-player")
            return
        }

        // Check amount
        val amount = args[2].toIntOrNull()
        if (amount == null || amount <= 0) {
            this.msg.send(sender, "invalid-amount")
            return
        }

        // Check if the amount provided is more than cap
        if (amount > this.plugin.config.getInt("max-xp")) {
            this.msg.send(sender, "max-xp")
            return
        }

        if (amount < this.plugin.config.getInt("min-xp")) {
            this.msg.send(sender, "min-xp")
            return
        }

        val bottle = this.plugin.createBottle(amount, player)
        this.msg.send(player, "given-bottle", StringPlaceholders.single("xp", plugin.formatNum(amount)))
        if (player.inventory.firstEmpty() == -1) {
            this.msg.send(sender, "dropped-xp-bottle")

            player.world.spawn(player.location, Item::class.java) {
                it.itemStack = bottle
                it.persistentDataContainer.set(NamespacedKey(plugin, "bottleOwner"), PersistentDataType.STRING, player.uniqueId.toString())
            }

            return
        }
        player.inventory.addItem(bottle)
    }
}