package org.seed419.FoundDiamonds;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.seed419.FoundDiamonds.Listeners.*;
import org.seed419.FoundDiamonds.Metrics.MetricsLite;

/* TODO
* AdminMap memory?
* VERSITALE LISTS - in progress
* Smarter trap blocks - remember material.
* Finish set menu, integrate with main menu
* Look into pulling stats from MC client?  Or MySQL?
* Finish config and toggle
* /fd top ?
* Re-implement admin messages
* implement light level block list

/*
* Changelog:
* Implemented versatile lists that can be edited to whatever you want!
* Made fd log player commands to the console, including failed command attempts.
* Fixed a terrible admin message bug :C
* Moved prefix to broadcast message as @Prefix@ instead of having a separate option for it.
* Fixed color bug when using nicknames in broadcast (now looks way better imo)
* Fixed a bug with random items not even working -.-
* Added 3 new potions.
* Made potion messages more descriptive, and configurable.
* Fixed bugs with menus and made other improvements to them.
* Traps can now be set without using '_' characters, and just spaces (/fd trap gold ore)
* Fixed a bug with /fd reload   (The fuck was the bug?)
* Refactored a TON of code.

*
  */

/*  Attribute Only (Public) License
        Version 0.a3, July 11, 2011

    Copyright (C) 2012 Blake Bartenbach <seed419@gmail.com> (@seed419)

    Anyone is allowed to copy and distribute verbatim or modified
    copies of this license document and altering is allowed as long
    as you attribute the author(s) of this license document / files.

    ATTRIBUTE ONLY PUBLIC LICENSE
    TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

      1. Attribute anyone attached to the license document.
         * Do not remove pre-existing attributes.

         Plausible attribution methods:
            1. Through comment blocks.
            2. Referencing on a site, wiki, or about page.

      2. Do whatever you want as long as you don't invalidate 1.


@license AOL v.a3 <http://aol.nexua.org>*/



public class FoundDiamonds extends JavaPlugin {

    private final File mainDir = new File("plugins/FoundDiamonds/");
    private final File logs = new File(getDataFolder(), "log.txt");
    private final File traps = new File(getDataFolder(), ".traplocations");
    private final File cleanLog = new File(getDataFolder(), "cleanlog.txt");
    private final File configFile = new File(getDataFolder(), "config.yml");
    private final File placed = new File(getDataFolder(), ".placed");
    private final static String prefix = "[FD]";
    private final static String adminPrefix = ChatColor.RED + "[FD Admin]" + ChatColor.YELLOW;
    private final static String debugPrefix = "[FD Debug] ";
    private final static String loggerPrefix = "[FoundDiamonds]";
    private List<Node> broadcastedBlocks = new LinkedList<Node>();
    private final List<Node> adminMessageBlocks = new LinkedList<Node>();
    private final List<Node> lightLevelBlocks = new LinkedList<Node>();
    private final List<Location> trapBlocks = new LinkedList<Location>();
    private static final List<Location> announcedBlocks = new LinkedList<Location>();
    private static final List<Location> placedBlocks = new LinkedList<Location>();
    private final HashMap<Player, Boolean> adminMessagePlayers = new HashMap<Player, Boolean>();
    private final HashMap<Player, Boolean> jumpPotion = new HashMap<Player,Boolean>();
    private final static Logger log = Logger.getLogger("FoundDiamonds");
    private BlockBreakListener breakListener;
    private BlockPlaceListener placeListener;
    private Config config;
    private JoinListener join;
    private QuitListener quit;
    private PlayerDamageListener damage;
    private static String pluginName ;
    private PluginDescriptionFile pdf;
    private boolean printed = false;
    private final static int togglePages = 2;
    private final static int configPages = 2;


    @Override
    public void onEnable() {
        config = new Config(this);

        checkFiles();
        checkWorlds();
        loadBroadcastedBlocksFromConfig();

        join = new JoinListener(this);
        quit = new QuitListener(this);
        breakListener = new BlockBreakListener(this);
        placeListener = new BlockPlaceListener(this);
        damage = new PlayerDamageListener(this);

	    PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this.breakListener, this);
        pm.registerEvents(this.join, this);
        pm.registerEvents(this.quit, this);
        pm.registerEvents(this.placeListener, this);
        pm.registerEvents(damage, this);

	    pdf = this.getDescription();
	    pluginName = pdf.getName();

        startMetrics();

