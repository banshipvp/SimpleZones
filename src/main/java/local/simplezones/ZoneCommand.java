package local.simplezones;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ZoneCommand implements CommandExecutor, TabCompleter {

    private static final String PREFIX = ChatColor.GOLD + "[Zones] " + ChatColor.RESET;
    private final ZoneManager manager;

    public ZoneCommand(ZoneManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        if (!player.hasPermission("simplezones.admin")) {
            player.sendMessage(PREFIX + ChatColor.RED + "You don't have permission.");
            return true;
        }
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(player, args);
            case "delete" -> handleDelete(player, args);
            case "list"   -> handleList(player);
            case "info"   -> handleInfo(player, args);
            default       -> sendHelp(player);
        }
        return true;
    }

    // ── Subcommands ───────────────────────────────────────────────────────────

    private void handleCreate(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(PREFIX + ChatColor.YELLOW + "Usage: /zone create <name> <safezone|warzone>");
            return;
        }
        String name = args[1];
        Zone.ZoneType type;
        try {
            type = Zone.ZoneType.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(PREFIX + ChatColor.RED + "Invalid type. Use SAFEZONE or WARZONE.");
            return;
        }

        if (manager.getZone(name) != null) {
            player.sendMessage(PREFIX + ChatColor.RED + "A zone named '" + name + "' already exists.");
            return;
        }

        // Get WorldEdit selection
        try {
            LocalSession session = WorldEdit.getInstance()
                    .getSessionManager()
                    .get(BukkitAdapter.adapt(player));
            Region region = session.getSelection(BukkitAdapter.adapt(player.getWorld()));
            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();

            Zone zone = new Zone(
                    name, type,
                    player.getWorld().getName(),
                    min.getX(), min.getY(), min.getZ(),
                    max.getX(), max.getY(), max.getZ()
            );
            manager.addZone(zone);

            player.sendMessage(PREFIX + ChatColor.GREEN + "Zone '" + name + "' created as "
                    + type.name() + " (" + region.getVolume() + " blocks).");
        } catch (IncompleteRegionException e) {
            player.sendMessage(PREFIX + ChatColor.RED + "You need to make a WorldEdit selection first (use //pos1 and //pos2).");
        } catch (Exception e) {
            player.sendMessage(PREFIX + ChatColor.RED + "Error reading selection: " + e.getMessage());
        }
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(PREFIX + ChatColor.YELLOW + "Usage: /zone delete <name>");
            return;
        }
        String name = args[1];
        if (manager.removeZone(name)) {
            player.sendMessage(PREFIX + ChatColor.GREEN + "Zone '" + name + "' deleted.");
        } else {
            player.sendMessage(PREFIX + ChatColor.RED + "No zone named '" + name + "' found.");
        }
    }

    private void handleList(Player player) {
        var zones = manager.getAllZones();
        if (zones.isEmpty()) {
            player.sendMessage(PREFIX + ChatColor.YELLOW + "No zones defined.");
            return;
        }
        player.sendMessage(PREFIX + ChatColor.GOLD + "Zones (" + zones.size() + "):");
        for (Zone z : zones) {
            ChatColor color = z.getType() == Zone.ZoneType.SAFEZONE ? ChatColor.GREEN : ChatColor.RED;
            player.sendMessage(ChatColor.GRAY + "  - " + color + z.getName()
                    + ChatColor.GRAY + " [" + z.getType().name() + "] " + z.getWorld());
        }
    }

    private void handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(PREFIX + ChatColor.YELLOW + "Usage: /zone info <name>");
            return;
        }
        Zone z = manager.getZone(args[1]);
        if (z == null) {
            player.sendMessage(PREFIX + ChatColor.RED + "No zone named '" + args[1] + "' found.");
            return;
        }
        ChatColor color = z.getType() == Zone.ZoneType.SAFEZONE ? ChatColor.GREEN : ChatColor.RED;
        player.sendMessage(color + "=== " + z.getName() + " ===");
        player.sendMessage(ChatColor.GRAY + "Type:  " + color + z.getType().name());
        player.sendMessage(ChatColor.GRAY + "World: " + ChatColor.WHITE + z.getWorld());
        player.sendMessage(ChatColor.GRAY + "Min:   " + ChatColor.WHITE + z.getMinX() + ", " + z.getMinY() + ", " + z.getMinZ());
        player.sendMessage(ChatColor.GRAY + "Max:   " + ChatColor.WHITE + z.getMaxX() + ", " + z.getMaxY() + ", " + z.getMaxZ());
    }

    private void sendHelp(Player player) {
        player.sendMessage(PREFIX + ChatColor.GOLD + "Zone Commands:");
        player.sendMessage(ChatColor.YELLOW + "  /zone create <name> <safezone|warzone>" + ChatColor.GRAY + " - Create from WorldEdit selection");
        player.sendMessage(ChatColor.YELLOW + "  /zone delete <name>" + ChatColor.GRAY + " - Delete a zone");
        player.sendMessage(ChatColor.YELLOW + "  /zone list" + ChatColor.GRAY + " - List all zones");
        player.sendMessage(ChatColor.YELLOW + "  /zone info <name>" + ChatColor.GRAY + " - Show zone details");
    }

    // ── Tab completion ────────────────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!sender.hasPermission("simplezones.admin")) return List.of();
        if (args.length == 1) {
            return Arrays.asList("create", "delete", "list", "info").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("info"))) {
            return manager.getAllZones().stream()
                    .map(Zone::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            return Arrays.asList("safezone", "warzone").stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
