package me.itsatacoshop247.FoundDiamonds;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
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

/* TODO
* AdminMap memory?
* Clean log
* Mcmmo compatibility
* Versatile lists
* Redstone Admin
* Finish set menu, integrate with main menu
* /


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
    private File logs;
    private File traps;
    //private File announced;
    private File configFile;
    private File placed;
    private File adminMapFile;
    private List<Material> enabledBlocks = new LinkedList<Material>();
    private List<Location> trapBlocks = new LinkedList<Location>();
    private List<Location> announcedBlocks = new LinkedList<Location>();
    private List<Location> placedBlocks = new LinkedList<Location>();
    private HashMap<Player, Boolean> adminMessagePlayers;
    private HashMap<Player, Boolean> jumpPotion = new HashMap<Player,Boolean>();
    private static final Logger log = Logger.getLogger("FoundDiamonds");
    private BlockBreakListener breakListener;
    private BlockPlaceListener placeListener;
    private YAMLHandler config;
    private JoinListener join;
    private QuitListener quit;
    private PlayerDamageListener damage;
    private String pluginName;
    private PluginDescriptionFile pdf;
    private boolean printed = false;


    @Override
    public void onEnable() {
        //Manage files - get data
        config = new YAMLHandler(this);

        checkFiles();
        loadEnabledBlocks();

        //Init variables
        adminMessagePlayers = new HashMap<Player, Boolean>();
        join = new JoinListener(this, config);
        quit = new QuitListener(this);
        breakListener = new BlockBreakListener(this, config);
        placeListener = new BlockPlaceListener(this);
        damage = new PlayerDamageListener(this);

        //Register listeners
	PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this.breakListener, this);
        pm.registerEvents(this.join, this);
        pm.registerEvents(this.quit, this);
        pm.registerEvents(this.placeListener, this);
        pm.registerEvents(damage, this);

	pdf = this.getDescription();
	pluginName = pdf.getName();

        log.info(MessageFormat.format("[{0}] Enabled", pluginName));
    }

    @Override
    public void onDisable() {
        log.info(MessageFormat.format("[{0}] Saving blocks to files...", pluginName));
        String info = "This file stores your trap block locations.";
        String info2 = "If you have any issues with traps - feel free to delete this file.";
        boolean temp = writeBlocksToFile(traps, trapBlocks, info, info2);
//        String info3 = "This file stores the blocks that have already been announced.";
//        String info4 = "If you'd like to reannounce all the blocks - feel free to delete this file.";
//        boolean temp2 = writeBlocksToFile(announced, announcedBlocks, info3, info4);
        String info5 = "This file stores blocks that would be announced that players placed";
        String info6 = "If you'd like to announce these placed blocks, feel free to delete this file.";
        boolean temp3 = writeBlocksToFile(placed, placedBlocks, info5, info6);
        if (temp && temp3) {
            log.info(MessageFormat.format("[{0}] Successfully saved all blocks to files.", pluginName));
        } else {
            log.warning(MessageFormat.format("[{0}] Couldn't save blocks to files!", pluginName));
            log.warning(MessageFormat.format("[{0}] You could try deleting .placed and .traplocations", pluginName));
        }
        //saveAdminMap(adminMapFile);
        log.info(MessageFormat.format("[{0}] Disabled", pluginName));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        if (((commandLabel.equalsIgnoreCase("fd")) || commandLabel.equalsIgnoreCase("founddiamonds"))) {
            if (args.length == 0) {
                printMainMenu(sender);
                return true;
            } else {
                String arg = args[0];
                if (arg.equalsIgnoreCase("trap")) {
                    if (sender instanceof Player) {
                        if (hasPerms(player, "fd.trap")) {
                            return handleTrap(player, args);
                        }
                    } else {
                        sender.sendMessage(breakListener.getPrefix() + ChatColor.DARK_RED + "Can't set a trap from the console.");
                        return true;
                    }
                } else if (arg.equalsIgnoreCase("reload")) {
                    if (sender instanceof Player) {
                        if (!hasPerms(player, "fd.reload")) {
                            return false;
                        }
                    }
                    reloadConfig();
                    sender.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "Configuration reloaded.");
                    return true;
                } else if (arg.equalsIgnoreCase("toggle")) {
                    if (sender instanceof Player) {
                        if (!hasPerms(player, "fd.toggle")) {
                            return false;
                        }
                    }
                    if (args.length == 1) {
                            showToggle(sender);
                            return true;
                        } else  if (args.length == 2) {
                            arg = args[1];
                            return handleToggle(sender, arg);
                        } else {
                            sender.sendMessage(breakListener.getPrefix() + ChatColor.RED + "Invalid number of arguments.");
                            sender.sendMessage(ChatColor.RED + "See '/fd toggle' for the list of valid arguments.");
                            return false;
                        }
                    } else if (arg.equalsIgnoreCase("config")) {
                        if (sender instanceof Player) {
                            if (!hasPerms(player, "fd.config")) {
                                return false;
                            }
                        }
                        if (args.length == 2) {
                            if (args[1].equalsIgnoreCase("2")) {
                                showConfig2(sender);
                            } else if (args[1].equalsIgnoreCase("3")) {
                                showConfig3(sender);
                            }
                        } else {
                            showConfig(sender);
                        }
                    } else if (arg.equalsIgnoreCase("admin")) {
                        if (sender instanceof Player) {
                            if (!hasPerms(player, "fd.messages")) {
                                return false;
                            } else {
                                reloadAdminMessageMap(player);
                                if (adminMessagePlayers.get(player)) {
                                    player.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "Admin messages are " + ChatColor.DARK_GREEN + "ON");
                                } else {
                                    player.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "Admin messages are " + ChatColor.RED + "OFF");
                                }
                                return true;
                            }
                        } else {
                            sender.sendMessage(breakListener.getPrefix() + ChatColor.DARK_RED + "Admin messages cannot be toggled from the console.");
                        }
                    } else if (arg.equalsIgnoreCase("set")) {
                        if (sender instanceof Player) {
                            if (!hasPerms(player, "fd.toggle")) {
                                return false;
                            }
                        }
                        if (args.length == 1) {
                            showSetMenu(sender);
                            return true;
                        } else if (args.length == 2) {
                            if (args[1].equalsIgnoreCase("spellpercent")) {
                                System.out.println("spellpercent");
                                sender.sendMessage(breakListener.getPrefix() + ChatColor.DARK_RED + "Usage: /fd set "
                                            + "spellpercent <percent>");
                                return true;
                            }
                        } else if (args.length == 3) {
                            int percent;
                            try {
                                percent = Integer.parseInt(args[2].trim());
                            } catch (Exception ex) {
                                 sender.sendMessage(breakListener.getPrefix() + ChatColor.RED + "Invalid argument");
                                 return true;
                            }
                            if (percent >=0 && percent <=100) {
                                getConfig().set(config.getPercentToGetPotion(), percent);
                                printSaved(sender);
                                return true;
                            } else {
                                sender.sendMessage(breakListener.getPrefix() + ChatColor.DARK_RED + "Percentage "
                                     + "must be between 0 and 100");
                                return true;
                            }
                        }
                    } else if (arg.equalsIgnoreCase("world")) {
                        if (sender instanceof Player) {
                            if (!hasPerms(player, "fd.world")) {
                                return false;
                            }
                        }
                        handleWorldMenu(sender, args);
                        return true;
                    } else {
                        sender.sendMessage(breakListener.getPrefix() + ChatColor.DARK_RED + "Unrecognized command '"
                                + ChatColor.WHITE + args[0] + ChatColor.DARK_RED + "'");
                        return false;
                    }
                }
            }
            return false;
        }




    /*
     * World handlers
     */
    private boolean handleWorldMenu(CommandSender sender, String[] args) {
        if (args.length == 1) {
            showWorldMenu(sender);
            return true;
        } else if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("list")) {
                printEnabledWorlds(sender);
                return true;
            } else if (args[1].equalsIgnoreCase("add")) {
                if (args.length == 3) {
                    String worldName = args[2];
                    validateWorld(sender, worldName);
                    return true;
                } else if (args.length == 4) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(args[2]);
                    sb.append(" ");
                    sb.append(args[3]);
                    String worldName = sb.toString();
                    validateWorld(sender, worldName);
                    return true;
                } else {
                    sender.sendMessage(breakListener.getPrefix() + ChatColor.DARK_RED + "Usage: /fd world add <worldname>");
                    return true;
                }
            } else if (args[1].equalsIgnoreCase("remove")) {
                if (getConfig().getStringList(config.getEnabledWorlds()).contains(args[2])) {
                    List<?> worldList = getConfig().getList(config.getEnabledWorlds());
                    worldList.remove(args[2]);
                    getConfig().set(config.getEnabledWorlds(), worldList);
                    sender.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "World '" + args[2] +"' removed.");
                    saveYaml();
                    return true;
                } else {
                    sender.sendMessage(breakListener.getPrefix() + ChatColor.DARK_RED + "World '" + args[2] +"' isn't an enabled world.");
                    return true;
                }
            } else {
                sender.sendMessage(breakListener.getPrefix() + ChatColor.DARK_RED + "Unrecognized command.  See /fd world");
                return true;
            }
        }
        return false;
    }

    private void validateWorld(CommandSender sender, String worldName) {
        List<World> temp = getServer().getWorlds();
        for (World w : temp) {
            if (w.getName().equalsIgnoreCase(worldName)) {
                @SuppressWarnings("unchecked")
                List<String> worldList = (List<String>) getConfig().getList(config.getEnabledWorlds());
                worldList.add(worldName);
                getConfig().set(config.getEnabledWorlds(), worldList);
                sender.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "World '" + worldName + "' added.");
                saveYaml();
                return;
            }
        }
        sender.sendMessage(breakListener.getPrefix() + ChatColor.DARK_RED + "Couldn't find a world with the name '" + worldName + "'");

    }

    private void printEnabledWorlds(CommandSender sender) {
        sender.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "[Enabled Worlds]");
        for (Iterator<String> it = getConfig().getStringList(config.getEnabledWorlds()).iterator(); it.hasNext();) {
            String x = it.next();
            sender.sendMessage("    - " + x);
        }
    }

    private void showWorldMenu(CommandSender sender) {
        sender.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "[Worlds 1/1]");
        sender.sendMessage("/fd world" + ChatColor.RED + " <option>");
        sender.sendMessage(ChatColor.RED + "    list" + ChatColor.WHITE + " - List FD enabled worlds");
        sender.sendMessage(ChatColor.RED + "    add <world>" + ChatColor.WHITE + " - Add FD to a world.");
        sender.sendMessage(ChatColor.RED + "    remove <world>" + ChatColor.WHITE + " - Remove FD from a world.");
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
     * Main Menu
     */
    private void printMainMenu(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (hasPerms(player, "fd.trap") || hasPerms(player, "fd.messages") || hasPerms(player, "fd.config")
                    || hasPerms(player, "fd.config") || hasPerms(player, "fd.reload") || hasPerms(player, "fd.toggle")
                    || hasPerms(player, "fd.world")) {
                player.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "[FoundDiamonds Main Menu]");
                player.sendMessage("/fd " + ChatColor.RED + "<option>");
            }
            if (hasPerms(player, "FD.messages")) {
                player.sendMessage(ChatColor.RED + "    admin" + ChatColor.WHITE + " - Toggle your admin messages");
            }
            if (hasPerms(player, "FD.config")) {
                player.sendMessage(ChatColor.RED + "    config" + ChatColor.WHITE + " - View the configuration file");
            }
            if (hasPerms(player, "FD.reload")) {
                player.sendMessage(ChatColor.RED + "    reload" + ChatColor.WHITE + " - Reload the configuration file");
            }
            if (hasPerms(player, "FD.toggle")) {
                player.sendMessage(ChatColor.RED + "    toggle <option>" + ChatColor.WHITE + " - Change the configuration");
            }
            if (hasPerms(player, "FD.trap")) {
                player.sendMessage(ChatColor.RED + "    trap" + ChatColor.WHITE + " - Set a diamond ore trap");
            }
            if (hasPerms(player, "FD.trap")) {
                player.sendMessage(ChatColor.RED + "    trap <itemname>" + ChatColor.WHITE + " - Set a trap with another block");
            }
            if (hasPerms(player, "FD.world")) {
                player.sendMessage(ChatColor.RED + "    world" + ChatColor.WHITE + " - Manange FD enabled worlds");
            }
        } else {
                sender.sendMessage("/fd " + ChatColor.RED + "<option>");
                sender.sendMessage(ChatColor.RED + "    config" + ChatColor.WHITE + " - View the configuration file");
                sender.sendMessage(ChatColor.RED + "    reload" + ChatColor.WHITE + " - Reload the configuration file");
                sender.sendMessage(ChatColor.RED + "    toggle <option>" + ChatColor.WHITE + " - Change the configuration");
                sender.sendMessage(ChatColor.RED + "    world" + ChatColor.WHITE + " - Manange FD enabled worlds");
        }
    }




    /*
     * Configuration File
     */
    public void saveYaml() {
        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            log.severe(MessageFormat.format("[{0}] Unable to create save configuration file! {1}", pluginName, ex));
        }
    }

    public void loadYaml() {
        try {
            getConfig().options().copyDefaults(true);
            getConfig().save(configFile);
            getConfig().load(configFile);
        } catch (FileNotFoundException ex) {
            log.severe(MessageFormat.format("[{0}] Unable to load configuration file! {1}", pluginName, ex));
        } catch (IOException ex) {
            log.severe(MessageFormat.format("[{0}] Unable to load configuration file! {1}", pluginName, ex));
        } catch (InvalidConfigurationException ex) {
            log.severe(MessageFormat.format("[{0}] Unable to load configuration file! {1}", pluginName, ex));
        }
    }
    private void printSaved(CommandSender sender) {
        saveYaml();
        sender.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "Configuration updated.");
    }




    /*
     * Trap Blocks
     */
    private boolean handleTrap(Player player, String[] args) {
        Location playerLoc = player.getLocation();
        Material trap;
        if (args.length == 1) {
            trap = Material.DIAMOND_ORE;
        } else if (args.length == 2) {
            String item = args[1];
            Material temp = Material.matchMaterial(item);
            if (temp != null && temp.isBlock()) {
                trap = temp;
            } else {
                player.sendMessage(breakListener.getPrefix() + ChatColor.RED + "Unable to set a trap with '" + item + "'");
                player.sendMessage(ChatColor.RED + "Is it a block and a valid item? Try /fd trap gold_ore");
                return false;
            }
        } else {
            player.sendMessage(breakListener.getPrefix() + ChatColor.RED + "Invalid number of arguments");
            player.sendMessage(ChatColor.RED + "Is it a block and a valid item? Try /fd trap gold_ore");
            return false;
        }
        int x = playerLoc.getBlockX();
        int y = playerLoc.getBlockY();
        int z = playerLoc.getBlockZ();
        World world = player.getWorld();
        player.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "Trap set using " + trap.name().toLowerCase().replace("_", " "));
        int randomnumber = (int)(Math.random() * 100.0D);
        if ((randomnumber >= 0) && randomnumber < 50) {
            Block block1 = world.getBlockAt(x, y - 1, z);
            Block block2 = world.getBlockAt(x, y - 2, z + 1);
            Block block3 = world.getBlockAt(x - 1, y - 2, z);
            Block block4 = world.getBlockAt(x, y - 2, z);
            handleTrapBlocks(trap, block1, block2, block3, block4);
            return true;
        } else if (randomnumber >= 50) {
            Block block1 = world.getBlockAt(x, y - 1, z);
            Block block2 = world.getBlockAt(x - 1, y - 2, z);
            Block block3 = world.getBlockAt(x , y - 2, z);
            Block block4 = world.getBlockAt(x -1, y - 1, z);
            handleTrapBlocks(trap, block1, block2, block3, block4);
            return true;
        } else {
            return false;
        }
    }

    public void handleTrapBlocks(Material trap, Block block1, Block block2, Block block3, Block block4) {
        trapBlocks.add(block1.getLocation());
        trapBlocks.add(block2.getLocation());
        trapBlocks.add(block3.getLocation());
        trapBlocks.add(block4.getLocation());
        block1.setType(trap);
        block2.setType(trap);
        block3.setType(trap);
        block4.setType(trap);
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
                try {
                    file.createNewFile();
                    BufferedWriter out = new BufferedWriter(new FileWriter(file, false));
                    out.write("# " + info);
                    out.newLine();
                    out.write("# " + info2);
                    out.newLine();
                    for (Iterator<Location> it = blockList.iterator(); it.hasNext();) {
                        Location m = it.next();
                        out.write(m.getWorld().getName() + ";" + m.getX() + ";" + m.getY() + ";" + m.getZ());
                        out.newLine();
                    }
                    out.close();
                    return true;
                } catch (IOException ex) {
                    log.severe(MessageFormat.format("[{0}] Error writing blocks to file!", pluginName, file.getName()));
                    return false;
                }
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
        try {
            BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String strLine = b.readLine();
            while (strLine != null) {
                if (!strLine.startsWith("#")) {
                    try {
                        String[] fs = strLine.split(";");
                        Location lo = new Location(getServer().getWorld(fs[0]), Double.parseDouble(fs[1]),
                            Double.parseDouble(fs[2]), Double.parseDouble(fs[3]));
                        list.add(lo);
                    } catch (Exception ex) {
                        log.severe(MessageFormat.format("[{0}] Invalid block in trapblocks.txt!  It''s recommened that you delete it!", pluginName));
                    }
                }
                strLine = b.readLine();
            }
            b.close();
        } catch (Exception ex) {
            log.severe(MessageFormat.format("[{0}] Unable to read trap blocks from file! {1}", pluginName, ex));
        }
    }

    private void checkFiles() {
        if (!mainDir.exists()) {
            mainDir.mkdir();
        }
        logs = new File(getDataFolder(), "logs.txt");
        traps = new File(getDataFolder(), ".traplocations");
        //announced = new File(getDataFolder(), ".announced");
        configFile = new File(getDataFolder(), "config.yml");
        placed = new File(getDataFolder(), ".placed");
        adminMapFile = new File(getDataFolder(), ".adminMap");

        if (!logs.exists()) {
            try {
                logs.createNewFile();
            } catch (IOException ex) {
                log.severe(MessageFormat.format("[{0}] Unable to create log file! {1}", pluginName, ex));
       	    }
         }
         if (traps.exists()) {
             readBlocksFromFile(traps, trapBlocks);
         }
//         if (announced.exists()) {
//             readBlocksFromFile(announced, announcedBlocks);
//         }
         if (placed.exists()) {
             readBlocksFromFile(placed, placedBlocks);
         }
         if(!configFile.exists()) {
             config.load();
         } else {
             loadYaml();
         }
    }

    public File getTrapsFile() {
        return traps;
    }

    public File getLogFile() {
        return logs;
    }




    /*
     * Enabled block handlers
     */
    public void reloadEnabledBlocks(Boolean b, Material m) {
        if (b) {
            enabledBlocks.add(m);
        } else {
            enabledBlocks.remove(m);
        }
    }

    public void reloadEnabledBlocks(Boolean b, Material m, Material o) {
        if (b) {
            enabledBlocks.add(m);
            enabledBlocks.add(o);
        } else {
            enabledBlocks.remove(m);
            enabledBlocks.remove(o);
        }
    }

    private void loadEnabledBlocks() {
        if (getConfig().getBoolean(config.getBcDiamond())) {
            enabledBlocks.add(Material.DIAMOND_ORE);
        }
        if (getConfig().getBoolean(config.getBcGold())) {
            enabledBlocks.add(Material.GOLD_ORE);
        }
        if (getConfig().getBoolean(config.getBcIron())) {
            enabledBlocks.add(Material.IRON_ORE);
        }
        if (getConfig().getBoolean(config.getBcLapis())) {
            enabledBlocks.add(Material.LAPIS_ORE);
        }
        if (getConfig().getBoolean(config.getBcMossy())) {
            enabledBlocks.add(Material.MOSSY_COBBLESTONE);
        }
        if (getConfig().getBoolean(config.getBcRedstone())) {
            enabledBlocks.add(Material.REDSTONE_ORE);
            enabledBlocks.add(Material.GLOWING_REDSTONE_ORE);
        }
        if (getConfig().getBoolean(config.getBcCoal())) {
            enabledBlocks.add(Material.COAL_ORE);
        }
        if (getConfig().getBoolean(config.getBcObby())) {
            enabledBlocks.add(Material.OBSIDIAN);
        }
    }

    public List<Material> getEnabledBlocks() {
        return Collections.unmodifiableList(enabledBlocks);
    }




    /*
     * Toggle Menu and handler
     */
    private void showToggle(CommandSender sender) {
        sender.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "[Toggle Options 1/3]");
        sender.sendMessage(ChatColor.RED + "    diamond" + ChatColor.WHITE + " - Diamond broadcast");
        sender.sendMessage(ChatColor.RED + "    gold" + ChatColor.WHITE + " - Gold broadcast");
        sender.sendMessage(ChatColor.RED + "    lapis" + ChatColor.WHITE + " - Lapis broadcast");
        sender.sendMessage(ChatColor.RED + "    redstone" + ChatColor.WHITE + " - Redstone broadcast");
        sender.sendMessage(ChatColor.RED + "    iron" + ChatColor.WHITE + " - Iron broadcast");
        sender.sendMessage(ChatColor.RED + "    coal" + ChatColor.WHITE + " - Coal broadcast");
        sender.sendMessage(ChatColor.RED + "    mossy" + ChatColor.WHITE + " - Mossy broadcast");
        sender.sendMessage(ChatColor.RED + "    obby" + ChatColor.WHITE + " - Obsidian broadcast");
        sender.sendMessage(ChatColor.RED + "    darkness" + ChatColor.WHITE + " - Disable mining in total darkness");
        sender.sendMessage("Type /fd toggle 2 to read the next page");
    }

    private void showToggle2(CommandSender sender) {
        sender.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "[Toggle Options 2/3]");
        sender.sendMessage(ChatColor.RED + "    ops" + ChatColor.WHITE + " - OPs have all permissions");
        sender.sendMessage(ChatColor.RED + "    kick" + ChatColor.WHITE + " - Kick player on trap break");
        sender.sendMessage(ChatColor.RED + "    ban" + ChatColor.WHITE + " - Ban player on trap break");
        sender.sendMessage(ChatColor.RED + "    trapalerts" + ChatColor.WHITE + " - Send admin alerts on trap breaks");
        sender.sendMessage(ChatColor.RED + "    diamondadmin" + ChatColor.WHITE + " - Send admin messages on diamond breaks");
        sender.sendMessage(ChatColor.RED + "    logging" + ChatColor.WHITE + " - Log all diamond breaks to log.txt");
        sender.sendMessage(ChatColor.RED + "    usenicks" + ChatColor.WHITE + " - Use player nicknames in broadcasts");
        sender.sendMessage(ChatColor.RED + "    creative" + ChatColor.WHITE + " - Disable in creative gamemode");
        sender.sendMessage(ChatColor.RED + "    diamondadmin" + ChatColor.WHITE + " - Show admin messages for diamond");
        sender.sendMessage("Type /fd toggle 3 to read the next page");
    }

    private void showToggle3(CommandSender sender) {
        sender.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "[Toggle Options 2/3]");
        sender.sendMessage(ChatColor.RED + "    goldadmin" + ChatColor.WHITE + " - Show admin messages for gold");
        sender.sendMessage(ChatColor.RED + "    lapisadmin" + ChatColor.WHITE + " - Show admin messages for lapis");
        sender.sendMessage(ChatColor.RED + "    ironadmin" + ChatColor.WHITE + " - Show admin messages for iron");
    }

    private boolean handleToggle(CommandSender sender, String arg) {
        if (arg.equalsIgnoreCase("creative")) {
            getConfig().set(config.getDisableInCreative(), !getConfig().getBoolean(config.getDisableInCreative()));
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("darkness")) {
            getConfig().set(config.getDisableMiningInTotalDarkness(), !getConfig().getBoolean(config.getDisableMiningInTotalDarkness()));
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("ops")) {
            getConfig().set(config.getOpsAsFDAdmin(), !getConfig().getBoolean(config.getOpsAsFDAdmin()));
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("kick")) {
            getConfig().set(config.getKickOnTrapBreak(), !getConfig().getBoolean(config.getKickOnTrapBreak()));
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("ban")) {
            getConfig().set(config.getBanOnTrapBreak(), !getConfig().getBoolean(config.getBanOnTrapBreak()));
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("trapalerts")) {
            getConfig().set(config.getAdminAlertsOnAllTrapBreaks(), !getConfig().getBoolean(config.getAdminAlertsOnAllTrapBreaks()));
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("randomitems")) {
            getConfig().set(config.getAwardsForFindingDiamonds(), !getConfig().getBoolean(config.getAwardsForFindingDiamonds()));
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("diamond")) {
            getConfig().set(config.getBcDiamond(), !getConfig().getBoolean(config.getBcDiamond()));
            reloadEnabledBlocks(getConfig().getBoolean(config.getBcDiamond()), Material.DIAMOND_ORE);
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("gold")) {
            getConfig().set(config.getBcGold(), !getConfig().getBoolean(config.getBcGold()));
            reloadEnabledBlocks(getConfig().getBoolean(config.getBcGold()), Material.GOLD_ORE);
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("lapis")) {
            getConfig().set(config.getBcLapis(), !getConfig().getBoolean(config.getBcLapis()));
            reloadEnabledBlocks(getConfig().getBoolean(config.getBcLapis()), Material.LAPIS_ORE);
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("redstone")) {
            getConfig().set(config.getBcRedstone(), !getConfig().getBoolean(config.getBcRedstone()));
            reloadEnabledBlocks(getConfig().getBoolean(config.getBcRedstone()), Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE);
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("iron")) {
            getConfig().set(config.getBcIron(), !getConfig().getBoolean(config.getBcIron()));
            reloadEnabledBlocks(getConfig().getBoolean(config.getBcIron()), Material.IRON_ORE);
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("mossy") || arg.equalsIgnoreCase("moss")) {
            getConfig().set(config.getBcMossy(), !getConfig().getBoolean(config.getBcMossy()));
            reloadEnabledBlocks(getConfig().getBoolean(config.getBcMossy()), Material.MOSSY_COBBLESTONE);
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("logging")) {
            getConfig().set(config.getLogDiamondBreaks(), !getConfig().getBoolean(config.getLogDiamondBreaks()));
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("coal")) {
            getConfig().set(config.getBcCoal(), !getConfig().getBoolean(config.getBcCoal()));
            reloadEnabledBlocks(getConfig().getBoolean(config.getBcIron()), Material.COAL_ORE);
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("diamondadmin")) {
            getConfig().set(config.getDiamondAdmin(), !getConfig().getBoolean(config.getDiamondAdmin()));
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("goldadmin")) {
            getConfig().set(config.getGoldAdmin(), !getConfig().getBoolean(config.getGoldAdmin()));
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("lapisadmin")) {
            getConfig().set(config.getLapisAdmin(), !getConfig().getBoolean(config.getLapisAdmin()));
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("ironadmin")) {
            getConfig().set(config.getIronAdmin(), !getConfig().getBoolean(config.getIronAdmin()));
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("usenicks") || arg.equalsIgnoreCase("nick") || arg.equalsIgnoreCase("nicks")) {
            getConfig().set(config.getUseNick(), !getConfig().getBoolean(config.getUseNick()));
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("obby") || arg.equalsIgnoreCase("obsidian")) {
            getConfig().set(config.getBcObby(), !getConfig().getBoolean(config.getBcObby()));
            reloadEnabledBlocks(getConfig().getBoolean(config.getBcObby()), Material.OBSIDIAN);
            printSaved(sender);
        } else if (arg.equalsIgnoreCase("2")) {
            showToggle2(sender);
        } else if (arg.equalsIgnoreCase("3")) {
            showToggle3(sender);
        } else {
            sender.sendMessage(breakListener.getPrefix() + ChatColor.RED + "Argument '" + arg + "' unrecognized.");
            sender.sendMessage(ChatColor.RED + "See '/fd toggle' for the list of valid arguments.");
            return false;
        }
        return true;
    }




    /*
     * Config menus
     */
    private void showConfig(CommandSender sender) {
        sender.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "[Configuration 1/3]");
        sender.sendMessage(ChatColor.RED + "    Diamond Ore: " + getConfigBoolean(getConfig().getBoolean(config.getBcDiamond())));
        sender.sendMessage(ChatColor.RED + "    Gold Ore: " + getConfigBoolean(getConfig().getBoolean(config.getBcGold())));
        sender.sendMessage(ChatColor.RED + "    Lapis Ore: " + getConfigBoolean(getConfig().getBoolean(config.getBcLapis())));
        sender.sendMessage(ChatColor.RED + "    Redstone Ore: " + getConfigBoolean(getConfig().getBoolean(config.getBcRedstone())));
        sender.sendMessage(ChatColor.RED + "    Iron Ore: " + getConfigBoolean(getConfig().getBoolean(config.getBcIron())));
        sender.sendMessage(ChatColor.RED + "    Coal Ore: " + getConfigBoolean(getConfig().getBoolean(config.getBcCoal())));
        sender.sendMessage(ChatColor.RED + "    Mossy Cobblestone: " + getConfigBoolean( getConfig().getBoolean(config.getBcMossy())));
        sender.sendMessage(ChatColor.RED + "    Obsidian: " + getConfigBoolean(getConfig().getBoolean(config.getBcObby())));
        sender.sendMessage(ChatColor.RED + "    Disable ore mining in total darkness: " + getConfigBoolean(getConfig().getBoolean(config.getDisableMiningInTotalDarkness())));
        sender.sendMessage("Type /fd config 2 to read the next page");

    }

    private void showConfig2(CommandSender sender) {
        sender.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "[Configuration 2/3]");
        sender.sendMessage(ChatColor.RED + "    Use Player NickNames: " + getConfigBoolean(getConfig().getBoolean(config.getUseNick())));
        sender.sendMessage(ChatColor.RED + "    Random Item 1: " + ChatColor.WHITE + Material.getMaterial(getConfig().getInt(config.getRandomItem1())).toString().toLowerCase().replace("_", " "));
        sender.sendMessage(ChatColor.RED + "    Random Item 2: " + ChatColor.WHITE + Material.getMaterial(getConfig().getInt(config.getRandomItem2())).toString().toLowerCase().replace("_", " "));
        sender.sendMessage(ChatColor.RED + "    Random Item 3: " + ChatColor.WHITE + Material.getMaterial(getConfig().getInt(config.getRandomItem3())).toString().toLowerCase().replace("_", " "));
        sender.sendMessage(ChatColor.RED + "    Give OPs all permissions: " +  getConfigBoolean(getConfig().getBoolean(config.getOpsAsFDAdmin())));
        sender.sendMessage(ChatColor.RED + "    Kick players on trap break: " + getConfigBoolean(getConfig().getBoolean(config.getKickOnTrapBreak())));
        sender.sendMessage(ChatColor.RED + "    Ban players on trap break: " + getConfigBoolean(getConfig().getBoolean(config.getBanOnTrapBreak())));
        sender.sendMessage(ChatColor.RED + "    Admin alerts on all trap breaks: " + getConfigBoolean(getConfig().getBoolean(config.getAdminAlertsOnAllTrapBreaks())));
        sender.sendMessage(ChatColor.RED + "    Random awards for finding diamonds: " + getConfigBoolean(getConfig().getBoolean(config.getAwardsForFindingDiamonds())));
        sender.sendMessage("Type /fd config 3 to read the next page");
    }

    private void showConfig3(CommandSender sender) {
        sender.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "[Configuration 3/3]");
        sender.sendMessage(ChatColor.RED + "    Disable in creative mode: " + getConfigBoolean(getConfig().getBoolean(config.getDisableInCreative())));
        sender.sendMessage(ChatColor.RED + "    Logging all diamond ore breaks: " + getConfigBoolean(getConfig().getBoolean(config.getLogDiamondBreaks())));
        sender.sendMessage(ChatColor.RED + "    Diamond Admin Messages: " + getConfigBoolean(getConfig().getBoolean(config.getDiamondAdmin())));
        sender.sendMessage(ChatColor.RED + "    Gold Admin Messages: " + getConfigBoolean(getConfig().getBoolean(config.getGoldAdmin())));
        sender.sendMessage(ChatColor.RED + "    Lapis Admin Messages: " + getConfigBoolean(getConfig().getBoolean(config.getLapisAdmin())));
        sender.sendMessage(ChatColor.RED + "    Iron Admin Messages: " + getConfigBoolean(getConfig().getBoolean(config.getIronAdmin())));
    }

    private String getConfigBoolean(Boolean b) {
        if(b) {
            return ChatColor.DARK_GREEN + "[On]";
        }
        return ChatColor.DARK_RED + "[Off]";
    }




    /*
     * Set Menu
     */
    private void showSetMenu(CommandSender sender) {
        sender.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "[Set]");
        sender.sendMessage(ChatColor.RED + "    randomitem1 <id number>");
        sender.sendMessage(ChatColor.RED + "    randomitem2 <id number>");
        sender.sendMessage(ChatColor.RED + "    randomitem3 <id number>");
        sender.sendMessage(ChatColor.RED + "    spellpercent <percent>");
        sender.sendMessage(ChatColor.RED + "    This is not implemented yet!");
    }




    /*
     * Placed blocks
     */
    public void addToPlacedBlocks(Location w) {
        placedBlocks.add(w);
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<Location> getPlacedBlocks() {
        return placedBlocks;
    }




    /*
     * Misc
     */
    public String getPluginName() {
        return pluginName;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<Location> getAnnouncedBlocks() {
        return announcedBlocks;
    }

    public boolean hasPerms(Player player, String permission) {
        return (player.hasPermission(permission) || (getConfig().getBoolean(config.getOpsAsFDAdmin()) && player.isOp())
                || player.hasPermission("fd.*"));
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public HashMap<Player, Boolean> getJumpPotion() {
        return jumpPotion;
    }


}
