package xyz.oribuin.xpbottles.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import xyz.oribuin.xpbottles.XPBottlePlugin;

import java.util.UUID;

public class PlayerListeners implements Listener {

    private final XPBottlePlugin plugin;

    public PlayerListeners(@NotNull XPBottlePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the player throwing the xp bottle
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onThrow(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() == null) return;

        final ItemStack item = event.getItem();
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        final Integer xpAmount = meta.getPersistentDataContainer().get(
                this.plugin.getXPKey(),
                PersistentDataType.INTEGER
        );

        if (xpAmount == null) return;

        event.setCancelled(true);
        this.plugin.setXP(event.getPlayer(), event.getPlayer().getTotalExperience() + xpAmount);

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            event.getPlayer().getInventory().setItem(event.getHand(), null);
        }

        // TODO: Send message to player
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) return;

        final PersistentDataContainer container = event.getItem().getPersistentDataContainer();
        final String owner = container.get(this.plugin.getOwnerKey(), PersistentDataType.STRING);
        if (owner == null) return;

        if (!event.getEntity().getUniqueId().equals(UUID.fromString(owner))) {
            event.setCancelled(true);
        }

    }

}
