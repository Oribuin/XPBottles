package xyz.oribuin.xpbottles;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import xyz.oribuin.xpbottles.command.BottleCommand;
import xyz.oribuin.xpbottles.listener.PlayerListeners;
import xyz.oribuin.xpbottles.util.HexUtils;
import xyz.oribuin.xpbottles.util.StringPlaceholders;

import java.util.List;
import java.util.stream.Collectors;

public class XPBottlePlugin extends JavaPlugin {

    private final NamespacedKey xpKey = new NamespacedKey(this, "xp");
    private final NamespacedKey ownerKey = new NamespacedKey(this, "owner");

    // Cached config values.
    private String name; // The name of the bottle
    private List<String> lore; // The lore of the bottle

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.reload();

        // Register Command
        new BottleCommand(this);

        // Register Listeners
        this.getServer().getPluginManager().registerEvents(new PlayerListeners(this), this);
    }

    /**
     * Reload the config
     */
    public void reload() {
        this.reloadConfig();

        this.name = this.getConfig().getString("name");
        this.lore = this.getConfig().getStringList("lore");
    }

    /**
     * Get the amount of xp for the level
     *
     * @param level The level
     * @return The amount of xp
     */
    public int getXPForLevel(int level) {
        if (level <= 15)
            return 2 * level + 7;

        if (level <= 30)
            return 5 * level - 38;

        return 9 * level - 158;
    }

    /**
     * Get the players total experience
     *
     * @param player The player
     * @return The total xp
     */
    public int getTotalXP(@NotNull Player player) {
        int exp = Math.round(this.getXPForLevel(player.getLevel()) * player.getExp());
        int currentLevel = player.getLevel();

        while (currentLevel > 0) {
            currentLevel--;
            exp += this.getXPForLevel(currentLevel);
        }

        return exp;
    }

    /**
     * Set the players total experience
     *
     * @param player The player
     * @param toGive The amount of xp to give
     */
    public void setXP(@NotNull Player player, int toGive) {
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);

        int amount = toGive;
        while (amount < 0) {
            final int expToLevel = getXPForLevel(player.getLevel());
            final int remainder = expToLevel - amount;
            amount -= expToLevel;
            if (amount < 0) {
                player.giveExp(expToLevel);
            } else {
                player.giveExp(remainder);
            }
        }
    }

    /**
     * Create a bottle with the given xp and owner
     *
     * @param owner The owner of the bottle
     * @param xp    The amount of xp in the bottle
     * @return The bottle
     */
    @NotNull
    public ItemStack createBottle(@NotNull CommandSender owner, int xp) {
        final ItemStack bottle = new ItemStack(Material.EXPERIENCE_BOTTLE);
        final ItemMeta meta = bottle.getItemMeta();
        if (meta == null) return new ItemStack(Material.AIR); // will never happen

        final StringPlaceholders placeholders = StringPlaceholders.of(
                "owner", owner.getName(),
                "xp", String.valueOf(xp)
        );

        final PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(xpKey, PersistentDataType.INTEGER, xp);

        meta.setDisplayName(HexUtils.colorify(placeholders.apply(this.name)));
        meta.setLore(
                this.lore.stream()
                        .map(s -> HexUtils.colorify(placeholders.apply(s)))
                        .collect(Collectors.toList())
        );

        bottle.setItemMeta(meta);
        return bottle;
    }

    public NamespacedKey getXPKey() {
        return xpKey;
    }

    public NamespacedKey getOwnerKey() {
        return ownerKey;
    }

}
