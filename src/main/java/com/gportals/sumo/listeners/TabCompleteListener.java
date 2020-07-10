package com.gportals.sumo.listeners;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static com.gportals.sumo.Sumo.plugin;

public class TabCompleteListener implements TabCompleter {

    Server server = plugin.getServer();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> ACList = new ArrayList<>();

        if (command.getName().equals("sumo")) {

            if (args[0].matches("^s?t?o?p?")) {
                ACList.add("stop");
            }
            if (args[0].matches("^a?c?c?e?p?t?")) {
                ACList.add("accept");
            }
            if (args[0].matches("^d?e?n?y?")) {
                ACList.add("deny");
            }

            if (ACList.isEmpty()) {
                for (Player player : server.getOnlinePlayers()) {
                    ACList.add(player.getName());
                }
            }

        }

        return ACList;
    }
}
