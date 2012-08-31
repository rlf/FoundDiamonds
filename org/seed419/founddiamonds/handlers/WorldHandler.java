package org.seed419.founddiamonds.handlers;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.util.PluginUtils;
import org.seed419.founddiamonds.util.Prefix;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Attribute Only (Public) License
 * Version 0.a3, July 11, 2011
 * <p/>
 * Copyright (C) 2012 Blake Bartenbach <seed419@gmail.com> (@seed419)
 * <p/>
 * Anyone is allowed to copy and distribute verbatim or modified
 * copies of this license document and altering is allowed as long
 * as you attribute the author(s) of this license document / files.
 * <p/>
 * ATTRIBUTE ONLY PUBLIC LICENSE
 * TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 * <p/>
 * 1. Attribute anyone attached to the license document.
 * Do not remove pre-existing attributes.
 * <p/>
 * Plausible attribution methods:
 * 1. Through comment blocks.
 * 2. Referencing on a site, wiki, or about page.
 * <p/>
 * 2. Do whatever you want as long as you don't invalidate 1.
 *
 * @license AOL v.a3 <http://aol.nexua.org>
 */
public class WorldHandler {


    private FoundDiamonds fd;


    public WorldHandler(FoundDiamonds fd) {
        this.fd = fd;
    }

    public void handleWorldMenu(CommandSender sender, String[] args) {
        if (args.length == 1) {
            fd.getMenuHandler().showWorldMenu(sender);
        } else if (args.length > 1) {
            if (args[1].equalsIgnoreCase("list")) {
                printEnabledWorlds(sender);
            } else if (args[1].equalsIgnoreCase("add")) {
                if (args.length > 2) {
                    String worldName = PluginUtils.getArgs2Plus(args);
                    validateWorld(sender, worldName);
                } else {
                    sender.sendMessage(Prefix.getChatPrefix() + ChatColor.DARK_RED + " Usage: /fd world add <worldname>");
                }
            } else if (args[1].equalsIgnoreCase("remove")) {
                if (fd.getConfig().getStringList(Config.enabledWorlds).contains(PluginUtils.getArgs2Plus(args))) {
                    List<?> worldList = fd.getConfig().getList(Config.enabledWorlds);
                    worldList.remove(PluginUtils.getArgs2Plus(args));
                    fd.getConfig().set(Config.enabledWorlds, worldList);
                    sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " World '" + PluginUtils.getArgs2Plus(args) +"' removed.");
                    fd.saveConfig();
                } else {
                    sender.sendMessage(Prefix.getChatPrefix() + ChatColor.DARK_RED + " World '" + PluginUtils.getArgs2Plus(args) +"' isn't an enabled world.");
                }
            } else {
                sender.sendMessage(Prefix.getChatPrefix() + ChatColor.DARK_RED + " Unrecognized command.  See /fd world");
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
                    sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " World '" + worldName + "' added.");
                    fd.saveConfig();
                    return;
                } else {
                    sender.sendMessage(Prefix.getChatPrefix() + ChatColor.DARK_RED + " That world is already enabled.");
                    return;
                }
            }
        }
        sender.sendMessage(Prefix.getChatPrefix() + ChatColor.DARK_RED + " Couldn't find a world with the name '" + worldName + "'");
    }

    public void printEnabledWorlds(CommandSender sender) {
        sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " [Enabled Worlds]");
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

    public boolean isEnabledWorld(Player player) {
        return fd.getConfig().getList(Config.enabledWorlds).contains(player.getWorld().getName());
    }

    public boolean isValidGameMode(Player player) {
        return !((player.getGameMode() == GameMode.CREATIVE) && (fd.getConfig().getBoolean(Config.disableInCreative)));
    }
}
