package com.gportals.sumo.models;

import com.destroystokyo.paper.Title;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import static com.gportals.sumo.Sumo.plugin;
import static com.gportals.sumo.Sumo.sumoFights;

public class SumoFight {

    Configuration config = plugin.getConfig();

    Player startPlayer;
    Player targetPlayer;
    String arena;
    long inviteTime;
    boolean started;
    boolean freezePlayers;

    String startPlayerUUID;
    String targetPlayerUUID;
    Location startPlayerLoc;
    Location targetPlayerLoc;

    File playerConfigFile = new File(plugin.getDataFolder(), "players.yml");
    FileConfiguration playerCfg = YamlConfiguration.loadConfiguration(playerConfigFile);

    int validSeconds = 60;

    public SumoFight(Player startPlayer, Player targetPlayer) {
        this.startPlayer = startPlayer;
        this.targetPlayer = targetPlayer;
        this.arena = getAvailableArena();
        this.inviteTime = System.currentTimeMillis();
        this.started = false;
        this.freezePlayers = false;

        startPlayerUUID = startPlayer.getUniqueId().toString();
        targetPlayerUUID = targetPlayer.getUniqueId().toString();
    }

    public Player getStartPlayer() {
        return startPlayer;
    }

    public Player getTargetPlayer() {
        return targetPlayer;
    }

    public long getInviteTime() {
        return inviteTime;
    }

    public boolean isAcceptValid() {
        return inviteTime + (validSeconds * 1000) >= System.currentTimeMillis();
    }

