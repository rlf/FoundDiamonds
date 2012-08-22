/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seed419.founddiamonds.handlers;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.seed419.founddiamonds.*;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.file.FileHandler;
import org.seed419.founddiamonds.util.PluginUtils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author seed419
 */
public class CommandHandler implements CommandExecutor {


    private FoundDiamonds fd;
    private WorldHandler wm;
    private TrapHandler trap;


    public CommandHandler(FoundDiamonds fd, WorldHandler wm, TrapHandler trap) {
        this.fd = fd;
        this.wm = wm;
        this.trap = trap;
    }



 @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
            PluginUtils.logCommandToConsole(fd, player, commandLabel, args);
        }
        if (((commandLabel.equalsIgnoreCase("fd")) || commandLabel.equalsIgnoreCase("founddiamonds"))) {
            if (args.length == 0) {
                MenuHandler.printMainMenu(sender);
                return true;
            } else {
                String arg = args[0];
                if (arg.equalsIgnoreCase("admin")) {
                    if (sender instanceof Player) {
                        if (Permissions.hasPerms(player, "fd.manage.admin.add") || Permissions.hasPerms(player, "fd.manage.admin.remove")
                                || Permissions.hasPerms(player, "fd.manage.admin.list")) {
                            MenuHandler.handleAdminMenu(fd, sender, args);
                        } else {
                            sendPermissionsMessage(player);
                        }
                    } else {
                        MenuHandler.handleAdminMenu(fd, sender, args);
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("bc") || arg.equalsIgnoreCase("broadcast")) {
                    if (sender instanceof Player) {
                        if (Permissions.hasPerms(player, "fd.manage.bc.add") || Permissions.hasPerms(player, "fd.manage.bc.remove")
                                || Permissions.hasPerms(player, "fd.manage.bc.list")) {
                            MenuHandler.handleBcMenu(fd, sender, args);
                        } else {
                            sendPermissionsMessage(player);
                        }
                    } else {
                        MenuHandler.handleBcMenu(fd, sender, args);
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("config")) {
                    if (sender instanceof Player) {
                        if (Permissions.hasPerms(player, "fd.config")) {
                            if (args.length == 2) {
                                if (args[1].equalsIgnoreCase("2")) {
                                    MenuHandler.showConfig2(fd, sender);
                                }
                            } else {
                                MenuHandler.showConfig(fd, sender);
                            }
                        } else {
                            sendPermissionsMessage(player);
                        }
                    }
                } else if (arg.equalsIgnoreCase("debug")) {
                    if (sender instanceof Player) {
                        if (Permissions.hasPerms(player, "fd.toggle")) {
                            if (args.length == 2) {
                                if (args[1].equalsIgnoreCase("2")) {
                                    MenuHandler.showConfig2(fd, sender);
                                }
                            } else {
                                MenuHandler.showConfig(fd, sender);
                            }
                        } else {
                            sendPermissionsMessage(player);
                        }
                    }
                } else if (arg.equalsIgnoreCase("light")) {
                    if (sender instanceof Player) {
                        if (Permissions.hasPerms(player, "fd.manage.light.add") || Permissions.hasPerms(player, "fd.manage.light.remove")
                                || Permissions.hasPerms(player, "fd.manage.light.list")) {
                            MenuHandler.handleLightMenu(fd, sender, args);
                        } else {
                            sendPermissionsMessage(player);
                        }
                    } else {
                        MenuHandler.handleLightMenu(fd, sender, args);
                    }
                    return true;
               } else if (arg.equalsIgnoreCase("reload")) {
                    if (sender instanceof Player) {
                        if (Permissions.hasPerms(player, "fd.reload")) {
                            fd.reloadConfig();
                            fd.saveConfig();
                            sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + " Configuration saved and reloaded.");
                        } else {
                            sendPermissionsMessage(player);
                        }
                    } else {
                        fd.reloadConfig();
                        fd.saveConfig();
                        sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + " Configuration saved and reloaded.");
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("set")) {
                    if (sender instanceof Player) {
                        if (Permissions.hasPerms(player, "fd.toggle")) {
                            MenuHandler.handleSetMenu(fd, sender, args);
                        } else {
                            sendPermissionsMessage(player);
                        }
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("toggle")) {
                    if (!Permissions.hasPerms(sender, "fd.toggle")) {
                        sendPermissionsMessage(sender);
                    } else {
                        if (args.length == 1) {
                            MenuHandler.showToggle(sender);
                        } else  if (args.length == 2) {
                            arg = args[1];
                            handleToggle(sender, arg);
                        } else {
                            sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " Invalid number of arguments.");
                            sender.sendMessage(ChatColor.RED + "See '/fd toggle' for the list of valid arguments.");
                        }
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("trap")) {
                    if (sender instanceof Player) {
                        if (Permissions.hasPerms(player, "fd.trap")) {
                            trap.handleTrap(player, args);
                        } else {
                            sendPermissionsMessage(player);
                        }
                    } else {
                        sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.DARK_RED + " Can't set a trap from the console.");
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("world")) {
                    if (sender instanceof Player) {
                        if (Permissions.hasPerms(player, "fd.world")) {
                            wm.handleWorldMenu(sender, args);
                        } else {
                            sendPermissionsMessage(player);
                        }
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("version")) {
                    MenuHandler.showVersion(sender);
                    return true;
                } else {
                        sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.DARK_RED + " Unrecognized command '"
                                + ChatColor.WHITE + args[0] + ChatColor.DARK_RED + "'");
                    return true;
                }
            }
        }
        return false;
    }


    public void sendPermissionsMessage(CommandSender sender) {
        sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " You don't have permission to do that.");
        fd.getLog().warning(sender.getName() + " was denied access to a command.");
    }

    private boolean handleToggle(CommandSender sender, String arg) {
        if (arg.equalsIgnoreCase("creative")) {
            fd.getConfig().set(Config.disableInCreative, !fd.getConfig().getBoolean(Config.disableInCreative));
            MenuHandler.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("ops")) {
            fd.getConfig().set(Config.opsAsFDAdmin, !fd.getConfig().getBoolean(Config.opsAsFDAdmin));
            MenuHandler.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("kick")) {
            fd.getConfig().set(Config.kickOnTrapBreak, !fd.getConfig().getBoolean(Config.kickOnTrapBreak));
            MenuHandler.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("ban") || arg.equalsIgnoreCase("bans")) {
            fd.getConfig().set(Config.banOnTrapBreak, !fd.getConfig().getBoolean(Config.banOnTrapBreak));
            MenuHandler.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("items")) {
            fd.getConfig().set(Config.itemsForFindingDiamonds, !fd.getConfig().getBoolean(Config.itemsForFindingDiamonds));
            MenuHandler.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("logging")) {
            fd.getConfig().set(Config.logDiamondBreaks, !fd.getConfig().getBoolean(Config.logDiamondBreaks));
            MenuHandler.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("spells")) {
            fd.getConfig().set(Config.potionsForFindingDiamonds, !fd.getConfig().getBoolean(Config.potionsForFindingDiamonds));
            MenuHandler.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("cleanlog")) {
            fd.getConfig().set(Config.cleanLog, !fd.getConfig().getBoolean(Config.cleanLog));
            if (!FileHandler.getCleanLog().exists()) {
                try {
                    boolean successful = FileHandler.getCleanLog().createNewFile();
                    if (successful) {sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.DARK_GREEN +" Cleanlog created.");}
                    } catch (IOException ex) {
                    sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.DARK_RED + " Uh-oh...couldn't create CleanLog.txt");
                    Logger.getLogger(FoundDiamonds.class.getName()).log(Level.SEVERE, "Failed to create CleanLog file.", ex);
                }
            }
            MenuHandler.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("nick") || arg.equalsIgnoreCase("nicks")) {
            fd.getConfig().set(Config.useNick, !fd.getConfig().getBoolean(Config.useNick));
            MenuHandler.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("debug")) {
            fd.getConfig().set(Config.debug, !fd.getConfig().getBoolean(Config.debug));
            MenuHandler.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("2")) {
            MenuHandler.showToggle2(sender);
        } else {
            sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " Argument '" + arg + "' unrecognized.");
            sender.sendMessage(ChatColor.RED + "See '/fd toggle' for the list of valid arguments.");
            return false;
        }
        return true;
    }

}
