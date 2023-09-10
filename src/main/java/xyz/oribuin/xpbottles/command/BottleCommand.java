package xyz.oribuin.xpbottles.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.oribuin.xpbottles.XPBottlePlugin;
import xyz.oribuin.xpbottles.util.HexUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BottleCommand implements TabExecutor {

    private final XPBottlePlugin plugin;

    public BottleCommand(@NotNull XPBottlePlugin plugin) {
        this.plugin = plugin;

        final PluginCommand command = this.plugin.getCommand("bottle");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Invalid usage
        if (args.length == 0) {
            sender.sendMessage(HexUtils.colorify(this.plugin.getConfig().getString("invalid-args")));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                this.reload(sender);
                break;

            case "give":
                this.give(sender, args);
                break;

            case "max":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(HexUtils.colorify(this.plugin.getConfig().getString("player-only")));
                    return true;
                }

                this.bottle(sender, Math.min((this.plugin.getTotalXP((Player) sender)), this.plugin.getConfig().getInt("max-xp")));
            default:

                if (!(sender instanceof Player)) {
                    sender.sendMessage(HexUtils.colorify(this.plugin.getConfig().getString("player-only")));
                    return true;
                }

                int amount;

                try {
                    amount = Integer.parseInt(args[0]);
                } catch (NumberFormatException ex) {
                    sender.sendMessage(HexUtils.colorify(this.plugin.getConfig().getString("invalid-amount")));
                    return true;
                }

                this.bottle(sender, amount);
                break;
        }

        return true;
    }

    /**
     * Create a bottle with the given xp and owner
     *
     * @param sender The player
     * @param amount The amount of xp to give
     */
    private void bottle(CommandSender sender, int amount) {
        if (!sender.hasPermission("xpbottles.bottle")) {
            sender.sendMessage(HexUtils.colorify(this.plugin.getConfig().getString("no-permission")));
            return;
        }

        final Player player = (Player) sender;
        final ItemStack result = this.plugin.createBottle(sender, amount);

        // Make sure amount is valid
        if (amount < this.plugin.getConfig().getInt("min-xp") || amount > this.plugin.getConfig().getInt("max-xp")) {
            sender.sendMessage(HexUtils.colorify(this.plugin.getConfig().getString("invalid-amount")));
            return;
        }

        int playerXP = this.plugin.getTotalXP(player);
        if (amount > playerXP) {
            sender.sendMessage(HexUtils.colorify(this.plugin.getConfig().getString("not-enough-xp")));
            return;
        }

        // Inventory is full
        if (player.getInventory().firstEmpty() == -1) {
            sender.sendMessage(HexUtils.colorify(this.plugin.getConfig().getString("full-inventory")));
            player.getWorld().dropItem(player.getLocation(), result,
                    item -> item.getPersistentDataContainer()
                            .set(
                                    this.plugin.getOwnerKey(),
                                    PersistentDataType.STRING,
                                    player.getUniqueId().toString()
                            ));

            return;
        }

        // give bottle ^.^
        player.getInventory().addItem(result);
    }

    /**
     * Give command for the plugin
     *
     * @param sender The command sender
     * @param args   The command arguments
     */
    private void give(CommandSender sender, String[] args) {

        // no perms :(
        if (!sender.hasPermission("xpbottles.give")) {
            sender.sendMessage(HexUtils.colorify(this.plugin.getConfig().getString("no-permission")));
            return;
        }

        // invalid usage
        if (args.length < 3) {
            sender.sendMessage(HexUtils.colorify(this.plugin.getConfig().getString("invalid-args")));
            return;
        }

        // invalid player
        final Player player = this.plugin.getServer().getPlayer(args[1]);
        if (player == null) {
            sender.sendMessage(HexUtils.colorify(this.plugin.getConfig().getString("invalid-player")));
            return;
        }

        // invalid amount
        int xpAmount;
        try {
            xpAmount = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(HexUtils.colorify(this.plugin.getConfig().getString("invalid-args")));
            return;
        }

        // invalid amount 2 electric boogaloo
        if (xpAmount <= 0) {
            sender.sendMessage(HexUtils.colorify(this.plugin.getConfig().getString("invalid-args")));
            return;
        }

        final ItemStack result = this.plugin.createBottle(player, xpAmount);

        // Inventory is full
        if (player.getInventory().firstEmpty() == -1) {
            sender.sendMessage(HexUtils.colorify(this.plugin.getConfig().getString("full-inventory")));

            // Drop the bottle on the floor
            player.getWorld().dropItem(player.getLocation(), result,
                    item -> item.getPersistentDataContainer()
                            .set(
                                    this.plugin.getOwnerKey(),
                                    PersistentDataType.STRING,
                                    player.getUniqueId().toString()
                            ));
            return;
        }

        // give bottle ^.^
        player.getInventory().addItem(result);
    }

    /**
     * Reload command for the plugin
     *
     * @param sender The command sender
     */
    private void reload(CommandSender sender) {

        if (!sender.hasPermission("xpbottles.reload")) {
            sender.sendMessage(HexUtils.colorify(this.plugin.getConfig().getString("no-permission")));
            return;
        }

        this.plugin.reload();
        sender.sendMessage(HexUtils.colorify(this.plugin.getConfig().getString("reload")));
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // TODO: Improve tab completion
        return Arrays.asList("reload", "give", "max", "<amount>");
    }

}
