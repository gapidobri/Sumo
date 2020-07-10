package com.gportals.sumo.listeners;

import com.gportals.sumo.models.SumoFight;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.gportals.sumo.Sumo.plugin;
import static com.gportals.sumo.Sumo.sumoFights;

public class Listeners implements Listener {
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        for (SumoFight sumoFight : sumoFights) {
            if (sumoFight.isStarted() && sumoFight.containsPlayer((Player) event.getEntity())) {
                ((Player) event.getEntity()).setHealth(((Player) event.getEntity()).getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        for (SumoFight sumoFight : sumoFights) {
            if (sumoFight.isStarted() && sumoFight.containsPlayer(event.getPlayer())) {
                if (event.getTo().getY() < plugin.getConfig().getDouble("arenas." + sumoFight.getArena() + ".pos1.y") - 3) {
                    sumoFight.end(event.getPlayer() != sumoFight.getStartPlayer() ? sumoFight.getStartPlayer() : sumoFight.getTargetPlayer());
                    return;
                }
                if (sumoFight.isFreezePlayers()) event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        File playerConfigFile = new File(plugin.getDataFolder(), "players.yml");
        FileConfiguration playerCfg = YamlConfiguration.loadConfiguration(playerConfigFile);
        Player player = event.getPlayer();

        if (!playerCfg.getKeys(false).contains(player.getUniqueId().toString())) return;

        // Give players their inventory
        List<?> inventory = playerCfg.getList(player.getUniqueId() + ".inventory");
        List<?> armor = playerCfg.getList(player.getUniqueId() + ".armor");

        player.getInventory().setContents(inventory.toArray(new ItemStack[inventory.size()]));
        player.getInventory().setArmorContents(armor.toArray(new ItemStack[armor.size()]));
        player.addPotionEffects((Collection<PotionEffect>) playerCfg.get(player.getUniqueId() + ".effects"));

        // Set player's health and food
        player.setHealth(playerCfg.getDouble(player.getUniqueId() + ".health"));
        player.setFoodLevel(playerCfg.getInt(player.getUniqueId() + ".food"));

        // Set player's gamemode
        player.setGameMode((GameMode) playerCfg.get(player.getUniqueId() + ".gamemode"));

        // TP players back to initial location
        player.teleport(playerCfg.getLocation(player.getUniqueId() + ".location"));

        // Remove player data from config
        playerCfg.set(String.valueOf(player.getUniqueId()), null);

        try {
            playerCfg.save(playerConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
