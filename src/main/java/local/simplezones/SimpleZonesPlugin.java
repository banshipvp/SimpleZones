package local.simplezones;

import org.bukkit.plugin.java.JavaPlugin;

public class SimpleZonesPlugin extends JavaPlugin {

    private ZoneManager zoneManager;

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();
        zoneManager = new ZoneManager(getDataFolder());

        // Register commands
        ZoneCommand zoneCmd = new ZoneCommand(zoneManager);
        var cmd = getCommand("zone");
        if (cmd != null) {
            cmd.setExecutor(zoneCmd);
            cmd.setTabCompleter(zoneCmd);
        }

        // Register listeners
        getServer().getPluginManager().registerEvents(new ZoneListener(zoneManager), this);

        getLogger().info("SimpleZones enabled — " + zoneManager.getAllZones().size() + " zone(s) loaded.");
    }

    @Override
    public void onDisable() {
        getLogger().info("SimpleZones disabled.");
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }
}
