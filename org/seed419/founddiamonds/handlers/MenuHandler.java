package org.seed419.founddiamonds.handlers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.util.Format;
import org.seed419.founddiamonds.util.Prefix;

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
public class MenuHandler {


    private FoundDiamonds fd;
    private final static int configPages = 2;


    public MenuHandler(FoundDiamonds fd) {
        this.fd = fd;
    }


    public void printMainMenu(CommandSender sender) {
        if (fd.getPermissions().hasAnyMenuPerm(sender)) {
            sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " [FoundDiamonds Main Menu]");
            sender.sendMessage(" /fd " + ChatColor.RED + "[argument] {optional}");
            if (fd.getPermissions().hasAdminManagementPerm(sender)) {
                sender.sendMessage(ChatColor.RED + "    admin" + ChatColor.WHITE + " - Manage admin message blocks");
            }
            if (fd.getPermissions().hasBroadcastManagementPerm(sender)) {
                sender.sendMessage(ChatColor.RED + "    bc" + ChatColor.WHITE + " - Manage broadcasted blocks");
            }
            if (fd.getPermissions().hasBroadcastManagementPerm(sender)) {
                sender.sendMessage(ChatColor.RED + "    clearplaced" + ChatColor.WHITE + " - Forget placed blocks");
            }
            if (fd.getPermissions().hasConfigPerm(sender)) {
                sender.sendMessage(ChatColor.RED + "    config" + ChatColor.WHITE + " - View the configuration file");
            }
            if (fd.getPermissions().hasLightManagementPerm(sender)) {
                sender.sendMessage(ChatColor.RED + "    light" + ChatColor.WHITE + " - Manage light-monitored blocks");
            }
            if (fd.getPermissions().hasReloadPerm(sender)) {
                sender.sendMessage(ChatColor.RED + "    reload" + ChatColor.WHITE + " - Reload the configuration file");
            }
            if (fd.getPermissions().hasTogglePerm(sender)) {
                sender.sendMessage(ChatColor.RED + "    set" + ChatColor.WHITE + " - Modify values in the config");
                sender.sendMessage(ChatColor.RED + "    toggle" + ChatColor.WHITE + " - Toggle options in the configuration");
            }
            if (fd.getPermissions().hasTrapPerm(sender)) {
                sender.sendMessage(ChatColor.RED + "    trap {item} {depth}" + ChatColor.WHITE + " - Set a trap block");
            }
            if (fd.getPermissions().hasWorldManagementPerm(sender)) {
                sender.sendMessage(ChatColor.RED + "    world" + ChatColor.WHITE + " - Manage enabled worlds");
            }
            if (fd.getPermissions().hasAnyMenuPerm(sender)) {
                sender.sendMessage(ChatColor.RED + "    version" + ChatColor.WHITE + " - View version information");
            }
        } else {
            fd.getPermissions().sendPermissionsMessage(sender);
        }
    }

    public void showToggle(CommandSender sender) {
        sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " [Toggle Options]");
        sender.sendMessage(ChatColor.RED + "    ops" + ChatColor.WHITE + " - OPs have all permissions");
        sender.sendMessage(ChatColor.RED + "    kick" + ChatColor.WHITE + " - Kick players on trap breaks");
        sender.sendMessage(ChatColor.RED + "    ban" + ChatColor.WHITE + " - Ban players on trap breaks");
        sender.sendMessage(ChatColor.RED + "    logging" + ChatColor.WHITE + " - Log all diamond breaks to log.txt");
        sender.sendMessage(ChatColor.RED + "    nicks" + ChatColor.WHITE + " - Use player nicknames in broadcasts");
        sender.sendMessage(ChatColor.RED + "    creative" + ChatColor.WHITE + " - Disable in creative gamemode");
        sender.sendMessage(ChatColor.RED + "    items" + ChatColor.WHITE + " - Random items for finding diamonds");
        sender.sendMessage(ChatColor.RED + "    spells" + ChatColor.WHITE + " - Random spells for finding diamonds");
        sender.sendMessage(ChatColor.RED + "    cleanlog" + ChatColor.WHITE + " - Clean log (all ore announcements)");
    }

    public void showConfig(FoundDiamonds fd, CommandSender sender) {
        sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " [Configuration 1/" + configPages + "]");
        sender.sendMessage(ChatColor.RED + "    Random spells for finding diamonds: " + getPrettyMenuBoolean(fd.getConfig().getBoolean(Config.potionsForFindingDiamonds)));
        sender.sendMessage(ChatColor.RED + "    Spell Strength: " + ChatColor.AQUA + fd.getConfig().getInt(Config.potionStrength));
        sender.sendMessage(ChatColor.RED + "    Odds of casting spells: " + ChatColor.AQUA + fd.getConfig().getInt(Config.chanceToGetPotion) + "%");
        sender.sendMessage(ChatColor.RED + "    Random items for finding diamonds: " + getPrettyMenuBoolean(fd.getConfig().getBoolean(Config.itemsForFindingDiamonds)));
        sender.sendMessage(ChatColor.RED + "    Odds of getting items: " + ChatColor.AQUA + fd.getConfig().getInt(Config.chanceToGetItem) + "%");
        sender.sendMessage(ChatColor.RED + "    Item 1: " + ChatColor.AQUA + Format.material(Material.getMaterial(fd.getConfig().getInt(Config.randomItem1))));
        sender.sendMessage(ChatColor.RED + "    Item 2: " + ChatColor.AQUA + Format.material(Material.getMaterial(fd.getConfig().getInt(Config.randomItem2))));
        sender.sendMessage(ChatColor.RED + "    Item 3: " + ChatColor.AQUA + Format.material(Material.getMaterial(fd.getConfig().getInt(Config.randomItem3))));
        sender.sendMessage(ChatColor.RED + "    LoggingHandler all diamond ore breaks: " + getPrettyMenuBoolean(fd.getConfig().getBoolean(Config.logDiamondBreaks)));
        sender.sendMessage("Type /fd config 2 to read the next page");
    }

    public void showConfig2(FoundDiamonds fd, CommandSender sender) {
        sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " [Configuration 2/" + configPages + "]");
        sender.sendMessage(ChatColor.RED + "    Disable in creative mode: " + getPrettyMenuBoolean(fd.getConfig().getBoolean(Config.disableInCreative)));
        sender.sendMessage(ChatColor.RED + "    Clean Log: " + getPrettyMenuBoolean(fd.getConfig().getBoolean(Config.cleanLog)));
        sender.sendMessage(ChatColor.RED + "    Light Level blocks disabling at " + ChatColor.AQUA + fd.getConfig().getString(Config.percentOfLightRequired) + ChatColor.RED + " light");
        sender.sendMessage(ChatColor.RED + "    Use player nicknames: " + getPrettyMenuBoolean(fd.getConfig().getBoolean(Config.useNick)));
        sender.sendMessage(ChatColor.RED + "    Give OPs all permissions: " +  getPrettyMenuBoolean(fd.getConfig().getBoolean(Config.opsAsFDAdmin)));
        sender.sendMessage(ChatColor.RED + "    Kick players on trap break: " + getPrettyMenuBoolean(fd.getConfig().getBoolean(Config.kickOnTrapBreak)));
        sender.sendMessage(ChatColor.RED + "    Ban players on trap break: " + getPrettyMenuBoolean(fd.getConfig().getBoolean(Config.banOnTrapBreak)));
    }

    public void showLightMenu(CommandSender sender) {
        sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " [Light Level Blocks]");
        sender.sendMessage(ChatColor.RED + "    add" + ChatColor.WHITE + " - Monitor a new blocks light level");
        sender.sendMessage(ChatColor.RED + "    remove" + ChatColor.WHITE + " - Stop a block from being monitored");
        sender.sendMessage(ChatColor.RED + "    list" + ChatColor.WHITE + " - List the currently monitored blocks");
    }

    public void showAdminMenu(CommandSender sender) {
        sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " [Admin Message Blocks]");
        sender.sendMessage(ChatColor.RED + "    add" + ChatColor.WHITE + " - Add a block that will send admin messages");
        sender.sendMessage(ChatColor.RED + "    remove" + ChatColor.WHITE + " - Remove an admin message block");
        sender.sendMessage(ChatColor.RED + "    list" + ChatColor.WHITE + " - List the current admin message blocks");
    }

    public void showBcMenu(CommandSender sender) {
        sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " [Broadcasted Blocks]");
        sender.sendMessage(ChatColor.RED + "    add" + ChatColor.WHITE + " - Add a new block to be broadcasted");
        sender.sendMessage(ChatColor.RED + "    remove" + ChatColor.WHITE + " - Remove a block from being broadcasted");
        sender.sendMessage(ChatColor.RED + "    list" + ChatColor.WHITE + " - List all currently broadcasted blocks");
    }

    public void showWorldMenu(CommandSender sender) {
        sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " [Worlds 1/1]");
        sender.sendMessage("/fd world" + ChatColor.RED + " <option>");
        sender.sendMessage(ChatColor.RED + "    list" + ChatColor.WHITE + " - List FD enabled worlds");
        sender.sendMessage(ChatColor.RED + "    add <world>" + ChatColor.WHITE + " - Add FD to a world.");
        sender.sendMessage(ChatColor.RED + "    remove <world>" + ChatColor.WHITE + " - Remove FD from a world.");
    }

    public void showSetMenu(CommandSender sender) {
        sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " [Set]");
        //todo? meh...
