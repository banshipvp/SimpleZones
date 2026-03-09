package local.simplezones;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ZoneManager {

    private final File dataFile;
    private final Map<String, Zone> zones = new HashMap<>();

    public ZoneManager(File dataFolder) {
        this.dataFile = new File(dataFolder, "zones.yml");
        load();
    }

    public void addZone(Zone zone) {
        zones.put(zone.getName().toLowerCase(), zone);
        save();
    }

    public boolean removeZone(String name) {
        boolean removed = zones.remove(name.toLowerCase()) != null;
        if (removed) save();
        return removed;
    }

    public Zone getZone(String name) {
        return zones.get(name.toLowerCase());
    }

    public Collection<Zone> getAllZones() {
        return zones.values();
    }

    /** Returns the first zone that contains this location, or null. */
    public Zone getZoneAt(Location loc) {
        for (Zone z : zones.values()) {
            if (z.contains(loc)) return z;
        }
        return null;
    }

    // ── Persistence ──────────────────────────────────────────────────────────

    private void load() {
        if (!dataFile.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection sec = cfg.getConfigurationSection("zones");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            ConfigurationSection zs = sec.getConfigurationSection(key);
            if (zs == null) continue;
            try {
                Zone.ZoneType type = Zone.ZoneType.valueOf(zs.getString("type", "SAFEZONE").toUpperCase());
                Zone z = new Zone(
                        key,
                        type,
                        zs.getString("world", "world"),
                        zs.getInt("minX"), zs.getInt("minY"), zs.getInt("minZ"),
                        zs.getInt("maxX"), zs.getInt("maxY"), zs.getInt("maxZ")
                );
                zones.put(key.toLowerCase(), z);
            } catch (Exception e) {
                // skip malformed entry
            }
        }
    }

    private void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        for (Zone z : zones.values()) {
            String key = "zones." + z.getName();
            cfg.set(key + ".type",  z.getType().name());
            cfg.set(key + ".world", z.getWorld());
            cfg.set(key + ".minX",  z.getMinX());
            cfg.set(key + ".minY",  z.getMinY());
            cfg.set(key + ".minZ",  z.getMinZ());
            cfg.set(key + ".maxX",  z.getMaxX());
            cfg.set(key + ".maxY",  z.getMaxY());
            cfg.set(key + ".maxZ",  z.getMaxZ());
        }
        try {
            cfg.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
