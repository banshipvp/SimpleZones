package local.simplezones;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ZoneListener implements Listener {

    private final ZoneManager manager;

    public ZoneListener(ZoneManager manager) {
        this.manager = manager;
    }

    // ── Block protection ──────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Zone zone = manager.getZoneAt(event.getBlock().getLocation());
        if (zone == null) return;
        Player player = event.getPlayer();
        if (player.hasPermission("simplezones.admin")) return; // admins bypass
        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "You cannot break blocks in a " + zone.getType().name().toLowerCase() + ".");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Zone zone = manager.getZoneAt(event.getBlock().getLocation());
        if (zone == null) return;
        Player player = event.getPlayer();
        if (player.hasPermission("simplezones.admin")) return;
        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "You cannot place blocks in a " + zone.getType().name().toLowerCase() + ".");
    }

    // ── Explosion protection ──────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> manager.getZoneAt(block.getLocation()) != null);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> manager.getZoneAt(block.getLocation()) != null);
    }

    // ── PvP rules ─────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Only handle Player-vs-Player
        Entity attacker = event.getDamager();
        Entity victim   = event.getEntity();
        if (!(attacker instanceof Player) || !(victim instanceof Player)) return;

        Player attackerP = (Player) attacker;
        Player victimP   = (Player) victim;

        Zone attackerZone = manager.getZoneAt(attackerP.getLocation());
        Zone victimZone   = manager.getZoneAt(victimP.getLocation());

        // Cancel PvP if either participant is in a safezone
        if (isSafezone(attackerZone) || isSafezone(victimZone)) {
            event.setCancelled(true);
            attackerP.sendMessage(ChatColor.RED + "PvP is disabled in a safezone.");
        }
        // Warzones do NOT cancel — PvP is explicitly allowed
    }

    private boolean isSafezone(Zone zone) {
        return zone != null && zone.getType() == Zone.ZoneType.SAFEZONE;
    }
}
