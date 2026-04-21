package it.italiafactions.listeners;

import it.italiafactions.ItaliaFactions;
import it.italiafactions.models.Faction;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatListener implements Listener {

    private final ItaliaFactions plugin;
    public static final Set<UUID> factionChatEnabled = new HashSet<>();

    public ChatListener(ItaliaFactions plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!factionChatEnabled.contains(uuid)) return;

        e.setCancelled(true);

        Faction faction = plugin.getFactionManager().getFactionByPlayer(uuid);
        if (faction == null) {
            factionChatEnabled.remove(uuid);
            player.sendMessage(ChatColor.RED + "Non sei in nessuna fazione!");
            return;
        }

        String message = e.getMessage();
        String factionTag = ChatColor.GOLD + "[" + faction.getName() + "] ";

        Faction.Rank rank = faction.getRank(uuid);
        ChatColor rankColor = rank == Faction.Rank.BOSS ? ChatColor.RED :
                rank == Faction.Rank.UFFICIALE ? ChatColor.GOLD : ChatColor.GRAY;
        String rankName = rank == Faction.Rank.BOSS ? "Boss" :
                rank == Faction.Rank.UFFICIALE ? "Ufficiale" : "Soldato";

        String formatted = factionTag + rankColor + "[" + rankName + "] " + ChatColor.WHITE + player.getName() + ": " + ChatColor.YELLOW + message;

        // Manda il messaggio solo ai membri della fazione
        for (UUID memberUUID : faction.getMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberUUID);
            if (member != null) {
                member.sendMessage(formatted);
            }
        }
    }
}
