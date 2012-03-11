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

//TODO Worlds in menu.
public class FoundDiamonds extends JavaPlugin {

    private final File mainDir = new File("plugins/FoundDiamonds/");
    private File logs;
    private File traps;
    private File announced;
    private File configFile;
    private File placed;
    private List<Material> enabledBlocks = new LinkedList<Material>();
    private List<String> enabledWorlds = new LinkedList<String>();
    private List<Location> trapBlocks = new LinkedList<Location>();
    private List<Location> announcedBlocks = new LinkedList<Location>();
    private List<Location> placedBlocks = new LinkedList<Location>();
    private HashMap<Player, Boolean> adminMessagePlayers;
    private static final Logger log = Logger.getLogger("FoundDiamonds");
    private BlockBreakListener breakListener;
    private BlockPlaceListener placeListener;
    private YAMLHandler config;
    private JoinListener join;
    private QuitListener quit;
    private String pluginName;
    private PluginDescriptionFile pdf;


    @Override
    public void onEnable() {
        //Manage files - get data
        config = new YAMLHandler(this);
        checkFiles();
        loadWorlds();
        loadEnabledBlocks();

        //Init variables
        adminMessagePlayers = new HashMap<Player, Boolean>();
        join = new JoinListener(this, config);
        quit = new QuitListener(this);
        breakListener = new BlockBreakListener(this, config);
        placeListener = new BlockPlaceListener(this);

        //Register listeners
	PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this.breakListener, this);
        pm.registerEvents(this.join, this);
        pm.registerEvents(this.quit, this);
        pm.registerEvents(this.placeListener, this);

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
        String info3 = "This file stores the blocks that have already been announced.";
        String info4 = "If you'd like to reannounce all the blocks - feel free to delete this file.";
        boolean temp2 = writeBlocksToFile(announced, announcedBlocks, info3, info4);
        String info5 = "This file stores blocks that would be announced that players placed";
        String info6 = "If you'd like to announce these placed blocks, feel free to delete this file.";
        boolean temp3 = writeBlocksToFile(placed, placedBlocks, info5, info6);
        if (temp && temp2 && temp3) {
            log.info(MessageFormat.format("[{0}] Successfully saved all blocks to files.", pluginName));
        } else {
            log.warning(MessageFormat.format("[{0}] Couldn't save blocks to files!", pluginName));
            log.warning(MessageFormat.format("[{0}] You could try deleting .announced and .traplocations", pluginName));
        }
        log.info(MessageFormat.format("[{0}] Disabled", pluginName));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if ((sender instanceof Player)) {
            Player player = (Player)sender;
            if (((commandLabel.equalsIgnoreCase("fd")) || commandLabel.equalsIgnoreCase("founddiamonds"))) {
                if (args.length == 0) {
                    printMainMenu(player);
                    return true;
                } else {
                    String arg = args[0];
                    if (arg.equalsIgnoreCase("trap") && hasPerms(player, "fd.trap")) {
                        handleTrap(player, args);
                    } else if (arg.equalsIgnoreCase("reload") && hasPerms(player, "fd.reload")) {
                        reloadConfig();
                        player.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "Configuration reloaded.");
                        return true;
                    } else if (arg.equalsIgnoreCase("toggle") && hasPerms(player, "fd.toggle")) {
                        if (args.length == 1) {
                            showToggle(player);
                            return true;
                        } else  if (args.length == 2) {
                            arg = args[1];
                            if (arg.equalsIgnoreCase("creative")) {
                                getConfig().set(config.getDisableInCreative(), !getConfig().getBoolean(config.getDisableInCreative()));
                                printSaved(player);
                            } else if (arg.equalsIgnoreCase("darkness")) {
                                getConfig().set(config.getDisableMiningInTotalDarkness(), !getConfig().getBoolean(config.getDisableMiningInTotalDarkness()));
                                printSaved(player);
                            } else if (arg.equalsIgnoreCase("ops")) {
                                getConfig().set(config.getOpsAsFDAdmin(), !getConfig().getBoolean(config.getOpsAsFDAdmin()));
                                printSaved(player);
                            } else if (arg.equalsIgnoreCase("kick")) {
                                getConfig().set(config.getKickOnTrapBreak(), !getConfig().getBoolean(config.getKickOnTrapBreak()));
                                printSaved(player);
                            } else if (arg.equalsIgnoreCase("ban")) {
                                getConfig().set(config.getBanOnTrapBreak(), !getConfig().getBoolean(config.getBanOnTrapBreak()));
                                printSaved(player);
                            } else if (arg.equalsIgnoreCase("trapalerts")) {
                                getConfig().set(config.getAdminAlertsOnAllTrapBreaks(), !getConfig().getBoolean(config.getAdminAlertsOnAllTrapBreaks()));
                                printSaved(player);
                            } else if (arg.equalsIgnoreCase("randomitems")) {
                                getConfig().set(config.getAwardsForFindingDiamonds(), !getConfig().getBoolean(config.getAwardsForFindingDiamonds()));
                                printSaved(player);
                            } else if (arg.equalsIgnoreCase("diamond")) {
                                getConfig().set(config.getBcDiamond(), !getConfig().getBoolean(config.getBcDiamond()));
                                reloadEnabledBlocks(getConfig().getBoolean(config.getBcDiamond()), Material.DIAMOND_ORE);
                                printSaved(player);
                            } else if (arg.equalsIgnoreCase("gold")) {
                                getConfig().set(config.getBcGold(), !getConfig().getBoolean(config.getBcGold()));
                                reloadEnabledBlocks(getConfig().getBoolean(config.getBcGold()), Material.GOLD_ORE);
                                printSaved(player);
                            } else if (arg.equalsIgnoreCase("lapis")) {
                                getConfig().set(config.getBcLapis(), !getConfig().getBoolean(config.getBcLapis()));
                                reloadEnabledBlocks(getConfig().getBoolean(config.getBcLapis()), Material.LAPIS_ORE);
                                printSaved(player);
                            } else if (arg.equalsIgnoreCase("redstone")) {
                                getConfig().set(config.getBcRedstone(), !getConfig().getBoolean(config.getBcRedstone()));
                                reloadEnabledBlocks(getConfig().getBoolean(config.getBcRedstone()), Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE);
                                printSaved(player);
                            } else if (arg.equalsIgnoreCase("iron")) {
                                getConfig().set(config.getBcIron(), !getConfig().getBoolean(config.getBcIron()));
                                reloadEnabledBlocks(getConfig().getBoolean(config.getBcIron()), Material.IRON_ORE);
                                printSaved(player);
                            } else if (arg.equalsIgnoreCase("mossy")) {
                                getConfig().set(config.getBcMossy(), !getConfig().getBoolean(config.getBcMossy()));
                                reloadEnabledBlocks(getConfig().getBoolean(config.getBcMossy()), Material.MOSSY_COBBLESTONE);
                                printSaved(player);
                            } else if (arg.equalsIgnoreCase("logging")) {
                                getConfig().set(config.getLogDiamondBreaks(), !getConfig().getBoolean(config.getLogDiamondBreaks()));
                                printSaved(player);
                            } else if (arg.equalsIgnoreCase("coal")) {
                                getConfig().set(config.getBcCoal(), !getConfig().getBoolean(config.getBcCoal()));
                                reloadEnabledBlocks(getConfig().getBoolean(config.getBcIron()), Material.COAL_ORE);
                                printSaved(player);
                            } else if (arg.equalsIgnoreCase("diamondadmin")) {
                                getConfig().set(config.getDiamondAdmin(), !getConfig().getBoolean(config.getDiamondAdmin()));
                                printSaved(player);
                            } else if (arg.equalsIgnoreCase("usenicks")) {
                                getConfig().set(config.getUseNick(), !getConfig().getBoolean(config.getUseNick()));
                                printSaved(player);
                            } else if (arg.equalsIgnoreCase("obby") || arg.equalsIgnoreCase("obsidian")) {
                                getConfig().set(config.getBcObby(), !getConfig().getBoolean(config.getBcObby()));
                                reloadEnabledBlocks(getConfig().getBoolean(config.getBcObby()), Material.OBSIDIAN);
                                printSaved(player);
                            } else if (arg.equalsIgnoreCase("2")) {
                                showToggle2(player);
                            } else {
                                player.sendMessage(breakListener.getPrefix() + ChatColor.RED + "Argument '" + arg + "' unrecognized.");
                                player.sendMessage(ChatColor.RED + "See '/fd toggle' for the list of valid arguments.");
                                return false;
                            }
                            return true;
                        } else {
                            player.sendMessage(breakListener.getPrefix() + ChatColor.RED + "Invalid number of arguments.");
                            player.sendMessage(ChatColor.RED + "See '/fd toggle' for the list of valid arguments.");
                            return false;
                        }
                    } else if (arg.equalsIgnoreCase("config") && hasPerms(player, "fd.config")) {
                        if (args.length == 2) {
                            if (args[1].equalsIgnoreCase("2")) {
                                showConfig2(player);
                            } else if (args[1].equalsIgnoreCase("3")) {
                                showConfig3(player);
                            }
                        } else {
                            showConfig(player);
                        }
                    } else if (arg.equalsIgnoreCase("admin") && hasPerms(player, "fd.messages")) {
                        reloadAdminMessageMap(player);
                        if (adminMessagePlayers.get(player)) {
                            player.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "Admin messages are " + ChatColor.DARK_GREEN + "ON");
                        } else {
                            player.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "Admin messages are " + ChatColor.RED + "OFF");
                        }
                        return true;
                    } else if (arg.equalsIgnoreCase("set") && hasPerms(player, "fd.toggle")) {
                        if (args.length == 1) {
                            player.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "[Set]");
                            player.sendMessage(ChatColor.AQUA + "    /fd set randomitem1 <id number>");
                            player.sendMessage(ChatColor.AQUA + "    /fd set randomitem2 <id number>");
                            player.sendMessage(ChatColor.AQUA + "    /fd set randomitem3 <id number>");
                        }
                    } else {
                        return false;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private void reloadAdminMessageMap(Player player) {
        if ((adminMessagePlayers.containsKey(player)) && adminMessagePlayers.get(player)) {
            adminMessagePlayers.put(player, false);
        } else if ((adminMessagePlayers.containsKey(player)) && (!adminMessagePlayers.get(player))) {
            adminMessagePlayers.put(player, true);
        } else {
            adminMessagePlayers.put(player, true);
        }
    }

    private void printSaved(Player player) {
        saveYaml();
        player.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "Configuration updated.");
    }

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
                log.warning(MessageFormat.format("[{0}] Plugin folder not found.  Did you delete it?", pluginName));
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
        announced = new File(getDataFolder(), ".announced");
        configFile = new File(getDataFolder(), "config.yml");
        placed = new File(getDataFolder(), ".placed");

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
         if (announced.exists()) {
             readBlocksFromFile(announced, announcedBlocks);
         }
         if (placed.exists()) {
             readBlocksFromFile(placed, placedBlocks);
         }
         if(!configFile.exists()) {
             config.firstLoad();
         } else {
             loadYaml();
         }
    }

    //TODO Unused yet
    public void addWorld(String worldName) {
        List<World> tempWorldList = getServer().getWorlds();
        for (World w : tempWorldList) {
            if (w.getName().equalsIgnoreCase(worldName)) {
                config.getEnabledWorlds();
            }
        }
    }

    public void loadWorlds() {
        @SuppressWarnings("unchecked")
        List<String> temp = (List<String>) getConfig().getList(config.getEnabledWorlds());
        for (String x : temp) {
            enabledWorlds.add(x);
        }
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<String> getEnabledWorlds() {
        return enabledWorlds;
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

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<Location> getTrapBlocks() {
        return trapBlocks;
    }

    public List<Material> getEnabledBlocks() {
        return Collections.unmodifiableList(enabledBlocks);
    }

    public File getTrapsFile() {
        return traps;
    }

    public void saveYaml() {
        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            log.severe(MessageFormat.format("[{0}] Unable to create save configuration file! {1}", pluginName, ex));
        }
    }

    public void loadYaml() {
        try {
            getConfig().load(configFile);
        } catch (FileNotFoundException ex) {
            log.severe(MessageFormat.format("[{0}] Unable to load configuration file! {1}", pluginName, ex));
        } catch (IOException ex) {
            log.severe(MessageFormat.format("[{0}] Unable to load configuration file! {1}", pluginName, ex));
        } catch (InvalidConfigurationException ex) {
            log.severe(MessageFormat.format("[{0}] Unable to load configuration file! {1}", pluginName, ex));
        }
    }

    public String getPluginName() {
        return pluginName;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<Location> getAnnouncedBlocks() {
        return announcedBlocks;
    }

    public File getLogFile() {
        return logs;
    }

    public boolean hasPerms(Player player, String permission) {
        return (player.hasPermission(permission) || (getConfig().getBoolean(config.getOpsAsFDAdmin()) && player.isOp())
                || player.hasPermission("fd.*"));
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public HashMap<Player,Boolean> getAdminMessageMap() {
        return adminMessagePlayers;
    }

    private void showToggle(Player player) {
        player.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "[Toggle Options 1/2]");
        player.sendMessage(ChatColor.RED + "    diamond" + ChatColor.WHITE + " - Diamond broadcast");
        player.sendMessage(ChatColor.RED + "    gold" + ChatColor.WHITE + " - Gold broadcast");
        player.sendMessage(ChatColor.RED + "    lapis" + ChatColor.WHITE + " - Lapis broadcast");
        player.sendMessage(ChatColor.RED + "    redstone" + ChatColor.WHITE + " - Redstone broadcast");
        player.sendMessage(ChatColor.RED + "    iron" + ChatColor.WHITE + " - Iron broadcast");
        player.sendMessage(ChatColor.RED + "    coal" + ChatColor.WHITE + " - Coal broadcast");
        player.sendMessage(ChatColor.RED + "    mossy" + ChatColor.WHITE + " - Mossy broadcast");
        player.sendMessage(ChatColor.RED + "    obby" + ChatColor.WHITE + " - Obsidian broadcast");
        player.sendMessage(ChatColor.RED + "    darkness" + ChatColor.WHITE + " - Disable mining in total darkness");
        player.sendMessage("Type /fd toggle 2 to read the next page");
    }

    private void showToggle2(Player player) {
        player.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "[Toggle Options 2/2]");
        player.sendMessage(ChatColor.RED + "    ops" + ChatColor.WHITE + " - OPs have all permissions");
        player.sendMessage(ChatColor.RED + "    kick" + ChatColor.WHITE + " - Kick player on trap break");
        player.sendMessage(ChatColor.RED + "    ban" + ChatColor.WHITE + " - Ban player on trap break");
        player.sendMessage(ChatColor.RED + "    trapalerts" + ChatColor.WHITE + " - Send admin alerts on trap breaks");
        player.sendMessage(ChatColor.RED + "    diamondadmin" + ChatColor.WHITE + " - Send admin messages on diamond breaks");
        player.sendMessage(ChatColor.RED + "    logging" + ChatColor.WHITE + " - Log all diamond breaks to log.txt");
        player.sendMessage(ChatColor.RED + "    usenicks" + ChatColor.WHITE + " - Use player nicknames in broadcasts");
        player.sendMessage(ChatColor.RED + "    creative" + ChatColor.WHITE + " - Disable in creative gamemode");
        //one more
    }

    private void showConfig(Player player) {
        player.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "[Configuration 1/3]");
        player.sendMessage(ChatColor.RED + "    Diamond Ore: " + getConfigBoolean(getConfig().getBoolean(config.getBcDiamond())));
        player.sendMessage(ChatColor.RED + "    Gold Ore: " + getConfigBoolean(getConfig().getBoolean(config.getBcGold())));
        player.sendMessage(ChatColor.RED + "    Lapis Ore: " + getConfigBoolean(getConfig().getBoolean(config.getBcLapis())));
        player.sendMessage(ChatColor.RED + "    Redstone Ore: " + getConfigBoolean(getConfig().getBoolean(config.getBcRedstone())));
        player.sendMessage(ChatColor.RED + "    Iron Ore: " + getConfigBoolean(getConfig().getBoolean(config.getBcIron())));
        player.sendMessage(ChatColor.RED + "    Coal Ore: " + getConfigBoolean(getConfig().getBoolean(config.getBcCoal())));
        player.sendMessage(ChatColor.RED + "    Mossy Cobblestone: " + getConfigBoolean( getConfig().getBoolean(config.getBcMossy())));
        player.sendMessage(ChatColor.RED + "    Obsidian: " + getConfigBoolean(getConfig().getBoolean(config.getBcObby())));
        player.sendMessage(ChatColor.RED + "    Disable ore mining in total darkness: " + getConfigBoolean(getConfig().getBoolean(config.getDisableMiningInTotalDarkness())));
        player.sendMessage("Type /fd config 2 to read the next page");

    }

    private void showConfig2(Player player) {
        player.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "[Configuration 2/3]");
        player.sendMessage(ChatColor.RED + "    Use Player NickNames: " + getConfigBoolean(getConfig().getBoolean(config.getUseNick())));
        player.sendMessage(ChatColor.RED + "    Random Item 1: " + ChatColor.WHITE + Material.getMaterial(getConfig().getInt(config.getRandomItem1())).toString().toLowerCase().replace("_", " "));
        player.sendMessage(ChatColor.RED + "    Random Item 2: " + ChatColor.WHITE + Material.getMaterial(getConfig().getInt(config.getRandomItem2())).toString().toLowerCase().replace("_", " "));
        player.sendMessage(ChatColor.RED + "    Random Item 3: " + ChatColor.WHITE + Material.getMaterial(getConfig().getInt(config.getRandomItem3())).toString().toLowerCase().replace("_", " "));
        player.sendMessage(ChatColor.RED + "    Give OPs all permissions: " +  getConfigBoolean(getConfig().getBoolean(config.getOpsAsFDAdmin())));
        player.sendMessage(ChatColor.RED + "    Kick players on trap break: " + getConfigBoolean(getConfig().getBoolean(config.getKickOnTrapBreak())));
        player.sendMessage(ChatColor.RED + "    Ban players on trap break: " + getConfigBoolean(getConfig().getBoolean(config.getBanOnTrapBreak())));
        player.sendMessage(ChatColor.RED + "    Admin alerts on all trap breaks: " + getConfigBoolean(getConfig().getBoolean(config.getAdminAlertsOnAllTrapBreaks())));
        player.sendMessage(ChatColor.RED + "    Random awards for finding diamonds: " + getConfigBoolean(getConfig().getBoolean(config.getAwardsForFindingDiamonds())));
        player.sendMessage("Type /fd config 3 to read the next page");
    }

    private void showConfig3(Player player) {
        player.sendMessage(breakListener.getPrefix() + ChatColor.AQUA + "[Configuration 3/3]");
        player.sendMessage(ChatColor.RED + "    Disable in creative mode: " + getConfigBoolean(getConfig().getBoolean(config.getDisableInCreative())));
        player.sendMessage(ChatColor.RED + "    Logging all diamond ore breaks: " + getConfigBoolean(getConfig().getBoolean(config.getLogDiamondBreaks())));
        player.sendMessage(ChatColor.RED + "    Enabled worlds: ");
        for (String x : getEnabledWorlds()) {
            player.sendMessage("    - " + x);
        }
    }

    private String getConfigBoolean(Boolean b) {
        if(b) {
            return ChatColor.DARK_GREEN + "[On]";
        }
        return ChatColor.DARK_RED + "[Off]";
    }

    private void printMainMenu(Player player) {
        if (hasPerms(player, "FD.trap") || hasPerms(player, "FD.messages") || hasPerms(player, "FD.config")
                || hasPerms(player, "FD.config") || hasPerms(player, "fd.reload") || hasPerms(player, "fd.toggle")) {
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
    }

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

    public void addToPlacedBlocks(Location w) {
        placedBlocks.add(w);
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<Location> getPlacedBlocks() {
        return placedBlocks;
    }

}
