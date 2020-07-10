package com.gportals.sumo;

import com.gportals.sumo.listeners.CommandListener;
import com.gportals.sumo.listeners.Listeners;
import com.gportals.sumo.listeners.TabCompleteListener;
import com.gportals.sumo.models.SumoFight;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class Sumo extends JavaPlugin {
    public static Sumo plugin;
    public static ArrayList<SumoFight> sumoFights = new ArrayList<SumoFight>();

    @Override
    public void onEnable() {
        plugin = this;

        getServer().getPluginManager().registerEvents(new Listeners(), this);

        getCommand("sumo").setExecutor(new CommandListener());
        getCommand("sumo").setTabCompleter(new TabCompleteListener());

        saveDefaultConfig();

        getLogger().info(ChatColor.GREEN + "SUMO has started");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        plugin = null;
        getLogger().info(ChatColor.RED + "SUMO has stopped");

    }
}