        log.info(MessageFormat.format("[{0}] Enabled", pluginName));
    }

    @Override
    public void onDisable() {
        broadcastedBlocks = null;

        log.info(MessageFormat.format("[{0}] Saving all data...", pluginName));
        String info = "This file stores your trap block locations.";
        String info2 = "If you have any issues with traps - feel free to delete this file.";
        boolean temp = writeBlocksToFile(traps, trapBlocks, info, info2);
        String info5 = "This file stores blocks that would be announced that players placed";
        String info6 = "If you'd like to announce these placed blocks, feel free to delete this file.";
        boolean temp3 = writeBlocksToFile(placed, placedBlocks, info5, info6);
        if (temp && temp3) {
            log.info(MessageFormat.format("[{0}] Data successfully saved.", pluginName));
        } else {
            log.warning(MessageFormat.format("[{0}] Couldn't save blocks to files!", pluginName));
            log.warning(MessageFormat.format("[{0}] You could try deleting .placed and .traplocations", pluginName));
        }
        log.info(MessageFormat.format("[{0}] Disabled", pluginName));
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
            log.info("[PLAYER_COMMAND] " + player.getName() + ": /" + cmd);
        }
        if (((commandLabel.equalsIgnoreCase("fd")) || commandLabel.equalsIgnoreCase("founddiamonds"))) {
            if (args.length == 0) {
                Menu.printMainMenu(this, sender);
                return true;
            } else {
                String arg = args[0];
                if (arg.equalsIgnoreCase("admin")) {
                    if (sender instanceof Player) {
                        if (hasPerms(player, "fd.manage.admin.add") || hasPerms(player, "fd.manage.admin.remove")
                                || hasPerms(player, "fd.manage.admin.list")) {
                            handleAdminMenu(sender, args);
                        } else {
                            sendPermissionsMessage(player);
                        }
                    } else {
                        handleAdminMenu(sender, args);
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("bc")) {
                    if (sender instanceof Player) {
                        if (hasPerms(player, "fd.manage.bc.add") || hasPerms(player, "fd.manage.bc.remove")
                                || hasPerms(player, "fd.manage.bc.list")) {
                            handleBcMenu(sender, args);
                        } else {
                            sendPermissionsMessage(player);
                        }
                    } else {
                        handleBcMenu(sender, args);
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("config")) {
                    if (sender instanceof Player) {
                        if (hasPerms(player, "fd.config")) {
                            if (args.length == 2) {
                                if (args[1].equalsIgnoreCase("2")) {
                                    Menu.showConfig2(this, sender);
                                }
                            } else {
                                Menu.showConfig(this, sender);
                            }
                        } else {
                            sendPermissionsMessage(player);
                        }
                    }
                } else if (arg.equalsIgnoreCase("light")) {
                    if (sender instanceof Player) {
                        if (hasPerms(player, "fd.manage.light.add") || hasPerms(player, "fd.manage.light.remove")
                                || hasPerms(player, "fd.manage.light.list")) {
                            handleLightMenu(sender, args);
                        } else {
                            sendPermissionsMessage(player);
                        }
                    } else {
                        handleLightMenu(sender, args);
                    }
                    return true;
               } else if (arg.equalsIgnoreCase("reload")) {
                    if (sender instanceof Player) {
                        if (hasPerms(player, "fd.reload")) {
                            reloadConfig();
                            saveConfig();
                            sender.sendMessage(getPrefix() + ChatColor.AQUA + " Configuration saved and reloaded.");
                        } else {
                            sendPermissionsMessage(player);
                        }
                    } else {
                        saveConfig();
                        sender.sendMessage(getPrefix() + ChatColor.AQUA + " Configuration saved and reloaded.");
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("set")) {
                    if (sender instanceof Player) {
                        if (hasPerms(player, "fd.toggle")) {
                            Menu.handleSetMenu(this, sender, args);
                        } else {
                            sendPermissionsMessage(player);
                        }
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("toggle")) {
                    if (sender instanceof Player) {
                        if (hasPerms(player, "fd.toggle")) {
                            if (args.length == 1) {
                                Menu.showToggle(sender);
                            } else  if (args.length == 2) {
                                arg = args[1];
                                handleToggle(sender, arg);
                            } else {
                                sender.sendMessage(getPrefix() + ChatColor.RED + " Invalid number of arguments.");
                                sender.sendMessage(ChatColor.RED + "See '/fd toggle' for the list of valid arguments.");
                            }
                        } else {
                            sendPermissionsMessage(player);
                        }
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("trap")) {
                    if (sender instanceof Player) {
                        if (hasPerms(player, "fd.trap")) {
                            handleTrap(player, args);
                        } else {
                            sendPermissionsMessage(player);
                        }
                    } else {
                        sender.sendMessage(getPrefix() + ChatColor.DARK_RED + " Can't set a trap from the console.");
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("world")) {
                    if (sender instanceof Player) {
                        if (hasPerms(player, "fd.world")) {
                            handleWorldMenu(sender, args);
                        } else {
                            sendPermissionsMessage(player);
                        }
                    }
                    return true;
                } else {
                        sender.sendMessage(getPrefix() + ChatColor.DARK_RED + " Unrecognized command '"
                                + ChatColor.WHITE + args[0] + ChatColor.DARK_RED + "'");
                    return true;
                }
            }
        }
        return false;
    }




    /*
     * World handlers
     */
    private void handleWorldMenu(CommandSender sender, String[] args) {
        if (args.length == 1) {
            Menu.showWorldMenu(sender);
        } else if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("list")) {
                if (args.length == 2) {
                    printEnabledWorlds(sender);
                } else {
                    sender.sendMessage(getPrefix() + ChatColor.DARK_RED + " Usage: /fd world list");
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
                    sender.sendMessage(getPrefix() + ChatColor.DARK_RED + " Usage: /fd world add <worldname>");
                }
            } else if (args[1].equalsIgnoreCase("remove")) {
                if (getConfig().getStringList(Config.enabledWorlds).contains(args[2])) {
                    List<?> worldList = getConfig().getList(Config.enabledWorlds);
                    worldList.remove(args[2]);
                    getConfig().set(Config.enabledWorlds, worldList);
                    sender.sendMessage(getPrefix() + ChatColor.AQUA + " World '" + args[2] +"' removed.");
                    saveConfig();
                } else {
                    sender.sendMessage(getPrefix() + ChatColor.DARK_RED + " World '" + args[2] +"' isn't an enabled world.");
                }
            } else {
                sender.sendMessage(getPrefix() + ChatColor.DARK_RED + " Unrecognized command.  See /fd world");
            }
        }
    }

    private void validateWorld(CommandSender sender, String worldName) {
        List<World> temp = getServer().getWorlds();
        for (World w : temp) {
            if (w.getName().equals(worldName)) {
                @SuppressWarnings("unchecked")
                Collection<String> worldList = (Collection<String>) getConfig().getList(Config.enabledWorlds);
                if (!worldList.contains(worldName)) {
                    worldList.add(worldName);
                    getConfig().set(Config.enabledWorlds, worldList);
                    sender.sendMessage(getPrefix() + ChatColor.AQUA + " World '" + worldName + "' added.");
                    saveConfig();
                    return;
                } else {
                    sender.sendMessage(getPrefix() + ChatColor.DARK_RED + " That world is already enabled.");
                    return;
                }
            }
        }
        sender.sendMessage(getPrefix() + ChatColor.DARK_RED + " Couldn't find a world with the name '" + worldName + "'");
    }

    private void printEnabledWorlds(CommandSender sender) {
        sender.sendMessage(getPrefix() + ChatColor.AQUA + " [Enabled Worlds]");
        for (Iterator<String> it = getConfig().getStringList(Config.enabledWorlds).iterator(); it.hasNext();) {
            String x = it.next();
            sender.sendMessage("    - " + x);
        }
    }

    private void checkWorlds() {
        if (getConfig().getList(Config.enabledWorlds) == null) {
            addAllWorlds();
        }
    }

    private void addAllWorlds() {
        List<World> worldList = getServer().getWorlds();
        List<String> worldNames = new LinkedList<String>();
        for (World w : worldList) {
            if (!w.getName().equalsIgnoreCase("world_nether") && !w.getName().equalsIgnoreCase("world_the_end")) {
                worldNames.add(w.getName());
            }
        }
        getConfig().set(Config.enabledWorlds, worldNames);
        saveConfig();
    }




    /*
     * Admin Messages
     */
    private void reloadAdminMessageMap(Player player) {
        if ((adminMessagePlayers.containsKey(player)) && adminMessagePlayers.get(player)) {
            adminMessagePlayers.put(player, false);
        } else if ((adminMessagePlayers.containsKey(player)) && (!adminMessagePlayers.get(player))) {
            adminMessagePlayers.put(player, true);
        } else {
            adminMessagePlayers.put(player, true);
        }
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public HashMap<Player,Boolean> getAdminMessageMap() {
        return adminMessagePlayers;
    }




    /*
     * Configuration File
     */
    public void loadYaml() {
        try {
            getConfig().options().copyDefaults(true);
            getConfig().load(configFile);
        } catch (FileNotFoundException ex) {
            log.severe(MessageFormat.format("[{0}] Couldn't find config.yml {1}", pluginName, ex));
        } catch (IOException ex) {
            log.severe(MessageFormat.format("[{0}] Unable to load configuration file {1}", pluginName, ex));
        } catch (InvalidConfigurationException ex) {
            log.severe(MessageFormat.format("[{0}] Unable to load configuration file {1}", pluginName, ex));
        }
    }




    /*
     * Trap Blocks
     */
    private void handleTrap(Player player, String[] args) {
        Location playerLoc = player.getLocation();
        Material trap;
        String item;
        if (args.length == 1) {
            trap = Material.DIAMOND_ORE;
            item = "Diamond ore";
        } else if (args.length == 2) {
            item = args[1];
            trap = Material.matchMaterial(item);
        } else if (args.length == 3) {
            item = args[1] + "_" + args[2];
            trap = Material.matchMaterial(item);
        } else {
            player.sendMessage(getPrefix() + ChatColor.RED + " Invalid number of arguments");
            player.sendMessage(ChatColor.RED + "Is it a block and a valid item? Try /fd trap gold ore");
            return;
        }
        if (trap != null && trap.isBlock()) {
            getTrapLocations(player, playerLoc, trap);
        } else {
            player.sendMessage(getPrefix() + ChatColor.RED + " Unable to set a trap with '" + item + "'");
            player.sendMessage(ChatColor.RED + "Is it a block and a valid item? Try /fd trap gold ore");
        }
    }

    private void getTrapLocations(Player player, Location playerLoc, Material trap) {
        int x = playerLoc.getBlockX();
        int y = playerLoc.getBlockY();
        int maxHeight = player.getWorld().getMaxHeight();
        if ((y - 2) < 0) {
            player.sendMessage(getPrefix() + ChatColor.RED + " I can't place a trap down there, sorry.");
            return;
        } else if ((y - 1) > maxHeight) {
            player.sendMessage(getPrefix() + ChatColor.RED + " I can't place a trap this high, sorry.");
            return;
        }
        int z = playerLoc.getBlockZ();
        World world = player.getWorld();
        int randomnumber = (int)(Math.random() * 100);
        if ((randomnumber >= 0) && randomnumber < 50) {
            Block block1 = world.getBlockAt(x, y - 1, z);
            Block block2 = world.getBlockAt(x, y - 2, z + 1);
            Block block3 = world.getBlockAt(x - 1, y - 2, z);
            Block block4 = world.getBlockAt(x, y - 2, z);
            handleTrapBlocks(player, trap, block1, block2, block3, block4);
        } else if (randomnumber >= 50) {
            Block block1 = world.getBlockAt(x, y - 1, z);
            Block block2 = world.getBlockAt(x - 1, y - 2, z);
            Block block3 = world.getBlockAt(x , y - 2, z);
            Block block4 = world.getBlockAt(x -1, y - 1, z);
            handleTrapBlocks(player, trap, block1, block2, block3, block4);
        }
    }

    public void handleTrapBlocks(Player player, Material trap, Block block1, Block block2, Block block3, Block block4) {
        trapBlocks.add(block1.getLocation());
        trapBlocks.add(block2.getLocation());
        trapBlocks.add(block3.getLocation());
        trapBlocks.add(block4.getLocation());
        block1.setType(trap);
        block2.setType(trap);
        block3.setType(trap);
        block4.setType(trap);
        player.sendMessage(getPrefix() + ChatColor.AQUA + " Trap set using " + trap.name().toLowerCase().replace("_", " "));
    }


    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<Location> getTrapBlocks() {
        return trapBlocks;
    }




    /*
     * File handlers
     */
    private boolean writeBlocksToFile(File file, List<Location> blockList, String info, String info2) {
        if (blockList.size() > 0) {
            if (this.getDataFolder().exists()) {
                PrintWriter out = null;
                try {
                    file.createNewFile();
                    out =  new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
                    out.write("# " + info);
                    out.println();
                    out.write("# " + info2);
                    out.println();
                    for (Iterator<Location> it = blockList.iterator(); it.hasNext();) {
                        Location m = it.next();
                        out.write(m.getWorld().getName() + ";" + m.getX() + ";" + m.getY() + ";" + m.getZ());
                        out.println();
                    }
                } catch (IOException ex) {
                    log.severe(MessageFormat.format("[{0}] Error writing blocks to file!", pluginName, file.getName()));
                } finally {
                    close(out);
                }
                return true;
            } else {
                if (!printed) {
                    log.warning(MessageFormat.format("[{0}] Plugin folder not found.  Did you delete it?", pluginName));
                    printed = true;
                    return false;
                }
                return false;
            }
        } else {
            if (file.exists()) {
                file.delete();
            }
            return true;
        }
    }

    private void readBlocksFromFile(File file, List<Location> list) {
        BufferedReader b = null;
        try {
            b = new BufferedReader(new FileReader(file));
            String strLine = b.readLine();
            while (strLine != null) {
                if (!strLine.startsWith("#")) {
                    try {
                        String[] fs = strLine.split(";");
                        Location lo = new Location(getServer().getWorld(fs[0]), Double.parseDouble(fs[1]),
                            Double.parseDouble(fs[2]), Double.parseDouble(fs[3]));
                        list.add(lo);
                    } catch (Exception ex) {
                        log.severe(MessageFormat.format("[{0}] Invalid block in file.  Please delete the FoundDiamonds folder.", pluginName));
                    }
                }
                strLine = b.readLine();
            }
        } catch (Exception ex) {
            log.severe(MessageFormat.format("[{0}] Unable to read blocks from file, {1}", pluginName, ex));
        } finally {
            close(b);
        }
    }

    private void checkFiles() {
        if (!mainDir.exists()) {
            mainDir.mkdir();
        }
        if (!logs.exists()) {
            try {
                logs.createNewFile();
            } catch (IOException ex) {
                log.severe(MessageFormat.format("[{0}] Unable to create log file, {1}", pluginName, ex));
       	    }
         }
         if (traps.exists()) {
             readBlocksFromFile(traps, trapBlocks);
         }
         if (placed.exists()) {
             readBlocksFromFile(placed, placedBlocks);
         }
         if (!configFile.exists()) {
             config.load();
             addAllWorlds();
         } else {
             loadYaml();
         }
         if (getConfig().getBoolean(Config.cleanLog)) {
             try {
                 cleanLog.createNewFile();
             } catch (IOException ex) {
                 Logger.getLogger(FoundDiamonds.class.getName()).log(Level.SEVERE, "Couldn't create clean log file, ", ex);
             }
         }
    }

    public File getLogFile() {
        return logs;
    }

    public File getCleanLog() {
        return cleanLog;
    }

    public void close(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ex) {
                Logger.getLogger(FoundDiamonds.class.getName()).log(Level.SEVERE, "Couldn't close a stream, ", ex);
            }
        }
    }




    /*
     * Enabled block handlers
     */
    public List<Node> getBroadcastedBlocks() {
        return Collections.unmodifiableList(broadcastedBlocks);
    }

    public List<Node> getAdminMessageBlocks() {
        return Collections.unmodifiableList(adminMessageBlocks);
    }

    public List<Node> getLightLevelBlocks() {
        return Collections.unmodifiableList(lightLevelBlocks);
    }




    /*
     * Toggle handler
     */
    private boolean handleToggle(CommandSender sender, String arg) {
        if (arg.equalsIgnoreCase("creative")) {
            getConfig().set(Config.disableInCreative, !getConfig().getBoolean(Config.disableInCreative));
            Menu.printSaved(this, sender);
        } else if (arg.equalsIgnoreCase("admin") || arg.equalsIgnoreCase("adminmessages")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (hasPerms(player, "fd.admin")) {
                    reloadAdminMessageMap(player);
                    if (adminMessagePlayers.get(player)) {
                        player.sendMessage(getPrefix() + ChatColor.AQUA + " Admin messages are " + ChatColor.DARK_GREEN + "ON");
                    } else {
                        player.sendMessage(getPrefix() + ChatColor.AQUA + " Admin messages are " + ChatColor.RED + "OFF");
                    }
                } else {
                    sendPermissionsMessage(player);
                }
            } else {
                sender.sendMessage(getPrefix() + ChatColor.DARK_RED + " Admin messages cannot be toggled from the console.");
            }
        } else if (arg.equalsIgnoreCase("ops")) {
            getConfig().set(Config.opsAsFDAdmin, !getConfig().getBoolean(Config.opsAsFDAdmin));
            Menu.printSaved(this, sender);
        } else if (arg.equalsIgnoreCase("kick")) {
            getConfig().set(Config.kickOnTrapBreak, !getConfig().getBoolean(Config.kickOnTrapBreak));
            Menu.printSaved(this, sender);
        } else if (arg.equalsIgnoreCase("ban") || arg.equalsIgnoreCase("bans")) {
            getConfig().set(Config.banOnTrapBreak, !getConfig().getBoolean(Config.banOnTrapBreak));
            Menu.printSaved(this, sender);
        } else if (arg.equalsIgnoreCase("trapalerts")) {
            getConfig().set(Config.adminAlertsOnAllTrapBreaks, !getConfig().getBoolean(Config.adminAlertsOnAllTrapBreaks));
            Menu.printSaved(this, sender);
        } else if (arg.equalsIgnoreCase("items")) {
            getConfig().set(Config.itemsForFindingDiamonds, !getConfig().getBoolean(Config.itemsForFindingDiamonds));
            Menu.printSaved(this, sender);
        } else if (arg.equalsIgnoreCase("logging")) {
            getConfig().set(Config.logDiamondBreaks, !getConfig().getBoolean(Config.logDiamondBreaks));
            Menu.printSaved(this, sender);
        } else if (arg.equalsIgnoreCase("spells")) {
            getConfig().set(Config.potionsForFindingDiamonds, !getConfig().getBoolean(Config.potionsForFindingDiamonds));
            Menu.printSaved(this, sender);
        } else if (arg.equalsIgnoreCase("cleanlog")) {
            getConfig().set(Config.cleanLog, !getConfig().getBoolean(Config.cleanLog));
            if (!cleanLog.exists()) {
                try {
                    cleanLog.createNewFile();
                    sender.sendMessage(getPrefix() + ChatColor.DARK_GREEN +" Cleanlog created.");
                } catch (IOException ex) {
                    sender.sendMessage(getPrefix() + ChatColor.DARK_RED + " Uh-oh...couldn't create CleanLog.txt");
                    Logger.getLogger(FoundDiamonds.class.getName()).log(Level.SEVERE, "Failed to create CleanLog file.", ex);
                }
            }
            Menu.printSaved(this, sender);
        } else if (arg.equalsIgnoreCase("nick") || arg.equalsIgnoreCase("nicks")) {
            getConfig().set(Config.useNick, !getConfig().getBoolean(Config.useNick));
            Menu.printSaved(this, sender);
        } else if (arg.equalsIgnoreCase("debug")) {
            getConfig().set(Config.debug, !getConfig().getBoolean(Config.debug));
            Menu.printSaved(this, sender);
        } else if (arg.equalsIgnoreCase("prefix")) {
            sender.sendMessage(getPrefix() + ChatColor.DARK_RED + " Prefix is now a part of the broadcast message.");
            sender.sendMessage(getPrefix() + ChatColor.DARK_RED + " Please modify it in the config file.");
        } else if (arg.equalsIgnoreCase("2")) {
            Menu.showToggle2(sender);
        } else {
            sender.sendMessage(getPrefix() + ChatColor.RED + " Argument '" + arg + "' unrecognized.");
            sender.sendMessage(ChatColor.RED + "See '/fd toggle' for the list of valid arguments.");
            return false;
        }
        return true;
    }




    /*
     * Placed blocks
     */
    public static void addToPlacedBlocks(Location w) {
        placedBlocks.add(w);
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public static List<Location> getPlacedBlocks() {
        return placedBlocks;
    }




    /*
     * Misc
     */
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public static List<Location> getAnnouncedBlocks() {
        return announcedBlocks;
    }

    public static boolean isRedstone(Block m) {
        return (m.getType() == Material.REDSTONE_ORE || m.getType() == Material.GLOWING_REDSTONE_ORE);
    }

    public boolean hasPerms(Player player, String permission) {
        return (player.hasPermission(permission) || (getConfig().getBoolean(Config.opsAsFDAdmin) && player.isOp()));
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public HashMap<Player, Boolean> getJumpPotion() {
        return jumpPotion;
    }

    public static int getTogglePages() {
        return togglePages;
    }

    public static int getConfigPages() {
        return configPages;
    }

    public static void sendPermissionsMessage(Player player) {
        player.sendMessage(getPrefix() + ChatColor.RED + " You don't have permission to do that.");
        log.warning( player.getName() + " was denied access to a command.");
    }



    /*
     * Prefix
     */
    public static String getPrefix() {
        return prefix;
    }

    public static String getAdminPrefix() {
        return adminPrefix;
    }

    public static String getDebugPrefix() {
        return debugPrefix;
    }

    public static String getLoggerPrefix() {
        return loggerPrefix;
    }




    /*
     * Versatile lists
     */
    public List<?> getVersatileList(String configLoc) {
        return Collections.unmodifiableList(getConfig().getList(configLoc));
    }

    private void loadBroadcastedBlocksFromConfig() {
        if (getConfig().getList(Config.broadcastedBlocks) == null) {
            loadDefaults();
        } else {
            @SuppressWarnings("unchecked")
            List<String> blocks = (List<String>) getVersatileList(Config.broadcastedBlocks);
            //TODO the logging statement
            log.severe(getLoggerPrefix() + " Size of block list = " + blocks.size());
            for (Iterator<String> it = blocks.iterator(); it.hasNext();) {
                String x = it.next();
                String[] bi = x.split(":");
                Material matchAttempt= null;
                try {
                    matchAttempt = Material.matchMaterial(bi[0]);
                } catch (Exception ex) {
                    log.severe(getLoggerPrefix() + " Unable to match material '" + bi[0] + "'");
                }
                if (matchAttempt != null) {
                    if (matchAttempt.isBlock()) {
                        try {
                            String re = bi[1].replace(" ","_").toUpperCase();
                            ChatColor color = ChatColor.valueOf(re);
                            // TODO look at the color section here...
                            //ChatColor color = BlockColor.getBlockColor(matchAttempt);
                            //System.out.println("Adding " + matchAttempt.name() + " " + color.name());
                            if (Node.containsMat(broadcastedBlocks, matchAttempt)) {
                                broadcastedBlocks.add(new Node(matchAttempt,color));
                            }
                        } catch (Exception ex) {
                            log.severe(getLoggerPrefix() + " Unable to match color '" + bi[1] +"'");
                        }

                    } else {
                        log.warning(getLoggerPrefix() + " Unable to add " + x + " because it is not a block!");
                    }
                } else {
                    log.warning(getLoggerPrefix() + " Unable to add " + x + ".  Unrecognized block.  See bukkit material enum for valid block names.");
                }
            }
            writeListToConfig(broadcastedBlocks, Config.broadcastedBlocks);
        }
    }

    public void writeListToConfig(List<Node> list, String configLoc) {
        LinkedList<String> tempList = new LinkedList<String>();
        for (Node x : list) {
            tempList.add(x.getMaterial().name().toLowerCase().replace("_", " ") + ":" + x.getColor().name().toLowerCase().replace("_", " "));
        }
        getConfig().set(configLoc, tempList);
        this.saveConfig();
    }

    public void loadDefaults() {
        broadcastedBlocks.add(new Node(Material.DIAMOND_ORE, ChatColor.AQUA));
        broadcastedBlocks.add(new Node(Material.GOLD_ORE, ChatColor.GOLD));
        broadcastedBlocks.add(new Node(Material.LAPIS_ORE, ChatColor.BLUE));
        broadcastedBlocks.add(new Node(Material.IRON_ORE, ChatColor.GRAY));
        broadcastedBlocks.add(new Node(Material.COAL_ORE, ChatColor.DARK_GRAY));
        broadcastedBlocks.add(new Node(Material.REDSTONE_ORE, ChatColor.DARK_RED));
        broadcastedBlocks.add(new Node(Material.GLOWING_REDSTONE_ORE, ChatColor.DARK_RED));
        writeListToConfig(broadcastedBlocks, Config.broadcastedBlocks);
    }

    //TODO make this work with all 3 lists?
    public void handleAddToList(CommandSender sender, String[] args, List<Node> list, String configString) {
        if (args.length == 2) {
            sender.sendMessage(getPrefix() + ChatColor.RED + " Format is: item:color");
            sender.sendMessage(ChatColor.RED + " Color is an optional argument.");
            sender.sendMessage(ChatColor.RED + " Ex: sugar cane block:dark green");
        } else if (args.length >= 3) {
            StringBuilder sb = new StringBuilder();
            for (int i=2;i<args.length;i++) {
                sb.append(args[i]).append(" ");
            }
            Node block = Node.parseNode(sb.toString().trim());
            if (block != null) {
                if (!Node.containsMat(list, block.getMaterial())) {
                    list.add(block);
                    //TODO just save the list?
                    //addNodeToConfig(block, configString);
                    sender.sendMessage(getPrefix() + ChatColor.AQUA + " Added " + block.getColor()
                            + Format.material(block.getMaterial()));
                    writeListToConfig(list, configString);
                } else {
                    removeMaterialFromList(block.getMaterial(), list, configString);
                    list.add(block);
                    sender.sendMessage(getPrefix() + ChatColor.AQUA + " Updated " + block.getColor()
                            + Format.material(block.getMaterial()));
                    writeListToConfig(list, configString);
                }
            } else {
                sender.sendMessage(getPrefix() + ChatColor.DARK_RED + " Unable to add block.  Please check your format.");
            }
        }
    }

    public void handleRemoveFromList(CommandSender sender, String[] args, List<Node> list, String configString) {
        if (args.length == 2) {
            sender.sendMessage(getPrefix() + ChatColor.RED + " Simply type the name of the block you want to remove");
            sender.sendMessage(getPrefix() + ChatColor.RED + " It unfortunely must match bukkit's material enum");
            sender.sendMessage(getPrefix() + ChatColor.RED + " If this is really buggy, please ask SeeD419 for help!");
        } else if (args.length > 2) {
            StringBuilder sb = new StringBuilder();
            log.severe(getLoggerPrefix() + " Size of block list = " + list.size());
            for (int i = 2; i < args.length; i++) {
                sb.append(args[i] + " ");
            }
            Material matToRemove = Material.matchMaterial(sb.toString().trim());
            if (matToRemove == null) {
                sender.sendMessage(getPrefix() + ChatColor.DARK_RED + " Unrecognized material");
            } else {
                ChatColor color = getNodeColor(matToRemove,  list);
                if (removeMaterialFromList(matToRemove, list, configString)) {
                    sender.sendMessage(getPrefix() + ChatColor.DARK_RED + " Removed " + color + Format.material(matToRemove));
                    log.severe(getLoggerPrefix() + " Size of block list = " + list.size());
                } else {
                    sender.sendMessage(getPrefix() + " "  + ChatColor.WHITE + Format.material(matToRemove) + ChatColor.DARK_RED + " isn't listed.");
                }
            }
        }
    }

    public void handleListingList(CommandSender sender, List<Node> list) {
        for (Node x : list) {
            sender.sendMessage(x.getColor() + Format.capitalize(Format.material(x.getMaterial())));
        }
    }

    public ChatColor getNodeColor(Material mat, List<Node> list) {
        for (Node x : list) {
            if (x.getMaterial() == mat) {
                return x.getColor();
            }
        }
        return null;
    }

    public boolean removeMaterialFromList(Material mat, List<Node> list, String configString) {
        for (Node x : list) {
             if (x.getMaterial() == mat) {
                 list.remove(x);
                 writeListToConfig(list, configString);
                 return true;
             }
        }
        return false;
    }




    /*
     * Menu handlers
     */
    public void handleBcMenu(CommandSender sender, String[] args) {
        if (args.length == 1) {
            Menu.showBcMenu(sender);
        } else if (args.length > 1) {
            if (args[1].equalsIgnoreCase("list")) {
                sender.sendMessage(getPrefix() + ChatColor.AQUA + " [Broadcasted Blocks]");
                handleListingList(sender, broadcastedBlocks);
            } else if (args[1].equalsIgnoreCase("add")) {
                if (args.length == 2) {
                    sender.sendMessage(getPrefix() + ChatColor.RED + " Format: /fd add bc item:color ex: coal ore:dark gray");
                    sender.sendMessage(getPrefix() + ChatColor.RED + " Color is an optional argument.  If color is left out");
                    sender.sendMessage(getPrefix() + ChatColor.RED + " FD will attempt to pick a color for you. ex: obsidian");
                }
                handleAddToList(sender, args, broadcastedBlocks, Config.broadcastedBlocks);
            } else if (args[1].equalsIgnoreCase("remove")) {
                handleRemoveFromList(sender, args, broadcastedBlocks, Config.broadcastedBlocks);
            } else {
                sender.sendMessage(getPrefix() + ChatColor.DARK_RED + " Unrecognized command " + ChatColor.WHITE + "'" + args[1] + "'");
            }
        }
    }

    public void handleAdminMenu(CommandSender sender, String[] args) {
        if (args.length == 1) {
            Menu.showAdminMenu(sender);
        } else if (args.length > 1) {
            if (args[1].equalsIgnoreCase("list")) {
                sender.sendMessage(getPrefix() + ChatColor.AQUA + " [Admin Message Blocks]");
                handleListingList(sender, adminMessageBlocks);
            } else if (args[1].equalsIgnoreCase("add")) {
                handleAddToList(sender, args, adminMessageBlocks, Config.adminMessageBlocks);
            } else if (args[1].equalsIgnoreCase("remove")) {
                handleRemoveFromList(sender, args, adminMessageBlocks, Config.adminMessageBlocks);
            } else {
                sender.sendMessage(getPrefix() + ChatColor.DARK_RED + " Unrecognized command " + ChatColor.WHITE + "'" + args[1] + "'");
            }
        }
    }

    public void handleLightMenu(CommandSender sender, String[] args) {
        if (args.length == 1) {
            Menu.showLightMenu(sender);
        } else if (args.length > 1) {
            if (args[1].equalsIgnoreCase("list")) {
                sender.sendMessage(getPrefix() + ChatColor.AQUA + " [Light-Monitored Blocks]");
                handleListingList(sender, lightLevelBlocks);
            } else if (args[1].equalsIgnoreCase("add")) {
                handleAddToList(sender, args, lightLevelBlocks, Config.lightLevelBlocks);
            } else if (args[1].equalsIgnoreCase("remove")) {
                handleRemoveFromList(sender, args, lightLevelBlocks, Config.lightLevelBlocks);
            } else {
                sender.sendMessage(getPrefix() + ChatColor.DARK_RED + " Unrecognized command " + ChatColor.WHITE + "'" + args[1] + "'");
            }
        }
    }




    /*
     * Metrics
     */
    private void startMetrics() {
        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            //couldn't start metrics :(
        }
    }
}
