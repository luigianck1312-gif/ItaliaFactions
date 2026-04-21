package it.italiafactions;

import it.italiafactions.commands.FactionCommand;
import it.italiafactions.gui.GUIManager;
import it.italiafactions.listeners.ChatListener;
import it.italiafactions.listeners.GUIListener;
import it.italiafactions.listeners.TerritoryListener;
import it.italiafactions.managers.FactionManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ItaliaFactions extends JavaPlugin {

    private FactionManager factionManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        getLogger().info("ItaliaFactions avviato!");
        getDataFolder().mkdirs();

        factionManager = new FactionManager(this);
        guiManager = new GUIManager(this);

        // Registra comandi
        getCommand("f").setExecutor(new FactionCommand(this));

        // Registra listener
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new TerritoryListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        getLogger().info("ItaliaFactions caricato con successo!");
    }

    @Override
    public void onDisable() {
        if (factionManager != null) factionManager.saveFactions();
        getLogger().info("ItaliaFactions disattivato!");
    }

    public FactionManager getFactionManager() { return factionManager; }
    public GUIManager getGUIManager() { return guiManager; }
}
