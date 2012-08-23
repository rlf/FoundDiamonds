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
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.file.FileHandler;
import org.seed419.founddiamonds.util.PluginUtils;
import org.seed419.founddiamonds.util.Prefix;

import java.io.IOException;

/**
 *
 * @author seed419
 */
public class CommandHandler implements CommandExecutor {


    private FoundDiamonds fd;


    public CommandHandler(FoundDiamonds fd) {
        this.fd = fd;
    }

    //TODO better Permissions functionality will fix this

    //TODO IE Has any permission, has light permission, has broadcast permission, etc;



 @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
            PluginUtils.logCommandToConsole(fd, player, commandLabel, args);
        }
        if (((commandLabel.equalsIgnoreCase("fd")) || commandLabel.equalsIgnoreCase("founddiamonds"))) {
            if (args.length == 0) {
                fd.getMenuHandler().printMainMenu(sender);
                return true;
            } else {
                String arg = args[0];
                if (arg.equalsIgnoreCase("admin")) {
                    if (sender instanceof Player) {
                        if (fd.getPermissions().hasPerm(player, "fd.manage.admin.add") || fd.getPermissions().hasPerm(player, "fd.manage.admin.remove")
                                || fd.getPermissions().hasPerm(player, "fd.manage.admin.list")) {
                            fd.getMenuHandler().handleAdminMenu(fd, sender, args);
                        } else {
                            fd.getPermissions().sendPermissionsMessage(player);
                        }
                    } else {
                        fd.getMenuHandler().handleAdminMenu(fd, sender, args);
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("bc") || arg.equalsIgnoreCase("broadcast")) {
                    if (sender instanceof Player) {
                        if (fd.getPermissions().hasPerm(player, "fd.manage.bc.add") || fd.getPermissions().hasPerm(player, "fd.manage.bc.remove")
                                || fd.getPermissions().hasPerm(player, "fd.manage.bc.list")) {
                            fd.getMenuHandler().handleBcMenu(fd, sender, args);
                        } else {
                            fd.getPermissions().sendPermissionsMessage(player);
                        }
                    } else {
                        fd.getMenuHandler().handleBcMenu(fd, sender, args);
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("config")) {
                    if (sender instanceof Player) {
                        if (fd.getPermissions().hasPerm(player, "fd.config")) {
                            if (args.length == 2) {
                                if (args[1].equalsIgnoreCase("2")) {
                                    fd.getMenuHandler().showConfig2(fd, sender);
                                }
                            } else {
                                fd.getMenuHandler().showConfig(fd, sender);
                            }
                        } else {
                            fd.getPermissions().sendPermissionsMessage(player);
                        }
                    }
                } else if (arg.equalsIgnoreCase("debug")) {
                    if (sender instanceof Player) {
                        if (fd.getPermissions().hasPerm(player, "fd.toggle")) {
                            if (args.length == 2) {
                                if (args[1].equalsIgnoreCase("2")) {
                                    fd.getMenuHandler().showConfig2(fd, sender);
                                }
                            } else {
                                fd.getMenuHandler().showConfig(fd, sender);
                            }
                        } else {
                            fd.getPermissions().sendPermissionsMessage(player);
                        }
                    }
                } else if (arg.equalsIgnoreCase("light")) {
                    if (sender instanceof Player) {
                        if (fd.getPermissions().hasPerm(player, "fd.manage.light.add") || fd.getPermissions().hasPerm(player, "fd.manage.light.remove")
                                || fd.getPermissions().hasPerm(player, "fd.manage.light.list")) {
                            fd.getMenuHandler().handleLightMenu(fd, sender, args);
                        } else {
                            fd.getPermissions().sendPermissionsMessage(player);
                        }
                    } else {
                        fd.getMenuHandler().handleLightMenu(fd, sender, args);
                    }
                    return true;
               } else if (arg.equalsIgnoreCase("reload")) {
                    if (sender instanceof Player) {
                        if (fd.getPermissions().hasPerm(player, "fd.reload")) {
                            fd.reloadConfig();
                            fd.saveConfig();
                            sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " Configuration saved and reloaded.");
                        } else {
                            fd.getPermissions().sendPermissionsMessage(player);
                        }
                    } else {
                        fd.reloadConfig();
                        fd.saveConfig();
                        sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " Configuration saved and reloaded.");
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("set")) {
                    if (sender instanceof Player) {
                        if (fd.getPermissions().hasPerm(player, "fd.toggle")) {
                            fd.getMenuHandler().handleSetMenu(fd, sender, args);
                        } else {
                            fd.getPermissions().sendPermissionsMessage(player);
                        }
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("toggle")) {
                    if (!fd.getPermissions().hasPerm(sender, "fd.toggle")) {
                        fd.getPermissions().sendPermissionsMessage(sender);
                    } else {
                        if (args.length == 1) {
                            fd.getMenuHandler().showToggle(sender);
                        } else  if (args.length == 2) {
                            arg = args[1];
                            handleToggle(sender, arg);
                        } else {
                            sender.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " Invalid number of arguments.");
                            sender.sendMessage(ChatColor.RED + "See '/fd toggle' for the list of valid arguments.");
                        }
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("trap")) {
                    if (sender instanceof Player) {
                        if (fd.getPermissions().hasPerm(player, "fd.trap")) {
                            fd.getTrapHandler().handleTrap(player, args);
                        } else {
                            fd.getPermissions().sendPermissionsMessage(player);
                        }
                    } else {
                        sender.sendMessage(Prefix.getChatPrefix() + ChatColor.DARK_RED + " Can't set a trap from the console.");
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("world")) {
                    if (sender instanceof Player) {
                        if (fd.getPermissions().hasPerm(player, "fd.world")) {
                            fd.getWorldHandler().handleWorldMenu(sender, args);
                        } else {
                            fd.getPermissions().sendPermissionsMessage(player);
                        }
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("version")) {
                    fd.getMenuHandler().showVersion(fd, sender);
                    return true;
                } else {
                        sender.sendMessage(Prefix.getChatPrefix() + ChatColor.DARK_RED + " Unrecognized command '"
                                + ChatColor.WHITE + args[0] + ChatColor.DARK_RED + "'");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean handleToggle(CommandSender sender, String arg) {
        if (arg.equalsIgnoreCase("creative")) {
            fd.getConfig().set(Config.disableInCreative, !fd.getConfig().getBoolean(Config.disableInCreative));
            fd.getMenuHandler().printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("ops")) {
            fd.getConfig().set(Config.opsAsFDAdmin, !fd.getConfig().getBoolean(Config.opsAsFDAdmin));
            fd.getMenuHandler().printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("kick")) {
            fd.getConfig().set(Config.kickOnTrapBreak, !fd.getConfig().getBoolean(Config.kickOnTrapBreak));
            fd.getMenuHandler().printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("ban") || arg.equalsIgnoreCase("bans")) {
            fd.getConfig().set(Config.banOnTrapBreak, !fd.getConfig().getBoolean(Config.banOnTrapBreak));
            fd.getMenuHandler().printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("items")) {
            fd.getConfig().set(Config.itemsForFindingDiamonds, !fd.getConfig().getBoolean(Config.itemsForFindingDiamonds));
            fd.getMenuHandler().printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("logging")) {
            fd.getConfig().set(Config.logDiamondBreaks, !fd.getConfig().getBoolean(Config.logDiamondBreaks));
            fd.getMenuHandler().printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("spells")) {
            fd.getConfig().set(Config.potionsForFindingDiamonds, !fd.getConfig().getBoolean(Config.potionsForFindingDiamonds));
            fd.getMenuHandler().printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("cleanlog")) {
            fd.getConfig().set(Config.cleanLog, !fd.getConfig().getBoolean(Config.cleanLog));
            if (!FileHandler.getCleanLog().exists()) {
                try {
                    boolean successful = FileHandler.getCleanLog().createNewFile();
                    if (successful) {sender.sendMessage(Prefix.getChatPrefix() + ChatColor.DARK_GREEN +" Cleanlog created.");}
                    } catch (IOException ex) {
                    sender.sendMessage(Prefix.getChatPrefix() + ChatColor.DARK_RED + " Uh-oh...couldn't create CleanLog.txt");
                    fd.getLog().severe("Failed to create CleanLog.txt");
                }
            }
            fd.getMenuHandler().printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("nick") || arg.equalsIgnoreCase("nicks")) {
            fd.getConfig().set(Config.useNick, !fd.getConfig().getBoolean(Config.useNick));
            fd.getMenuHandler().printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("debug")) {
            fd.getConfig().set(Config.debug, !fd.getConfig().getBoolean(Config.debug));
            fd.getMenuHandler().printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("2")) {
            fd.getMenuHandler().showToggle2(sender);
        } else {
            sender.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " Argument '" + arg + "' unrecognized.");
            sender.sendMessage(ChatColor.RED + "See '/fd toggle' for the list of valid arguments.");
            return false;
        }
        return true;
    }

}
