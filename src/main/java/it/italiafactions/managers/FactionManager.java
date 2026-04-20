package it.italiafactions.managers;

import it.italiafactions.models.Faction;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class FactionManager {

    private final JavaPlugin plugin;
    private final Map<String, Faction> factions = new HashMap<>(); // nome -> Faction
    private final Map<UUID, String> playerFaction = new HashMap<>(); // uuid -> nome fazione

    public FactionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadFactions();
    }

    public Faction getFaction(String name) {
        return factions.get(name.toLowerCase());
    }

    public Faction getFactionByPlayer(UUID uuid) {
        String name = playerFaction.get(uuid);
        if (name == null) return null;
        return factions.get(name);
    }

    public String getFactionNameByPlayer(UUID uuid) {
        return playerFaction.get(uuid);
    }

    public Collection<Faction> getAllFactions() {
        return factions.values();
    }

    public boolean factionExists(String name) {
        return factions.containsKey(name.toLowerCase());
    }

    public void createFaction(String name, UUID leaderUUID) {
        Faction f = new Faction(name);
        f.addMember(leaderUUID, Faction.Rank.BOSS);
        factions.put(name.toLowerCase(), f);
        playerFaction.put(leaderUUID, name.toLowerCase());
        saveFactions();
    }

    public void addPlayerToFaction(UUID uuid, String factionName, Faction.Rank rank) {
        playerFaction.put(uuid, factionName.toLowerCase());
        Faction f = getFaction(factionName);
        if (f != null) f.addMember(uuid, rank);
        saveFactions();
    }

    public void removePlayerFromFaction(UUID uuid) {
        String fname = playerFaction.remove(uuid);
        if (fname != null) {
            Faction f = factions.get(fname);
            if (f != null) f.removeMember(uuid);
        }
        saveFactions();
    }

    public boolean isPlayerInFaction(UUID uuid) {
        return playerFaction.containsKey(uuid);
    }

    public void saveFactions() {
        File file = new File(plugin.getDataFolder(), "factions.yml");
        YamlConfiguration config = new YamlConfiguration();

        for (Faction f : factions.values()) {
            String base = "factions." + f.getName();
            config.set(base + ".name", f.getName());
            config.set(base + ".barriereAttive", f.isBarriereAttive());
            config.set(base + ".territorySize", f.getTerritorySize());

            if (f.getSpawn() != null) {
                config.set(base + ".spawn.world", f.getSpawn().getWorld().getName());
                config.set(base + ".spawn.x", f.getSpawn().getX());
                config.set(base + ".spawn.y", f.getSpawn().getY());
                config.set(base + ".spawn.z", f.getSpawn().getZ());
            }

            if (f.getTerritoryCenter() != null) {
                config.set(base + ".territory.world", f.getTerritoryCenter().getWorld().getName());
                config.set(base + ".territory.x", f.getTerritoryCenter().getX());
                config.set(base + ".territory.y", f.getTerritoryCenter().getY());
                config.set(base + ".territory.z", f.getTerritoryCenter().getZ());
            }

            for (Map.Entry<UUID, Faction.Rank> entry : f.getMembers().entrySet()) {
                config.set(base + ".members." + entry.getKey().toString(), entry.getValue().name());
            }
        }

        try { config.save(file); } catch (Exception e) { e.printStackTrace(); }
    }

    @SuppressWarnings("unchecked")
    public void loadFactions() {
        File file = new File(plugin.getDataFolder(), "factions.yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.isConfigurationSection("factions")) return;

        for (String key : config.getConfigurationSection("factions").getKeys(false)) {
            String base = "factions." + key;
            String name = config.getString(base + ".name", key);
            Faction f = new Faction(name);
            f.setBarriereAttive(config.getBoolean(base + ".barriereAttive", true));
            f.setTerritorySize(config.getInt(base + ".territorySize", 0));

            if (config.isConfigurationSection(base + ".spawn")) {
                String world = config.getString(base + ".spawn.world");
                double x = config.getDouble(base + ".spawn.x");
                double y = config.getDouble(base + ".spawn.y");
                double z = config.getDouble(base + ".spawn.z");
                org.bukkit.World w = plugin.getServer().getWorld(world);
                if (w != null) f.setSpawn(new org.bukkit.Location(w, x, y, z));
            }

            if (config.isConfigurationSection(base + ".territory")) {
                String world = config.getString(base + ".territory.world");
                double x = config.getDouble(base + ".territory.x");
                double y = config.getDouble(base + ".territory.y");
                double z = config.getDouble(base + ".territory.z");
                org.bukkit.World w = plugin.getServer().getWorld(world);
                if (w != null) f.setTerritoryCenter(new org.bukkit.Location(w, x, y, z));
            }

            if (config.isConfigurationSection(base + ".members")) {
                for (String uuidStr : config.getConfigurationSection(base + ".members").getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        Faction.Rank rank = Faction.Rank.valueOf(config.getString(base + ".members." + uuidStr));
                        f.addMember(uuid, rank);
                        playerFaction.put(uuid, name.toLowerCase());
                    } catch (Exception ignored) {}
                }
            }

            factions.put(name.toLowerCase(), f);
        }
    }
}
