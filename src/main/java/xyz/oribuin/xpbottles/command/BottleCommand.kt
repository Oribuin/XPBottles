package xyz.oribuin.xpbottles.command

import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import xyz.oribuin.orilibrary.command.Command
import xyz.oribuin.orilibrary.util.StringPlaceholders
import xyz.oribuin.xpbottles.XPBottles
import xyz.oribuin.xpbottles.manager.MessageManager

@Command.Info(
    name = "bottle",
    aliases = ["xpbottle"],
    description = "Main command for obtaining xp bottles",
    usage = "/bottle <amount>",
    permission = "xpbottle.use",
    playerOnly = false,
    subCommands = [GiveSub::class, ReloadSub::class]
)
class BottleCommand(private val plugin: XPBottles) : Command(plugin) {

    private val msg = this.plugin.getManager(MessageManager::class.java)

    override fun runFunction(sender: CommandSender, label: String, args: Array<String>) {

        if (args.isEmpty()) {
            this.msg.send(sender, "invalid-args", StringPlaceholders.single("usage", "/bottle <amount>"))
            return
        }

        val subCommand = this.subCommands.find { subCommand -> subCommand.info.names.contains(args[0].lowercase()) }

        // Check if the sender used a subcommand
        if (subCommand != null) {
            this.runSubCommands(sender, args, { this.msg.send(it, "unknown-cmd") }, { this.msg.send(it, "no-perm") })
            return
        }

        // Check if sender is a player, console cant withdraw xp
        if (sender !is Player) {
            this.msg.send(sender, "player-only")
            return
        }

        val amount = if (args[0].equals("all", ignoreCase = true) || args[0].equals("max", ignoreCase = true))
            sender.totalExperience.coerceAtMost(plugin.config.getInt("max-xp"))
        else
            args[0].toIntOrNull()

        // Check if the player put in a valid number or if their number is less or equal to 0
        if (amount == null || amount <= 0) {
            this.msg.send(sender, "invalid-amount")
            return
        }

        // Check if the sender has enough xp to do it
        if (plugin.getTotalXp(sender) < this.plugin.config.getInt("min-xp")) {
            this.msg.send(sender, "not-enough-xp")
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

        if(plugin.getTotalXp(sender) < amount) {
            this.msg.send(sender, "not-enough-xp")
            return
        }

        val bottle = this.plugin.createBottle(amount, sender)
        plugin.setXp(sender, plugin.getTotalXp(sender) -amount)
        this.msg.send(sender, "withdrew-xp", StringPlaceholders.single("xp", plugin.formatNum(amount)))
        if (sender.inventory.firstEmpty() == -1) {
            this.msg.send(sender, "dropped-xp-bottle")

            sender.world.spawn(sender.location, Item::class.java) {
                it.itemStack = bottle
                it.persistentDataContainer.set(NamespacedKey(plugin, "bottleOwner"), PersistentDataType.STRING, sender.uniqueId.toString())
            }

            return
        }

        sender.inventory.addItem(bottle)
    }

    override fun completeString(sender: CommandSender, label: String, args: Array<String>): MutableList<String> {
        val tabComplete = mutableListOf<String>()

        when (args.size) {
            1 -> {
                tabComplete.addAll(subCommands.filter { sender.hasPermission(it.info.permission) }.map { it.info.names[0].lowercase() })
                tabComplete.addAll(listOf("<amount>", "max", "all"))
            }

            2 -> if (args[0].equals("give", true))
                tabComplete.addAll(playerList(sender))

            3 -> if (args[0].equals("give", true))
                tabComplete.add("<amount>")
        }
        return tabComplete
    }

}