    public boolean containsPlayer(Player player) {
        return startPlayer == player || targetPlayer == player;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isFreezePlayers() {
        return freezePlayers;
    }

    public String getArena() {
        return arena;
    }

    public String getAvailableArena() {
        ConfigurationSection arenas = config.getConfigurationSection("arenas");
        String emptyArena = null;
        for (String arena : arenas.getKeys(false)) {
            boolean isFull = false;
            for (SumoFight sumoFight : sumoFights) {
                if (sumoFight.arena.equals(arena)) {
                    isFull = true;
                }
            }
            if (!isFull) {
                emptyArena = arena;
                break;
            }
        }
        return emptyArena;
    }

    public void broadcast(String message) {
        startPlayer.sendMessage(message);
        targetPlayer.sendMessage(message);
    }

    public void start() {

        if (arena == null) {
            broadcast(ChatColor.RED + "There are no free arenas!");
            sumoFights.remove(this);
            return;
        }

        broadcast(ChatColor.DARK_GREEN + "Sumo fight has started");

        // Save player's inventory (to config)
        playerCfg.set(startPlayerUUID + ".inventory", startPlayer.getInventory().getContents());
        playerCfg.set(startPlayerUUID + ".armor", startPlayer.getInventory().getArmorContents());
        playerCfg.set(startPlayerUUID + ".effects", startPlayer.getActivePotionEffects());

        playerCfg.set(targetPlayerUUID + ".inventory", targetPlayer.getInventory().getContents());
        playerCfg.set(targetPlayerUUID + ".armor", targetPlayer.getInventory().getArmorContents());
        playerCfg.set(targetPlayerUUID + ".effects", targetPlayer.getActivePotionEffects());

        // Save player's location to config
        playerCfg.set(startPlayerUUID + ".location", startPlayer.getLocation());
        playerCfg.set(targetPlayerUUID + ".location", targetPlayer.getLocation());

        // Save player's health and food to config
        playerCfg.set(startPlayerUUID + ".health", startPlayer.getHealth());
        playerCfg.set(targetPlayerUUID + ".health", targetPlayer.getHealth());

        playerCfg.set(startPlayerUUID + ".food", startPlayer.getFoodLevel());
        playerCfg.set(targetPlayerUUID + ".food", targetPlayer.getFoodLevel());

        // Save player's gamemode to config
        playerCfg.set(startPlayerUUID + ".gamemode", startPlayer.getGameMode());
        playerCfg.set(targetPlayerUUID + ".gamemode", targetPlayer.getGameMode());

        try {
            playerCfg.save(playerConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Remove player's inventory and effects
        startPlayer.getInventory().clear();
        for (PotionEffect potionEffect : startPlayer.getActivePotionEffects()) {
            startPlayer.removePotionEffect(potionEffect.getType());
        }

        targetPlayer.getInventory().clear();
        for (PotionEffect potionEffect : targetPlayer.getActivePotionEffects()) {
            targetPlayer.removePotionEffect(potionEffect.getType());
        }

        // Set player's gamemode
        startPlayer.setGameMode(GameMode.SURVIVAL);
        targetPlayer.setGameMode(GameMode.SURVIVAL);

        // TP players to arena location
        ConfigurationSection arenaConf = config.getConfigurationSection("arenas." + arena);
        ConfigurationSection startPlayerConf = arenaConf.getConfigurationSection("pos1");
        ConfigurationSection targetPlayerConf = arenaConf.getConfigurationSection("pos2");

        startPlayer.teleport(new Location(
                plugin.getServer().getWorld(startPlayerConf.getString("world")),
                startPlayerConf.getDouble("x"),
                startPlayerConf.getDouble("y"),
                startPlayerConf.getDouble("z"),
                startPlayerConf.getInt("yaw"),
                startPlayerConf.getInt("pitch")
        ));

        targetPlayer.teleport(new Location(
                plugin.getServer().getWorld(targetPlayerConf.getString("world")),
                targetPlayerConf.getDouble("x"),
                targetPlayerConf.getDouble("y"),
                targetPlayerConf.getDouble("z"),
                targetPlayerConf.getInt("yaw"),
                targetPlayerConf.getInt("pitch")
        ));


        // Set player's health and food
        startPlayer.setHealth(startPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
        targetPlayer.setHealth(targetPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());

        startPlayer.setFoodLevel(20);
        targetPlayer.setFoodLevel(20);

        freezePlayers = true;

        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int i = 5;

            @Override
            public void run() {
                i--;
                startPlayer.sendTitle(new Title(String.valueOf(i != 0 ? i : "Fight!"), "", 0, 20, 0));
                targetPlayer.sendTitle(new Title(String.valueOf(i != 0 ? i : "Fight!"), "", 0, 20, 0));
                if (i <= 0) {
                    freezePlayers = false;
                    started = true;
                    timer.cancel();
                }
            }
        }, 0, 1000);
    }

    public void end(Player winner) {
        // Give players their inventory
        startPlayer.getInventory().setContents((ItemStack[]) playerCfg.get(startPlayerUUID + ".inventory"));
        startPlayer.getInventory().setArmorContents((ItemStack[]) playerCfg.get(startPlayerUUID + ".armor"));
        startPlayer.addPotionEffects((Collection<PotionEffect>) playerCfg.get(startPlayerUUID + ".effects"));

        targetPlayer.getInventory().setContents((ItemStack[]) playerCfg.get(targetPlayerUUID + ".inventory"));
        targetPlayer.getInventory().setArmorContents((ItemStack[]) playerCfg.get(targetPlayerUUID + ".armor"));
        targetPlayer.addPotionEffects((Collection<PotionEffect>) playerCfg.get(targetPlayerUUID + ".effects"));

        // Set player's health and food
        startPlayer.setHealth(playerCfg.getDouble(startPlayerUUID + ".health"));
        targetPlayer.setHealth(playerCfg.getDouble(targetPlayerUUID + ".health"));

        startPlayer.setFoodLevel(playerCfg.getInt(startPlayerUUID + ".food"));
        targetPlayer.setFoodLevel(playerCfg.getInt(targetPlayerUUID + ".food"));

        // Set player's gamemode
        startPlayer.setGameMode((GameMode) playerCfg.get(startPlayerUUID + ".gamemode"));
        targetPlayer.setGameMode((GameMode) playerCfg.get(targetPlayerUUID + ".gamemode"));

        // TP players back to initial location
        startPlayer.teleport(playerCfg.getLocation(startPlayerUUID + ".location"));
        targetPlayer.teleport(playerCfg.getLocation(targetPlayerUUID + ".location"));

        // Remove player data from config
        playerCfg.set(startPlayerUUID, null);
        playerCfg.set(targetPlayerUUID, null);

        try {
            playerCfg.save(playerConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        started = false;

        if (winner instanceof Player) {
            broadcast(ChatColor.GREEN + winner.getDisplayName() + " has won!");
        } else {
            broadcast(ChatColor.DARK_GREEN + "Sumo fight has been terminated");
        }

        sumoFights.remove(this);
    }

    public void end() {
        end(null);
    }
}
