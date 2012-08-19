/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seed419.founddiamonds;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.seed419.founddiamonds.sql.MySQL;

/**
 *
 * @author seed419
 */
public class CommandHandler implements CommandExecutor {


    private FoundDiamonds fd;
    private MySQL mysql;
    private WorldManager wm;
    private Trap trap;


    public CommandHandler(FoundDiamonds fd, MySQL mysql, WorldManager wm, Trap trap) {
        this.fd = fd;
        this.mysql = mysql;
        this.wm = wm;
        this.trap = trap;
    }



 @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
            StringBuilder sb = new StringBuilder();
            sb.append(commandLabel).append(" ");
            if (args.length > 0) {
                for (String x : args) {
                    sb.append(x).append(" ");
                }
            }
            String cmd = sb.toString();
            fd.getLog().info("[PLAYER_COMMAND] " + player.getName() + ": /" + cmd);
        }
        if (((commandLabel.equalsIgnoreCase("fd")) || commandLabel.equalsIgnoreCase("founddiamonds"))) {
            if (args.length == 0) {
                Menu.printMainMenu(fd, sender);
                return true;
            } else {
                String arg = args[0];
                if (arg.equalsIgnoreCase("admin")) {
                    if (sender instanceof Player) {
                        if (fd.hasPerms(player, "fd.manage.admin.add") || fd.hasPerms(player, "fd.manage.admin.remove")
                                || fd.hasPerms(player, "fd.manage.admin.list")) {
                            Menu.handleAdminMenu(fd, sender, args);
                        } else {
                            sendPermissionsMessage(player);
                        }
                    } else {
                        Menu.handleAdminMenu(fd, sender, args);
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("bc") || arg.equalsIgnoreCase("broadcast")) {
                    if (sender instanceof Player) {
                        if (fd.hasPerms(player, "fd.manage.bc.add") || fd.hasPerms(player, "fd.manage.bc.remove")
                                || fd.hasPerms(player, "fd.manage.bc.list")) {
                            Menu.handleBcMenu(fd, sender, args);
                        } else {
                            sendPermissionsMessage(player);
                        }
                    } else {
                        Menu.handleBcMenu(fd, sender, args);
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("config")) {
                    if (sender instanceof Player) {
                        if (fd.hasPerms(player, "fd.config")) {
                            if (args.length == 2) {
                                if (args[1].equalsIgnoreCase("2")) {
                                    Menu.showConfig2(fd, sender);
                                }
                            } else {
                                Menu.showConfig(fd, sender);
                            }
                        } else {
                            sendPermissionsMessage(player);
                        }
                    }
                } else if (arg.equalsIgnoreCase("debug")) {
                    if (sender instanceof Player) {
                        if (fd.hasPerms(player, "fd.toggle")) {
                            if (args.length == 2) {
                                if (args[1].equalsIgnoreCase("2")) {
                                    Menu.showConfig2(fd, sender);
                                }
                            } else {
                                Menu.showConfig(fd, sender);
                            }
                        } else {
                            sendPermissionsMessage(player);
                        }
                    }
                } else if (arg.equalsIgnoreCase("light")) {
                    if (sender instanceof Player) {
                        if (fd.hasPerms(player, "fd.manage.light.add") || fd.hasPerms(player, "fd.manage.light.remove")
                                || fd.hasPerms(player, "fd.manage.light.list")) {
                            Menu.handleLightMenu(fd, sender, args);
                        } else {
                            sendPermissionsMessage(player);
                        }
                    } else {
                        Menu.handleLightMenu(fd, sender, args);
                    }
                    return true;
               } else if (arg.equalsIgnoreCase("reload")) {
                    if (sender instanceof Player) {
                        if (fd.hasPerms(player, "fd.reload")) {
                            fd.reloadConfig();
                            fd.saveConfig();
                            sender.sendMessage(fd.getPrefix() + ChatColor.AQUA + " Configuration saved and reloaded.");
                        } else {
                            sendPermissionsMessage(player);
                        }
                    } else {
                        fd.reloadConfig();
                        fd.saveConfig();
                        sender.sendMessage(fd.getPrefix() + ChatColor.AQUA + " Configuration saved and reloaded.");
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("set")) {
                    if (sender instanceof Player) {
                        if (fd.hasPerms(player, "fd.toggle")) {
                            Menu.handleSetMenu(fd, sender, args);
                        } else {
                            sendPermissionsMessage(player);
                        }
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("stats")) {
                    if (sender instanceof Player) {
                        mysql.printStats(player);
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("toggle")) {
                    if (!fd.hasPerms(sender, "fd.toggle")) {
                        sendPermissionsMessage(sender);
                    } else {
                        if (args.length == 1) {
                            Menu.showToggle(sender);
                        } else  if (args.length == 2) {
                            arg = args[1];
                            handleToggle(sender, arg);
                        } else {
                            sender.sendMessage(fd.getPrefix() + ChatColor.RED + " Invalid number of arguments.");
                            sender.sendMessage(ChatColor.RED + "See '/fd toggle' for the list of valid arguments.");
                        }
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("trap")) {
                    if (sender instanceof Player) {
                        if (fd.hasPerms(player, "fd.trap")) {
                            trap.handleTrap(player, args);
                        } else {
                            sendPermissionsMessage(player);
                        }
                    } else {
                        sender.sendMessage(fd.getPrefix() + ChatColor.DARK_RED + " Can't set a trap from the console.");
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("world")) {
                    if (sender instanceof Player) {
                        if (fd.hasPerms(player, "fd.world")) {
                            wm.handleWorldMenu(sender, args);
                        } else {
                            sendPermissionsMessage(player);
                        }
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("version")) {
                    Menu.showVersion(sender);
                    return true;
                } else if (arg.equalsIgnoreCase("diamond") || arg.equalsIgnoreCase("gold")
                        || arg.equalsIgnoreCase("lapis") || arg.equalsIgnoreCase("iron")
                        || arg.equalsIgnoreCase("redstone") || arg.equalsIgnoreCase("coal")) {
                    if (fd.getConfig().getBoolean(Config.mysqlEnabled)) {
                        if (args[0].equalsIgnoreCase("diamond")) {
                            mysql.handleTop(sender, "diamond");
                        } else if (args[0].equalsIgnoreCase("gold")) {
                            mysql.handleTop(sender, "gold");
                        } else if (args[0].equalsIgnoreCase("lapis")) {
                            mysql.handleTop(sender, "lapis");
                        } else if (args[0].equalsIgnoreCase("iron")) {
                            mysql.handleTop(sender, "iron");
                        } else if (args[0].equalsIgnoreCase("coal")) {
                            mysql.handleTop(sender, "coal");
                        } else if (args[0].equalsIgnoreCase("redstone")) {
                            mysql.handleTop(sender, "redstone");
                        }
                    }
                    return true;
                } else {
                        sender.sendMessage(fd.getPrefix() + ChatColor.DARK_RED + " Unrecognized command '"
                                + ChatColor.WHITE + args[0] + ChatColor.DARK_RED + "'");
                    return true;
                }
            }
        }
        return false;
    }


    public void sendPermissionsMessage(CommandSender sender) {
        sender.sendMessage(fd.getPrefix() + ChatColor.RED + " You don't have permission to do that.");
        fd.getLog().warning(sender.getName() + " was denied access to a command.");
    }

    private boolean handleToggle(CommandSender sender, String arg) {
        if (arg.equalsIgnoreCase("creative")) {
            fd.getConfig().set(Config.disableInCreative, !fd.getConfig().getBoolean(Config.disableInCreative));
            Menu.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("ops")) {
            fd.getConfig().set(Config.opsAsFDAdmin, !fd.getConfig().getBoolean(Config.opsAsFDAdmin));
            Menu.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("kick")) {
            fd.getConfig().set(Config.kickOnTrapBreak, !fd.getConfig().getBoolean(Config.kickOnTrapBreak));
            Menu.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("ban") || arg.equalsIgnoreCase("bans")) {
            fd.getConfig().set(Config.banOnTrapBreak, !fd.getConfig().getBoolean(Config.banOnTrapBreak));
            Menu.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("trapalerts")) {
            fd.getConfig().set(Config.adminAlertsOnAllTrapBreaks, !fd.getConfig().getBoolean(Config.adminAlertsOnAllTrapBreaks));
            Menu.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("items")) {
            fd.getConfig().set(Config.itemsForFindingDiamonds, !fd.getConfig().getBoolean(Config.itemsForFindingDiamonds));
            Menu.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("logging")) {
            fd.getConfig().set(Config.logDiamondBreaks, !fd.getConfig().getBoolean(Config.logDiamondBreaks));
            Menu.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("spells")) {
            fd.getConfig().set(Config.potionsForFindingDiamonds, !fd.getConfig().getBoolean(Config.potionsForFindingDiamonds));
            Menu.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("cleanlog")) {
            fd.getConfig().set(Config.cleanLog, !fd.getConfig().getBoolean(Config.cleanLog));
            if (!FileHandler.getCleanLog().exists()) {
                try {
                    boolean successful = FileHandler.getCleanLog().createNewFile();
                    if (successful) {sender.sendMessage(fd.getPrefix() + ChatColor.DARK_GREEN +" Cleanlog created.");}
                    } catch (IOException ex) {
                    sender.sendMessage(fd.getPrefix() + ChatColor.DARK_RED + " Uh-oh...couldn't create CleanLog.txt");
                    Logger.getLogger(FoundDiamonds.class.getName()).log(Level.SEVERE, "Failed to create CleanLog file.", ex);
                }
            }
            Menu.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("nick") || arg.equalsIgnoreCase("nicks")) {
            fd.getConfig().set(Config.useNick, !fd.getConfig().getBoolean(Config.useNick));
            Menu.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("debug")) {
            fd.getConfig().set(Config.debug, !fd.getConfig().getBoolean(Config.debug));
            Menu.printSaved(fd, sender);
        } else if (arg.equalsIgnoreCase("prefix")) {
            sender.sendMessage(fd.getPrefix() + ChatColor.DARK_RED + " Prefix is now a part of the broadcast message.");
            sender.sendMessage(fd.getPrefix() + ChatColor.DARK_RED + " Please modify it in the config file.");
        } else if (arg.equalsIgnoreCase("2")) {
            Menu.showToggle2(sender);
        } else {
            sender.sendMessage(fd.getPrefix() + ChatColor.RED + " Argument '" + arg + "' unrecognized.");
            sender.sendMessage(ChatColor.RED + "See '/fd toggle' for the list of valid arguments.");
            return false;
        }
        return true;
    }

}
