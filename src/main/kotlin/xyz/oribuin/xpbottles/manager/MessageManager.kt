package xyz.oribuin.xpbottles.manager

import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import xyz.oribuin.orilibrary.manager.Manager
import xyz.oribuin.orilibrary.util.FileUtils
import xyz.oribuin.orilibrary.util.HexUtils
import xyz.oribuin.orilibrary.util.StringPlaceholders
import xyz.oribuin.xpbottles.XPBottles
import java.io.File

class MessageManager(private val plugin: XPBottles) : Manager(plugin) {

    private lateinit var config: FileConfiguration

    override fun enable() {
        config = YamlConfiguration.loadConfiguration(FileUtils.createFile(plugin, "messages.yml"))

        // Set any values that dont exist
        for (msg in Messages.values()) {
            val key = msg.name.lowercase().replace("_", "-")
            if (config.get(key) == null) {
                config.set(key, msg.value)
            }
        }

        config.save(File(plugin.dataFolder, "messages.yml"))
    }

    /**
     * Send a configuration messageId with placeholders.
     *
     * @param receiver     The CommandSender who receives the messageId.
     * @param messageId    The messageId path
     * @param placeholders The Placeholders
     */
    fun send(receiver: CommandSender, messageId: String, placeholders: StringPlaceholders = StringPlaceholders.empty()) {
        val prefix = this.config.getString("prefix") ?: Messages.PREFIX.value
        receiver.sendMessage(HexUtils.colorify(prefix + placeholders.apply(this.get(messageId.uppercase()))))
    }

    /**
     * Send a raw message to the receiver with placeholders.
     *
     *
     * Use this to send a message to a player without the message being defined in a config.
     *
     * @param receiver     The message receiver
     * @param message      The message
     * @param placeholders Message Placeholders.
     */
    fun sendRaw(receiver: CommandSender, message: String?, placeholders: StringPlaceholders = StringPlaceholders.empty()) {
        receiver.sendMessage(HexUtils.colorify(placeholders.apply(message)))
    }

    fun get(message: String): String {
        return HexUtils.colorify(config.getString(message) ?: Messages.valueOf(message.replace("-", "_")).value)
    }

    override fun disable() {}

    enum class Messages(val value: String) {
        PREFIX("#99ff99&lXPBottles &8| &f"),
        WITHDREW_XP("You have withdrawn #99ff99%xp% &fxp into a bottle!"),
        DROPPED_XP_BOTTLE("The XP Bottle has dropped since your inventory was full."),
        NOT_ENOUGH_XP("You do not have the required XP to make this bottle."),
        MAX_XP("You cannot withdraw this much xp!"),
        MIN_XP("You need to withdraw more xp!"),
        GAINED_XP("You have gained #99ff99%xp% &fexperience."),
        GIVEN_BOTTLE("You have been given a #99ff99%xp% XP Bottle"),

        RELOAD("You have reloaded XPBottles !"),
        NO_PERM("You do not have permission to do this."),
        INVALID_PLAYER("Please provide a correct player name."),
        INVALID_ARGS("Please use the correct command usage, %usage%"),
        INVALID_AMOUNT("&fPlease provide a valid number."),
        UNKNOWN_CMD("&fPlease include a valid command."),
        PLAYER_ONLY("&fOnly a player can execute this command."),
        CONSOLE_ONLY("&fOnly console can execute this command.");
    }


}