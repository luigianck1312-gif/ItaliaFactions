package it.italiafactions.gui;

import it.italiafactions.ItaliaFactions;
import it.italiafactions.models.Faction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GUIManager {

    private final ItaliaFactions plugin;

    // Tracciamo chi ha aperto quale GUI
    public static final Map<UUID, String> openCassa = new HashMap<>(); // uuid -> faction name
    public static final Map<UUID, String> openBossMenu = new HashMap<>(); // uuid -> faction name
    public static final Map<UUID, UUID> managingPlayer = new HashMap<>(); // boss uuid -> target uuid

    public GUIManager(ItaliaFactions plugin) {
        this.plugin = plugin;
    }

    public void openBossMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "⚔ Menu Boss - " + faction.getName());

        // Slot 11: Gestisci Membri
        ItemStack membri = createItem(Material.PLAYER_HEAD, ChatColor.YELLOW + "Gestisci Membri",
                Arrays.asList(ChatColor.GRAY + "Assumi, licenzia o promuovi membri"));
        inv.setItem(11, membri);

        // Slot 13: Apri Cassa
        ItemStack cassa = createItem(Material.CHEST, ChatColor.GOLD + "Cassa della Fazione",
                Arrays.asList(ChatColor.GRAY + "Apri il deposito condiviso"));
        inv.setItem(13, cassa);

        // Slot 15: Info Fazione
        ItemStack info = createItem(Material.BOOK, ChatColor.AQUA + "Info Fazione",
                Arrays.asList(
                        ChatColor.GRAY + "Nome: " + ChatColor.WHITE + faction.getName(),
                        ChatColor.GRAY + "Membri: " + ChatColor.WHITE + faction.getMembers().size(),
                        ChatColor.GRAY + "Barriere: " + (faction.isBarriereAttive() ? ChatColor.GREEN + "Attive" : ChatColor.RED + "Disattive")
                ));
        inv.setItem(15, info);

        openBossMenu.put(player.getUniqueId(), faction.getName().toLowerCase());
        player.openInventory(inv);
    }

    public void openMembriMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.YELLOW + "Membri - " + faction.getName());

        int slot = 0;
        for (Map.Entry<UUID, Faction.Rank> entry : faction.getMembers().entrySet()) {
            if (slot >= 54) break;
            UUID memberUUID = entry.getKey();
            Faction.Rank rank = entry.getValue();

            // Non mostrare se stesso
            if (memberUUID.equals(player.getUniqueId())) continue;

            String memberName = Bukkit.getOfflinePlayer(memberUUID).getName();
            if (memberName == null) memberName = memberUUID.toString().substring(0, 8);

            ChatColor rankColor = rank == Faction.Rank.BOSS ? ChatColor.RED :
                    rank == Faction.Rank.UFFICIALE ? ChatColor.GOLD : ChatColor.GRAY;

            ItemStack item = createItem(Material.PLAYER_HEAD,
                    rankColor + memberName,
                    Arrays.asList(
                            ChatColor.GRAY + "Ruolo: " + rankColor + rank.name(),
                            ChatColor.YELLOW + "Clicca per gestire"
                    ));

            // Salva UUID nel nome per recuperarlo dopo
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(rankColor + memberName + ChatColor.BLACK + "" + memberUUID);
                item.setItemMeta(meta);
            }

            inv.setItem(slot++, item);
        }

        openBossMenu.put(player.getUniqueId(), faction.getName().toLowerCase());
        player.openInventory(inv);
    }

    public void openGestisciMembro(Player boss, Faction faction, UUID targetUUID) {
        String targetName = Bukkit.getOfflinePlayer(targetUUID).getName();
        if (targetName == null) targetName = "Sconosciuto";

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.RED + "Gestisci: " + targetName);

        Faction.Rank currentRank = faction.getRank(targetUUID);

        // Slot 10: Promuovi
        if (currentRank == Faction.Rank.SOLDATO) {
            inv.setItem(10, createItem(Material.DIAMOND, ChatColor.GREEN + "Promuovi a Ufficiale",
                    Collections.singletonList(ChatColor.GRAY + "Promuovi " + targetName)));
        } else if (currentRank == Faction.Rank.UFFICIALE) {
            inv.setItem(10, createItem(Material.EMERALD, ChatColor.GREEN + "Promuovi a Boss",
                    Collections.singletonList(ChatColor.GRAY + "Promuovi " + targetName)));
        }

        // Slot 13: Retrocedi
        if (currentRank == Faction.Rank.UFFICIALE) {
            inv.setItem(13, createItem(Material.IRON_INGOT, ChatColor.YELLOW + "Retrocedi a Soldato",
                    Collections.singletonList(ChatColor.GRAY + "Retrocedi " + targetName)));
        }

        // Slot 16: Licenzia
        inv.setItem(16, createItem(Material.BARRIER, ChatColor.RED + "Licenzia",
                Collections.singletonList(ChatColor.GRAY + "Rimuovi " + targetName + " dalla fazione")));

        managingPlayer.put(boss.getUniqueId(), targetUUID);
        openBossMenu.put(boss.getUniqueId(), faction.getName().toLowerCase());
        boss.openInventory(inv);
    }

    public void openCassa(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Cassa - " + faction.getName());

        // Carica contenuto salvato
        ItemStack[] contents = faction.getCassaContents();
        if (contents != null) {
            for (int i = 0; i < Math.min(contents.length, 27); i++) {
                inv.setItem(i, contents[i]);
            }
        }

        openCassa.put(player.getUniqueId(), faction.getName().toLowerCase());
        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
