package it.italiafactions.listeners;

import it.italiafactions.ItaliaFactions;
import it.italiafactions.gui.GUIManager;
import it.italiafactions.models.Faction;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

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
        String title = e.getView().getTitle();

        // === CASSA ===
        if (GUIManager.openCassa.containsKey(uuid)) {
            // Permettiamo libera interazione nella cassa
            return;
        }

        // === BOSS MENU PRINCIPALE ===
        if (title.startsWith(ChatColor.GOLD + "⚔ Menu Boss")) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;
            String factionName = GUIManager.openBossMenu.get(uuid);
            if (factionName == null) return;
            Faction faction = plugin.getFactionManager().getFaction(factionName);
            if (faction == null) return;

            int slot = e.getSlot();
            if (slot == 11) {
                // Gestisci Membri
                player.closeInventory();
                plugin.getGUIManager().openMembriMenu(player, faction);
            } else if (slot == 13) {
                // Apri Cassa
                player.closeInventory();
                plugin.getGUIManager().openCassa(player, faction);
            }
            return;
        }

        // === LISTA MEMBRI ===
        if (title.startsWith(ChatColor.YELLOW + "Membri")) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;
            String factionName = GUIManager.openBossMenu.get(uuid);
            if (factionName == null) return;
            Faction faction = plugin.getFactionManager().getFaction(factionName);
            if (faction == null) return;

            // Recupera UUID dal nome item (nascosto con ChatColor.BLACK)
            String displayName = e.getCurrentItem().getItemMeta() != null ?
                    e.getCurrentItem().getItemMeta().getDisplayName() : "";
            String uuidStr = ChatColor.stripColor(displayName);
            // L'UUID è negli ultimi 36 caratteri
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
        if (title.startsWith(ChatColor.RED + "Gestisci:")) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;
            String factionName = GUIManager.openBossMenu.get(uuid);
            if (factionName == null) return;
            Faction faction = plugin.getFactionManager().getFaction(factionName);
            if (faction == null) return;
            UUID targetUUID = GUIManager.managingPlayer.get(uuid);
            if (targetUUID == null) return;

            int slot = e.getSlot();
            if (slot == 10) {
                // Promuovi
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
                // Retrocedi
                Faction.Rank current = faction.getRank(targetUUID);
                if (current == Faction.Rank.UFFICIALE) {
                    faction.setRank(targetUUID, Faction.Rank.SOLDATO);
                    player.sendMessage(ChatColor.YELLOW + "Giocatore retrocesso a Soldato!");
                }
                plugin.getFactionManager().saveFactions();
                player.closeInventory();
            } else if (slot == 16) {
                // Licenzia
                plugin.getFactionManager().removePlayerFromFaction(targetUUID);
                player.sendMessage(ChatColor.RED + "Giocatore rimosso dalla fazione!");
                // Notifica al giocatore rimosso se online
                Player target = plugin.getServer().getPlayer(targetUUID);
                if (target != null) {
                    target.sendMessage(ChatColor.RED + "Sei stato rimosso dalla fazione " + faction.getName() + "!");
                }
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();

        // Salva cassa se era aperta
        if (GUIManager.openCassa.containsKey(uuid)) {
            String factionName = GUIManager.openCassa.remove(uuid);
            Faction faction = plugin.getFactionManager().getFaction(factionName);
            if (faction != null) {
                faction.setCassaContents(e.getInventory().getContents());
                plugin.getFactionManager().saveFactions();
            }
        }

        GUIManager.openBossMenu.remove(uuid);
        GUIManager.managingPlayer.remove(uuid);
    }
}
