package it.italiafactions.models;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Faction {

    public enum Rank {
        BOSS, UFFICIALE, SOLDATO
    }

    private String name;
    private Map<UUID, Rank> members = new HashMap<>(); // UUID -> Rank
    private Location spawn;
    private Location territoryCenter;
    private int territorySize; // dimensione lato (es. 64 = 64x64)
    private boolean barriereAttive = true;
    private ItemStack[] cassaContents = new ItemStack[27]; // cassa normale = 27 slot

    public Faction(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public Map<UUID, Rank> getMembers() { return members; }

    public void addMember(UUID uuid, Rank rank) { members.put(uuid, rank); }

    public void removeMember(UUID uuid) { members.remove(uuid); }

    public boolean hasMember(UUID uuid) { return members.containsKey(uuid); }

    public Rank getRank(UUID uuid) { return members.get(uuid); }

    public void setRank(UUID uuid, Rank rank) { members.put(uuid, rank); }

    public Location getSpawn() { return spawn; }
    public void setSpawn(Location spawn) { this.spawn = spawn; }

    public Location getTerritoryCenter() { return territoryCenter; }
    public void setTerritoryCenter(Location center) { this.territoryCenter = center; }

    public int getTerritorySize() { return territorySize; }
    public void setTerritorySize(int size) { this.territorySize = size; }

    public boolean isBarriereAttive() { return barriereAttive; }
    public void setBarriereAttive(boolean v) { this.barriereAttive = v; }

    public ItemStack[] getCassaContents() { return cassaContents; }
    public void setCassaContents(ItemStack[] contents) { this.cassaContents = contents; }

    public boolean isInTerritory(Location loc) {
        if (territoryCenter == null || territorySize <= 0) return false;
        if (!loc.getWorld().equals(territoryCenter.getWorld())) return false;
        double half = territorySize / 2.0;
        double dx = Math.abs(loc.getX() - territoryCenter.getX());
        double dz = Math.abs(loc.getZ() - territoryCenter.getZ());
        return dx <= half && dz <= half;
    }
}
