package local.simplezones;

import org.bukkit.Location;

public class Zone {

    public enum ZoneType {
        SAFEZONE,
        WARZONE
    }

    private final String name;
    private final ZoneType type;
    private final String world;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;

    public Zone(String name, ZoneType type, String world,
                int minX, int minY, int minZ,
                int maxX, int maxY, int maxZ) {
        this.name = name;
        this.type = type;
        this.world = world;
        this.minX = minX; this.minY = minY; this.minZ = minZ;
        this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
    }

    public String getName()    { return name; }
    public ZoneType getType()  { return type; }
    public String getWorld()   { return world; }

    public boolean contains(Location loc) {
        if (loc.getWorld() == null || !loc.getWorld().getName().equals(world)) return false;
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        return x >= minX && x <= maxX
            && y >= minY && y <= maxY
            && z >= minZ && z <= maxZ;
    }

    // Getters for serialization
    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }
}
