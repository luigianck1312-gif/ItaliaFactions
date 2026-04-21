package it.italiafactions.listeners;

import it.italiafactions.ItaliaFactions;
import it.italiafactions.gui.GUIManager;
import it.italiafactions.models.Faction;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Collection;
import java.util.UUID;

public class GUIListener implements Listener {

    private final ItaliaFactions plugin;

    public GUIListener(ItaliaFactions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();
        String guiType = GUIManager.openGUI.get(uuid);
        if (guiType == null) return;

        // === CASSA — libera interazione ===
        if (guiType.equals("cassa")) return;

        e.setCancelled(true);
        if (e.getCurrentItem() == null) return;

        int slot = e.getSlot();

        // === BOSS MENU PRINCIPALE ===
        if (guiType.equals("bossmenu")) {
            String factionName = GUIManager.openBossMenu.get(uuid);
            if (factionName == null) return;
            Faction faction = plugin.getFactionManager().getFaction(factionName);
            if (faction == null) return;

            if (slot == 11) {
                player.closeInventory();
                plugin.getGUIManager().openMembriMenu(player, faction);
            } else if (slot == 13) {
                player.closeInventory();
                plugin.getGUIManager().openCassa(player, faction);
            } else if (slot == 20) {
                // Assumi player vicino
                player.closeInventory();
                assumiPlayerVicino(player, faction);
            }
            return;
        }

        // === LISTA MEMBRI ===
        if (guiType.equals("membrimenu")) {
            String factionName = GUIManager.openBossMenu.get(uuid);
            if (factionName == null) return;
            Faction faction = plugin.getFactionManager().getFaction(factionName);
            if (faction == null) return;

            if (slot == 49) {
                // Torna al menu boss
                player.closeInventory();
                plugin.getGUIManager().openBossMenu(player, faction);
                return;
            }

            String displayName = e.getCurrentItem().getItemMeta() != null ?
                    e.getCurrentItem().getItemMeta().getDisplayName() : "";
            String uuidStr = ChatColor.stripColor(displayName);
            if (uuidStr.length() >= 36) {
                String uuidPart = uuidStr.substring(uuidStr.length() - 36);
                try {
                    UUID targetUUID = UUID.fromString(uuidPart);
                    player.closeInventory();
                    plugin.getGUIManager().openGestisciMembro(player, faction, targetUUID);
                } catch (Exception ignored) {}
            }
            return;
        }

        // === GESTISCI MEMBRO ===
        if (guiType.equals("gestiscimembro")) {
            String factionName = GUIManager.openBossMenu.get(uuid);
            if (factionName == null) return;
            Faction faction = plugin.getFactionManager().getFaction(factionName);
            if (faction == null) return;
            UUID targetUUID = GUIManager.managingPlayer.get(uuid);
            if (targetUUID == null) return;

            if (slot == 22) {
                // Torna ai membri
                player.closeInventory();
                plugin.getGUIManager().openMembriMenu(player, faction);
                return;
            }

            if (slot == 10) {
                Faction.Rank current = faction.getRank(targetUUID);
                if (current == Faction.Rank.SOLDATO) {
                    faction.setRank(targetUUID, Faction.Rank.UFFICIALE);
                    player.sendMessage(ChatColor.GREEN + "Giocatore promosso a Ufficiale!");
                } else if (current == Faction.Rank.UFFICIALE) {
                    faction.setRank(targetUUID, Faction.Rank.BOSS);
                    player.sendMessage(ChatColor.GREEN + "Giocatore promosso a Boss!");
                }
                plugin.getFactionManager().saveFactions();
                player.closeInventory();
            } else if (slot == 13) {
                Faction.Rank current = faction.getRank(targetUUID);
                if (current == Faction.Rank.UFFICIALE) {
                    faction.setRank(targetUUID, Faction.Rank.SOLDATO);
                    player.sendMessage(ChatColor.YELLOW + "Giocatore retrocesso a Soldato!");
                }
                plugin.getFactionManager().saveFactions();
                player.closeInventory();
            } else if (slot == 16) {
                plugin.getFactionManager().removePlayerFromFaction(targetUUID);
                player.sendMessage(ChatColor.RED + "Giocatore rimosso dalla fazione!");
                Player target = plugin.getServer().getPlayer(targetUUID);
                if (target != null) {
                    target.sendMessage(ChatColor.RED + "Sei stato rimosso dalla fazione " + faction.getName() + "!");
                }
                player.closeInventory();
            }
            return;
        }

        // === MAPPA TERRITORI ===
        if (guiType.equals("mappa")) {
            if (slot == 53) {
                player.closeInventory();
            }
            return;
        }
    }

    private void assumiPlayerVicino(Player boss, Faction faction) {
        Collection<Entity> nearby = boss.getWorld().getNearbyEntities(boss.getLocation(), 5, 5, 5);
        Player nearest = null;
        double minDist = Double.MAX_VALUE;

        for (Entity entity : nearby) {
            if (!(entity instanceof Player target)) continue;
            if (target.equals(boss)) continue;
            if (plugin.getFactionManager().isPlayerInFaction(target.getUniqueId())) continue;

            double dist = target.getLocation().distance(boss.getLocation());
            if (dist < minDist) {
                minDist = dist;
                nearest = target;
            }
        }

        if (nearest == null) {
            boss.sendMessage(ChatColor.RED + "Nessun player disponibile nelle vicinanze (raggio 5 blocchi)!");
            boss.sendMessage(ChatColor.GRAY + "Il player deve essere vicino e non essere in un'altra fazione.");
            return;
        }

        plugin.getFactionManager().addPlayerToFaction(nearest.getUniqueId(), faction.getName(), Faction.Rank.SOLDATO);
        boss.sendMessage(ChatColor.GREEN + nearest.getName() + " e' stato assunto nella fazione come Soldato!");
        nearest.sendMessage(ChatColor.GOLD + "Sei stato assunto nella fazione " + faction.getName() + " come Soldato!");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();

        if (GUIManager.openCassa.containsKey(uuid)) {
            String factionName = GUIManager.openCassa.remove(uuid);
            Faction faction = plugin.getFactionManager().getFaction(factionName);
            if (faction != null) {
                faction.setCassaContents(e.getInventory().getContents());
                plugin.getFactionManager().saveFactions();
            }
        }

        GUIManager.openGUI.remove(uuid);
        GUIManager.openBossMenu.remove(uuid);
        GUIManager.managingPlayer.remove(uuid);
    }
}
