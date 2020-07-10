package com.gportals.sumo.listeners;

import com.gportals.sumo.models.SumoFight;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

import static com.gportals.sumo.Sumo.plugin;
import static com.gportals.sumo.Sumo.sumoFights;

public class CommandListener implements CommandExecutor {

    Server server = plugin.getServer();
    Configuration config = plugin.getConfig();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command!");
            return true;
        }

        if (command.getName().equals("sumo")) {

            if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("deny")) {

                for (SumoFight sumoFight : sumoFights) {
                    if (sumoFight.getTargetPlayer() == sender) {
                        if (!(sumoFight.isAcceptValid())) {
                            sender.sendMessage("This invitation is not valid anymore!");
                            return true;
                        }

                        if (args[0].equalsIgnoreCase("accept")) {
                            sumoFight.getStartPlayer().sendMessage(sumoFight.getTargetPlayer().getDisplayName() +
                                    " has accepted your invitation");
                            sumoFight.start();
                        } else {
                            sumoFight.getStartPlayer().sendMessage(sumoFight.getTargetPlayer().getDisplayName() +
                                    " has denied your invitation");
                            sumoFights.remove(sumoFight);
                        }


                        return true;
                    }
                }

                sender.sendMessage("You haven't been invited to a sumo battle!");

                return true;
            }

            if (args[0].equalsIgnoreCase("stop")) {

                for (SumoFight sumoFight : sumoFights) {
                    if (sumoFight.isStarted()) {
                        if (sumoFight.containsPlayer((Player) sender)) {
                            sumoFight.end();
                            return true;
                        }
                    }
                }
                return true;
            }

            Player startPlayer = ((Player) sender).getPlayer();
            Player targetPlayer = server.getPlayer(args[0]);

            if (startPlayer == null || targetPlayer == null) {
                sender.sendMessage("That player doesn't exist!");
                return true;
            }

            if (startPlayer == targetPlayer && !startPlayer.isOp()) {
                sender.sendMessage("You can't invite yourself to a sumo fight!");
                return true;
            }

            for (SumoFight sumoFight : sumoFights) {
                if (sumoFight.containsPlayer(startPlayer)) {
                    sender.sendMessage("You are already in a sumo fight!");
                    return true;
                }
                if (sumoFight.containsPlayer(targetPlayer)) {
                    sender.sendMessage("That player is already in a sumo fight");
                    return true;
                }
            }

            TextComponent acceptMsg = new TextComponent(ChatColor.GREEN + "[accept] ");
            acceptMsg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sumo accept"));
            acceptMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/sumo accept").create()));

            TextComponent denyMsg = new TextComponent(ChatColor.RED + "[deny]");
            denyMsg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sumo deny"));
            denyMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/sumo deny").create()));

            sender.sendMessage(ChatColor.GREEN + "Sending invite for sumo fight to " + targetPlayer.getDisplayName());
            targetPlayer.sendMessage(startPlayer.getDisplayName() + ChatColor.GOLD + " has invited you to play a sumo fight");
            targetPlayer.sendMessage(acceptMsg, denyMsg);

            sumoFights.add(new SumoFight(startPlayer, targetPlayer));

            return true;
        }

        return true;
    }
}
