package it.italiafactions.listeners;

import it.italiafactions.ItaliaFactions;
import it.italiafactions.models.Faction;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class TerritoryListener implements Listener {

    private final ItaliaFactions plugin;

    public TerritoryListener(ItaliaFactions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Location to = e.getTo();
        if (to == null) return;

        // Admin bypass
        if (player.hasPermission("italiafactions.admin")) return;

        Faction playerFaction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());

        for (Faction faction : plugin.getFactionManager().getAllFactions()) {
            if (!faction.isBarriereAttive()) continue;
            if (faction.getTerritoryCenter() == null || faction.getTerritorySize() <= 0) continue;

            // Se il player è della stessa fazione, può entrare
            if (playerFaction != null && playerFaction.getName().equalsIgnoreCase(faction.getName())) continue;

            if (faction.isInTerritory(to)) {
                // Blocca il movimento
                e.setCancelled(true);
                player.sendMessage(ChatColor.RED + "⚠ Territorio della fazione " + faction.getName() + " - Non puoi entrare!");
                return;
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Location loc = e.getBlock().getLocation();

        if (player.hasPermission("italiafactions.admin")) return;

        Faction playerFaction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());

        for (Faction faction : plugin.getFactionManager().getAllFactions()) {
            if (!faction.isBarriereAttive()) continue;
            if (faction.isInTerritory(loc)) {
                if (playerFaction == null || !playerFaction.getName().equalsIgnoreCase(faction.getName())) {
                    e.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Non puoi rompere blocchi nel territorio di " + faction.getName() + "!");
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Location loc = e.getBlock().getLocation();

        if (player.hasPermission("italiafactions.admin")) return;

        Faction playerFaction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());

        for (Faction faction : plugin.getFactionManager().getAllFactions()) {
            if (!faction.isBarriereAttive()) continue;
            if (faction.isInTerritory(loc)) {
                if (playerFaction == null || !playerFaction.getName().equalsIgnoreCase(faction.getName())) {
                    e.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Non puoi piazzare blocchi nel territorio di " + faction.getName() + "!");
                    return;
                }
            }
        }
    }
}
