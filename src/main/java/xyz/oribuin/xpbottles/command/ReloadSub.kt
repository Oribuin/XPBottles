package xyz.oribuin.xpbottles.command

import org.bukkit.command.CommandSender
import xyz.oribuin.orilibrary.command.SubCommand
import xyz.oribuin.xpbottles.XPBottles
import xyz.oribuin.xpbottles.manager.MessageManager

@SubCommand.Info(
    names = ["reload"],
    usage = "/bottles reload",
    permission = "xpbottles.reload"
)
class ReloadSub(private val plugin: XPBottles, cmd: BottleCommand) : SubCommand(plugin, cmd) {

    private val msg = this.plugin.getManager(MessageManager::class.java)

    override fun executeArgument(sender: CommandSender, args: Array<String>) {

        this.plugin.reload()
        this.msg.send(sender, "reload")
    }
}