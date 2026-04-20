package it.italiafactions.commands;

import it.italiafactions.ItaliaFactions;
import it.italiafactions.listeners.ChatListener;
import it.italiafactions.models.Faction;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class FactionCommand implements CommandExecutor {

    private final ItaliaFactions plugin;

    public FactionCommand(ItaliaFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {

            // === ADMIN: /f create <nomefazione> <nomeleader> ===
            case "create" -> {
                if (!sender.hasPermission("italiafactions.admin")) {
                    sender.sendMessage(ChatColor.RED + "Non hai i permessi!");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Uso: /f create <nomefazione> <nomeleader>");
                    return true;
                }
                String fName = args[1];
                String leaderName = args[2];

                if (plugin.getFactionManager().factionExists(fName)) {
                    sender.sendMessage(ChatColor.RED + "Fazione già esistente!");
                    return true;
                }

                Player leader = plugin.getServer().getPlayer(leaderName);
                if (leader == null) {
                    sender.sendMessage(ChatColor.RED + "Giocatore non trovato o non online!");
                    return true;
                }

                if (plugin.getFactionManager().isPlayerInFaction(leader.getUniqueId())) {
                    sender.sendMessage(ChatColor.RED + leader.getName() + " è già in una fazione!");
                    return true;
                }

                plugin.getFactionManager().createFaction(fName, leader.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + "Fazione " + fName + " creata! Leader: " + leaderName);
                leader.sendMessage(ChatColor.GOLD + "Sei stato nominato Boss della fazione " + fName + "!");

                // Dai il menu al leader
                Faction faction = plugin.getFactionManager().getFaction(fName);
                plugin.getGUIManager().openBossMenu(leader, faction);
            }

            // === ADMIN: /f set territorio <dimensione> <nomefazione> ===
            case "set" -> {
                if (!sender.hasPermission("italiafactions.admin")) {
                    sender.sendMessage(ChatColor.RED + "Non hai i permessi!");
                    return true;
                }
                if (args.length < 2) {
                    sendHelp(sender);
                    return true;
                }

                String setType = args[1].toLowerCase();

                if (setType.equals("territorio")) {
                    if (!(sender instanceof Player admin)) {
                        sender.sendMessage(ChatColor.RED + "Devi essere in gioco!");
                        return true;
                    }
                    if (args.length < 4) {
                        sender.sendMessage(ChatColor.RED + "Uso: /f set territorio <dimensione> <nomefazione>");
                        return true;
                    }
                    int size;
                    try { size = Integer.parseInt(args[2]); } catch (NumberFormatException ex) {
                        sender.sendMessage(ChatColor.RED + "Dimensione non valida!");
                        return true;
                    }
                    String fName = args[3];
                    Faction faction = plugin.getFactionManager().getFaction(fName);
                    if (faction == null) {
                        sender.sendMessage(ChatColor.RED + "Fazione non trovata!");
                        return true;
                    }
                    faction.setTerritoryCenter(admin.getLocation());
                    faction.setTerritorySize(size);
                    plugin.getFactionManager().saveFactions();
                    sender.sendMessage(ChatColor.GREEN + "Territorio di " + fName + " impostato: " + size + "x" + size + " dal tuo punto!");
                }

                else if (setType.equals("job")) {
                    // /f set job <nomefazione> <nomeplayer> <grado>
                    if (args.length < 5) {
                        sender.sendMessage(ChatColor.RED + "Uso: /f set job <nomefazione> <nomeplayer> <BOSS/UFFICIALE/SOLDATO>");
                        return true;
                    }
                    String fName = args[2];
                    String playerName = args[3];
                    String rankStr = args[4].toUpperCase();

                    Faction faction = plugin.getFactionManager().getFaction(fName);
                    if (faction == null) {
                        sender.sendMessage(ChatColor.RED + "Fazione non trovata!");
                        return true;
                    }

                    Player target = plugin.getServer().getPlayer(playerName);
                    if (target == null) {
                        sender.sendMessage(ChatColor.RED + "Giocatore non online!");
                        return true;
                    }

                    Faction.Rank rank;
                    try { rank = Faction.Rank.valueOf(rankStr); } catch (Exception ex) {
                        sender.sendMessage(ChatColor.RED + "Grado non valido! Usa: BOSS, UFFICIALE, SOLDATO");
                        return true;
                    }

                    // Se il player è già in un'altra fazione, rimuovilo prima
                    if (plugin.getFactionManager().isPlayerInFaction(target.getUniqueId())) {
                        String currentFaction = plugin.getFactionManager().getFactionNameByPlayer(target.getUniqueId());
                        if (!currentFaction.equalsIgnoreCase(fName)) {
                            sender.sendMessage(ChatColor.RED + playerName + " è già in un'altra fazione!");
                            return true;
                        }
                        // È già nella stessa fazione, aggiorna il rank
                        faction.setRank(target.getUniqueId(), rank);
                    } else {
                        plugin.getFactionManager().addPlayerToFaction(target.getUniqueId(), fName, rank);
                    }

                    plugin.getFactionManager().saveFactions();
                    sender.sendMessage(ChatColor.GREEN + playerName + " è ora " + rank.name() + " nella fazione " + fName);
                    target.sendMessage(ChatColor.GOLD + "Sei stato assegnato come " + rank.name() + " nella fazione " + fName + "!");

                    // Se è boss, apri menu
                    if (rank == Faction.Rank.BOSS) {
                        plugin.getGUIManager().openBossMenu(target, faction);
                    }
                }

                else if (setType.equals("tp")) {
                    // /f set tp <nomefazione>
                    if (!(sender instanceof Player admin)) {
                        sender.sendMessage(ChatColor.RED + "Devi essere in gioco!");
                        return true;
                    }
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Uso: /f set tp <nomefazione>");
                        return true;
                    }
                    String fName = args[2];
                    Faction faction = plugin.getFactionManager().getFaction(fName);
                    if (faction == null) {
                        sender.sendMessage(ChatColor.RED + "Fazione non trovata!");
                        return true;
                    }
                    faction.setSpawn(admin.getLocation());
                    plugin.getFactionManager().saveFactions();
                    sender.sendMessage(ChatColor.GREEN + "Punto di spawn della fazione " + fName + " impostato!");
                }
            }

            // === ADMIN: /f disattiva barriere <nomefazione> ===
            case "disattiva" -> {
                if (!sender.hasPermission("italiafactions.admin")) {
                    sender.sendMessage(ChatColor.RED + "Non hai i permessi!");
                    return true;
                }
                if (args.length < 3 || !args[1].equalsIgnoreCase("barriere")) {
                    sender.sendMessage(ChatColor.RED + "Uso: /f disattiva barriere <nomefazione>");
                    return true;
                }
                String fName = args[2];
                Faction faction = plugin.getFactionManager().getFaction(fName);
                if (faction == null) {
                    sender.sendMessage(ChatColor.RED + "Fazione non trovata!");
                    return true;
                }
                faction.setBarriereAttive(false);
                plugin.getFactionManager().saveFactions();
                sender.sendMessage(ChatColor.YELLOW + "Barriere della fazione " + fName + " disattivate!");
            }

            // === ADMIN: /f attiva barriere <nomefazione> ===
            case "attiva" -> {
                if (!sender.hasPermission("italiafactions.admin")) {
                    sender.sendMessage(ChatColor.RED + "Non hai i permessi!");
                    return true;
                }
                if (args.length < 3 || !args[1].equalsIgnoreCase("barriere")) {
                    sender.sendMessage(ChatColor.RED + "Uso: /f attiva barriere <nomefazione>");
                    return true;
                }
                String fName = args[2];
                Faction faction = plugin.getFactionManager().getFaction(fName);
                if (faction == null) {
                    sender.sendMessage(ChatColor.RED + "Fazione non trovata!");
                    return true;
                }
                faction.setBarriereAttive(true);
                plugin.getFactionManager().saveFactions();
                sender.sendMessage(ChatColor.GREEN + "Barriere della fazione " + fName + " riattivate!");
            }

            // === PLAYER: /f tp <nomefazione> ===
            case "tp" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Devi essere in gioco!");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Uso: /f tp <nomefazione>");
                    return true;
                }
                String fName = args[1];
                Faction faction = plugin.getFactionManager().getFaction(fName);
                if (faction == null) {
                    sender.sendMessage(ChatColor.RED + "Fazione non trovata!");
                    return true;
                }

                // Solo i membri possono tpare
                if (!faction.hasMember(player.getUniqueId())) {
                    sender.sendMessage(ChatColor.RED + "Non sei membro di questa fazione!");
                    return true;
                }

                if (faction.getSpawn() == null) {
                    sender.sendMessage(ChatColor.RED + "Nessun punto di spawn impostato per questa fazione!");
                    return true;
                }

                player.teleport(faction.getSpawn());
                player.sendMessage(ChatColor.GREEN + "Teletrasportato alla base di " + faction.getName() + "!");
            }

            // === PLAYER: /f chat ===
            case "chat" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Devi essere in gioco!");
                    return true;
                }

                if (!plugin.getFactionManager().isPlayerInFaction(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "Non sei in nessuna fazione!");
                    return true;
                }

                UUID uuid = player.getUniqueId();
                if (ChatListener.factionChatEnabled.contains(uuid)) {
                    ChatListener.factionChatEnabled.remove(uuid);
                    player.sendMessage(ChatColor.YELLOW + "Chat fazione " + ChatColor.RED + "disattivata" + ChatColor.YELLOW + ". Ora scrivi in chat globale.");
                } else {
                    ChatListener.factionChatEnabled.add(uuid);
                    Faction faction = plugin.getFactionManager().getFactionByPlayer(uuid);
                    player.sendMessage(ChatColor.YELLOW + "Chat fazione " + ChatColor.GREEN + "attivata" + ChatColor.YELLOW + " [" + faction.getName() + "]. Scrivi normalmente per parlare in fazione.");
                }
            }

            // === PLAYER: /f menu (apre menu boss se è boss) ===
            case "menu" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Devi essere in gioco!");
                    return true;
                }
                Faction faction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());
                if (faction == null) {
                    player.sendMessage(ChatColor.RED + "Non sei in nessuna fazione!");
                    return true;
                }
                Faction.Rank rank = faction.getRank(player.getUniqueId());
                if (rank != Faction.Rank.BOSS) {
                    player.sendMessage(ChatColor.RED + "Solo i Boss possono aprire questo menu!");
                    return true;
                }
                plugin.getGUIManager().openBossMenu(player, faction);
            }

            // === /f info ===
            case "info" -> {
                if (!(sender instanceof Player player)) return true;
                Faction faction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());
                if (faction == null) {
                    player.sendMessage(ChatColor.RED + "Non sei in nessuna fazione!");
                    return true;
                }
                player.sendMessage(ChatColor.GOLD + "=== " + faction.getName() + " ===");
                player.sendMessage(ChatColor.YELLOW + "Membri: " + ChatColor.WHITE + faction.getMembers().size());
                player.sendMessage(ChatColor.YELLOW + "Territorio: " + ChatColor.WHITE + faction.getTerritorySize() + "x" + faction.getTerritorySize());
                player.sendMessage(ChatColor.YELLOW + "Barriere: " + (faction.isBarriereAttive() ? ChatColor.GREEN + "Attive" : ChatColor.RED + "Disattive"));
                Faction.Rank myRank = faction.getRank(player.getUniqueId());
                player.sendMessage(ChatColor.YELLOW + "Il tuo ruolo: " + ChatColor.WHITE + myRank.name());
            }

            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== ItaliaFactions - Comandi ===");
        sender.sendMessage(ChatColor.YELLOW + "/f tp <fazione>" + ChatColor.WHITE + " - Teleportati alla base");
        sender.sendMessage(ChatColor.YELLOW + "/f chat" + ChatColor.WHITE + " - Attiva/disattiva chat fazione");
        sender.sendMessage(ChatColor.YELLOW + "/f menu" + ChatColor.WHITE + " - Apri menu boss");
        sender.sendMessage(ChatColor.YELLOW + "/f info" + ChatColor.WHITE + " - Info sulla tua fazione");
        if (sender.hasPermission("italiafactions.admin")) {
            sender.sendMessage(ChatColor.RED + "--- Admin ---");
            sender.sendMessage(ChatColor.RED + "/f create <nome> <leader>");
            sender.sendMessage(ChatColor.RED + "/f set territorio <dim> <fazione>");
            sender.sendMessage(ChatColor.RED + "/f set tp <fazione>");
            sender.sendMessage(ChatColor.RED + "/f set job <fazione> <player> <grado>");
            sender.sendMessage(ChatColor.RED + "/f disattiva barriere <fazione>");
            sender.sendMessage(ChatColor.RED + "/f attiva barriere <fazione>");
        }
    }
}