/*        sender.sendMessage(ChatColor.RED + "    item1 <id number>");
        sender.sendMessage(ChatColor.RED + "    item2 <id number>");
        sender.sendMessage(ChatColor.RED + "    item3 <id number>");*/
        sender.sendMessage(ChatColor.RED + "    spellpercent <percent>" + ChatColor.WHITE + " Set odds that spells are casted.");
        sender.sendMessage(ChatColor.RED + "    itempercent <percent>" + ChatColor.WHITE + " Set odds that items are awarded.");
    }

    public boolean handleSetMenu(FoundDiamonds fd, CommandSender sender, String[] args) {
        if (args.length == 1) {
            showSetMenu(sender);
            return true;
        } else if (args.length == 2) {
            if (args[1].equalsIgnoreCase("spellpercent")) {
                System.out.println("spellpercent");
                sender.sendMessage(Prefix.getChatPrefix() + ChatColor.DARK_RED + " Usage: /fd set "
                        + "spellpercent <percent>");
                return true;
            } else if (args[1].equalsIgnoreCase("itempercent")) {
                sender.sendMessage(Prefix.getChatPrefix() + ChatColor.DARK_RED + " Usage: /fd set "
                        + "itempercent <percent>");
            }
        } else if (args.length == 3) {
            if (args[1].equalsIgnoreCase("spellpercent")) {
                int percent = getPercent(fd, sender, args);
                setPercent(fd, sender, percent, Config.chanceToGetPotion);
            } else if (args[1].equalsIgnoreCase("itempercent")) {
                int percent = getPercent(fd, sender, args);
                setPercent(fd, sender, percent, Config.chanceToGetItem);
            }
        }
        return false;
    }

    public void setPercent(FoundDiamonds fd, CommandSender sender, int percent, String configuration) {
        if (percent != 999) {
            fd.getConfig().set(configuration, percent);
            printSaved(fd, sender);
        } else {
            sender.sendMessage(Prefix.getChatPrefix() + ChatColor.DARK_RED + " Percentage "
                    + "must be between 0 and 100");
        }
    }

    public int getPercent(FoundDiamonds fd, CommandSender sender, String[] args) {
        int percent;
        try {
            percent = Integer.parseInt(args[2].trim());
        } catch (Exception ex) {
            sender.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " Invalid argument");
            return 999;
        }
        if (percent >=0 && percent <=100) {
            fd.getConfig().set(Config.chanceToGetPotion, percent);
            printSaved(fd, sender);
            return percent;
        } else {
            return 999;
        }
    }

    public void handleBcMenu(FoundDiamonds fd, CommandSender sender, String[] args) {
        if (args.length == 1) {
            showBcMenu(sender);
        } else if (args.length > 1) {
            if (args[1].equalsIgnoreCase("list")) {
                sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " [Broadcasted Blocks]");
                fd.getMapHandler().handleListingList(sender, fd.getMapHandler().getBroadcastedBlocks());
            } else if (args[1].equalsIgnoreCase("add")) {
                fd.getMapHandler().handleAddToList(sender, args, fd.getMapHandler().getBroadcastedBlocks(), Config.broadcastedBlocks);
            } else if (args[1].equalsIgnoreCase("remove")) {
                fd.getMapHandler().handleRemoveFromList(sender, args, fd.getMapHandler().getBroadcastedBlocks(), Config.broadcastedBlocks);
            } else {
                sender.sendMessage(Prefix.getChatPrefix() + ChatColor.DARK_RED + " Unrecognized command " + ChatColor.WHITE + "'" + args[1] + "'");
            }
        }
    }

    public void handleAdminMenu(FoundDiamonds fd, CommandSender sender, String[] args) {
        if (args.length == 1) {
            showAdminMenu(sender);
        } else if (args.length > 1) {
            if (args[1].equalsIgnoreCase("list")) {
                sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " [Admin Message Blocks]");
                fd.getMapHandler().handleListingList(sender, fd.getMapHandler().getAdminMessageBlocks());
            } else if (args[1].equalsIgnoreCase("add")) {
                fd.getMapHandler().handleAddToList(sender, args, fd.getMapHandler().getAdminMessageBlocks(), Config.adminMessageBlocks);
            } else if (args[1].equalsIgnoreCase("remove")) {
                fd.getMapHandler().handleRemoveFromList(sender, args, fd.getMapHandler().getAdminMessageBlocks(), Config.adminMessageBlocks);
            } else {
                sender.sendMessage(Prefix.getChatPrefix() + ChatColor.DARK_RED + " Unrecognized command " + ChatColor.WHITE + "'" + args[1] + "'");
            }
        }
    }

    public void handleLightMenu(FoundDiamonds fd, CommandSender sender, String[] args) {
        if (args.length == 1) {
            showLightMenu(sender);
        } else if (args.length > 1) {
            if (args[1].equalsIgnoreCase("list")) {
                sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " [Light-Monitored Blocks]");
                fd.getMapHandler().handleListingList(sender, fd.getMapHandler().getLightLevelBlocks());
            } else if (args[1].equalsIgnoreCase("add")) {
                fd.getMapHandler().handleAddToList(sender, args, fd.getMapHandler().getLightLevelBlocks(), Config.lightLevelBlocks);
            } else if (args[1].equalsIgnoreCase("remove")) {
                fd.getMapHandler().handleRemoveFromList(sender, args, fd.getMapHandler().getLightLevelBlocks(), Config.lightLevelBlocks);
            } else {
                sender.sendMessage(Prefix.getChatPrefix() + ChatColor.DARK_RED + " Unrecognized command " + ChatColor.WHITE + "'" + args[1] + "'");
            }
        }
    }

    public void showVersion(FoundDiamonds fd, CommandSender sender) {
        sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " [Version]");
        sender.sendMessage(ChatColor.DARK_GREEN + " @version:  " + ChatColor.GOLD + fd.getPdf().getVersion());
        sender.sendMessage(ChatColor.DARK_GREEN + " @author:   " + ChatColor.GOLD + "seed419");
        sender.sendMessage(ChatColor.DARK_GREEN + " @website:  " + ChatColor.GOLD + "http://dev.bukkit.org/server-mods/founddiamonds/");
    }

    private String getPrettyMenuBoolean(Boolean b) {
        return (b ? ChatColor.DARK_GREEN + "[On]" : ChatColor.DARK_RED + "[Off]");
    }

    public void printSaved(FoundDiamonds fd, CommandSender sender) {
        fd.saveConfig();
        sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " Configuration updated.");
    }

}
