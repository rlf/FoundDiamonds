package org.seed419.founddiamonds;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: seed419
 * Date: 5/10/12
 * Time: 7:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class WorldManager {


    private FoundDiamonds fd;


    public WorldManager(FoundDiamonds fd) {
        this.fd = fd;
    }

    public void handleWorldMenu(CommandSender sender, String[] args) {
        if (args.length == 1) {
            Menu.showWorldMenu(sender);
        } else if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("list")) {
                if (args.length == 2) {
                    printEnabledWorlds(sender);
                } else {
                    sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.DARK_RED + " Usage: /fd world list");
                }
            } else if (args[1].equalsIgnoreCase("add")) {
                if (args.length == 3) {
                    String worldName = args[2];
                    validateWorld(sender, worldName);
                } else if (args.length == 4) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(args[2]);
                    sb.append(" ");
                    sb.append(args[3]);
                    String worldName = sb.toString();
                    validateWorld(sender, worldName);
                } else {
                    sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.DARK_RED + " Usage: /fd world add <worldname>");
                }
            } else if (args[1].equalsIgnoreCase("remove")) {
                if (fd.getConfig().getStringList(Config.enabledWorlds).contains(args[2])) {
                    List<?> worldList = fd.getConfig().getList(Config.enabledWorlds);
                    worldList.remove(args[2]);
                    fd.getConfig().set(Config.enabledWorlds, worldList);
                    sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + " World '" + args[2] +"' removed.");
                    fd.saveConfig();
                } else {
                    sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.DARK_RED + " World '" + args[2] +"' isn't an enabled world.");
                }
            } else {
                sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.DARK_RED + " Unrecognized command.  See /fd world");
            }
        }
    }

    public void validateWorld(CommandSender sender, String worldName) {
        List<World> temp = fd.getServer().getWorlds();
        for (World w : temp) {
            if (w.getName().equals(worldName)) {
                @SuppressWarnings("unchecked")
                Collection<String> worldList = (Collection<String>) fd.getConfig().getList(Config.enabledWorlds);
                if (!worldList.contains(worldName)) {
                    worldList.add(worldName);
                    fd.getConfig().set(Config.enabledWorlds, worldList);
                    sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + " World '" + worldName + "' added.");
                    fd.saveConfig();
                    return;
                } else {
                    sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.DARK_RED + " That world is already enabled.");
                    return;
                }
            }
        }
        sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.DARK_RED + " Couldn't find a world with the name '" + worldName + "'");
    }

    public void printEnabledWorlds(CommandSender sender) {
        sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + " [Enabled Worlds]");
        for (Iterator<String> it = fd.getConfig().getStringList(Config.enabledWorlds).iterator(); it.hasNext();) {
            String x = it.next();
            sender.sendMessage("    - " + x);
        }
    }

    public void checkWorlds() {
        if (fd.getConfig().getList(Config.enabledWorlds) == null) {
            addAllWorlds();
        }
    }

    public void addAllWorlds() {
        List<World> worldList = fd.getServer().getWorlds();
        List<String> worldNames = new LinkedList<String>();
        for (World w : worldList) {
            worldNames.add(w.getName());
        }
        fd.getConfig().set(Config.enabledWorlds, worldNames);
        fd.saveConfig();
    }
}
