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

public class FoundDiamonds extends JavaPlugin {

    private final File mainDir = new File("plugins/FoundDiamonds/");
    private File logs;
    private File traps;
    private File announced;
    private File configFile;
    private List<Material> enabledBlocks = new LinkedList<Material>();
    private List<String> enabledWorlds = new LinkedList<String>();
    private List<Location> trapBlocks = new LinkedList<Location>();
    private List<Location> announcedBlocks = new LinkedList<Location>();
    private HashMap<Player, Boolean> adminMessagePlayers;
    private static final Logger log = Logger.getLogger("FoundDiamonds");
    private BlockListener blockListener;
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
        blockListener = new BlockListener(this, config);

        //Register listeners
	PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this.blockListener, this);
        pm.registerEvents(this.join, this);
        pm.registerEvents(this.quit, this);

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
        if (temp && temp2) {
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
            if (((commandLabel.equalsIgnoreCase("fd")) || commandLabel.equalsIgnoreCase("founddiamonds")) && hasPerms(player)) {
                if (args.length == 0) {
                    player.sendMessage(blockListener.getPrefix() + ChatColor.AQUA + "[FoundDiamonds Main Menu]");
                    player.sendMessage(ChatColor.AQUA + "    /fd " + ChatColor.WHITE + "admin");
                    player.sendMessage(ChatColor.AQUA + "    /fd " + ChatColor.WHITE + "config");
                    player.sendMessage(ChatColor.AQUA + "    /fd " + ChatColor.WHITE + "reload");
                    player.sendMessage(ChatColor.AQUA + "    /fd " + ChatColor.WHITE + "toggle");
                    player.sendMessage(ChatColor.AQUA + "    /fd " + ChatColor.WHITE + "trap");
                    player.sendMessage(ChatColor.AQUA + "    /fd " + ChatColor.WHITE + "trap <itemname>");
                    return true;
                } else {
                    String arg = args[0];
                    if (arg.equalsIgnoreCase("trap") && hasPerms(player)) {
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
                                player.sendMessage(blockListener.getPrefix() + ChatColor.RED + "Unable to set a trap with '" + item + "'");
                                player.sendMessage(ChatColor.RED + "Is it a block and a valid item? Try /fd trap gold_ore");
                                return false;
                            }
                        } else {
                            player.sendMessage(blockListener.getPrefix() + ChatColor.RED + "Invalid number of arguments");
                            player.sendMessage(ChatColor.RED + "Is it a block and a valid item? Try /fd trap gold_ore");
                            return false;
                        }
                        int x = playerLoc.getBlockX();
                        int y = playerLoc.getBlockY();
                        int z = playerLoc.getBlockZ();
                        World world = player.getWorld();
                        player.sendMessage(blockListener.getPrefix() + ChatColor.AQUA + "Trap set using " + trap.name().toLowerCase().replace("_", " "));
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
                        }
                    } else if (arg.equalsIgnoreCase("reload")) {
                        reloadConfig();
                        player.sendMessage(blockListener.getPrefix() + ChatColor.AQUA + "Configuration reloaded.");
                        return true;
                    } else if (arg.equalsIgnoreCase("toggle")) {
                        if (args.length == 1) {
                            player.sendMessage(blockListener.getPrefix() + ChatColor.AQUA + "[Toggle Configurations]");
                            player.sendMessage(ChatColor.AQUA + "    /fd toggle "+ ChatColor.WHITE + "diamond");
                            player.sendMessage(ChatColor.AQUA + "    /fd toggle "+ ChatColor.WHITE + "gold");
                            player.sendMessage(ChatColor.AQUA + "    /fd toggle "+ ChatColor.WHITE + "lapis");
                            player.sendMessage(ChatColor.AQUA + "    /fd toggle "+ ChatColor.WHITE + "redstone");
                            player.sendMessage(ChatColor.AQUA + "    /fd toggle "+ ChatColor.WHITE + "iron");
                            player.sendMessage(ChatColor.AQUA + "    /fd toggle "+ ChatColor.WHITE + "coal");
                            player.sendMessage(ChatColor.AQUA + "    /fd toggle "+ ChatColor.WHITE + "mossy");
                            player.sendMessage(ChatColor.AQUA + "    /fd toggle " + ChatColor.WHITE + "creative");
                            player.sendMessage(ChatColor.AQUA + "    /fd toggle " + ChatColor.WHITE + "darkness");
                            player.sendMessage(ChatColor.AQUA + "    /fd toggle " + ChatColor.WHITE + "ops");
                            player.sendMessage(ChatColor.AQUA + "    /fd toggle " + ChatColor.WHITE + "kick");
                            player.sendMessage(ChatColor.AQUA + "    /fd toggle "+ ChatColor.WHITE + "ban");
                            player.sendMessage(ChatColor.AQUA + "    /fd toggle "+ ChatColor.WHITE + "trapalerts");
                            player.sendMessage(ChatColor.AQUA + "    /fd toggle "+ ChatColor.WHITE + "randomitems");
                            player.sendMessage(ChatColor.AQUA + "    /fd toggle "+ ChatColor.WHITE + "logging");
//                            player.sendMessage("    /fd set randomitem1 <id number>");
//                            player.sendMessage("    /fd set randomitem2 <id number>");
//                            player.sendMessage("    /fd set randomitem3 <id number>");
                            return true;
                        } else  if (args.length == 2) {
                            arg = args[1];
                            if (arg.equalsIgnoreCase("creative")) {
                                getConfig().set(config.getDisableInCreative(), !getConfig().getBoolean(config.getDisableInCreative()));
                            } else if (arg.equalsIgnoreCase("darkness")) {
                                getConfig().set(config.getDisableMiningInTotalDarkness(), !getConfig().getBoolean(config.getDisableMiningInTotalDarkness()));
                            } else if (arg.equalsIgnoreCase("ops")) {
                                getConfig().set(config.getOpsAsFDAdmin(), !getConfig().getBoolean(config.getOpsAsFDAdmin()));
                            } else if (arg.equalsIgnoreCase("kick")) {
                                getConfig().set(config.getKickOnTrapBreak(), !getConfig().getBoolean(config.getKickOnTrapBreak()));
                            } else if (arg.equalsIgnoreCase("ban")) {
                                getConfig().set(config.getBanOnTrapBreak(), !getConfig().getBoolean(config.getBanOnTrapBreak()));
                            } else if (arg.equalsIgnoreCase("trapalerts")) {
                                getConfig().set(config.getAdminAlertsOnAllTrapBreaks(), !getConfig().getBoolean(config.getAdminAlertsOnAllTrapBreaks()));
                            } else if (arg.equalsIgnoreCase("randomitems")) {
                                getConfig().set(config.getAwardsForFindingDiamonds(), !getConfig().getBoolean(config.getAwardsForFindingDiamonds()));
                            } else if (arg.equalsIgnoreCase("diamond")) {
                                getConfig().set(config.getBcDiamond(), !getConfig().getBoolean(config.getBcDiamond()));
                                reloadEnabledBlocks(getConfig().getBoolean(config.getBcDiamond()), Material.DIAMOND_ORE);
                            } else if (arg.equalsIgnoreCase("gold")) {
                                getConfig().set(config.getBcGold(), !getConfig().getBoolean(config.getBcGold()));
                                reloadEnabledBlocks(getConfig().getBoolean(config.getBcGold()), Material.GOLD_ORE);
                            } else if (arg.equalsIgnoreCase("lapis")) {
                                getConfig().set(config.getBcLapis(), !getConfig().getBoolean(config.getBcLapis()));
                                reloadEnabledBlocks(getConfig().getBoolean(config.getBcLapis()), Material.LAPIS_ORE);
                            } else if (arg.equalsIgnoreCase("redstone")) {
                                getConfig().set(config.getBcRedstone(), !getConfig().getBoolean(config.getBcRedstone()));
                                reloadEnabledBlocks(getConfig().getBoolean(config.getBcRedstone()), Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE);
                            } else if (arg.equalsIgnoreCase("iron")) {
                                getConfig().set(config.getBcIron(), !getConfig().getBoolean(config.getBcIron()));
                                reloadEnabledBlocks(getConfig().getBoolean(config.getBcIron()), Material.IRON_ORE);
                            } else if (arg.equalsIgnoreCase("mossy")) {
                                getConfig().set(config.getBcMossy(), !getConfig().getBoolean(config.getBcMossy()));
                                reloadEnabledBlocks(getConfig().getBoolean(config.getBcMossy()), Material.MOSSY_COBBLESTONE);
                            } else if (arg.equalsIgnoreCase("logging")) {
                                getConfig().set(config.getLogDiamondBreaks(), !getConfig().getBoolean(config.getLogDiamondBreaks()));
                            } else if (arg.equalsIgnoreCase("coal")) {
                                getConfig().set(config.getBcCoal(), !getConfig().getBoolean(config.getBcCoal()));
                            } else {
                                player.sendMessage(blockListener.getPrefix() + ChatColor.RED + "Argument '" + arg + "' unrecognized.");
                                player.sendMessage(ChatColor.RED + "See '/fd toggle' for the list of valid arguments.");
                                return false;
                            }
                            saveYaml();
                            player.sendMessage(blockListener.getPrefix() + ChatColor.AQUA + "Configuration updated.");
                            showConfig(player);
                            return true;
                        } else {
                            player.sendMessage(blockListener.getPrefix() + ChatColor.RED + "Invalid number of arguments.");
                            player.sendMessage(ChatColor.RED + "See '/fd toggle' for the list of valid arguments.");
                            return false;
                        }
                    } else if (arg.equalsIgnoreCase("config")) {
                        player.sendMessage(blockListener.getPrefix() + ChatColor.AQUA + "[Configuration]");
                        showConfig(player);
                    } else if (arg.equalsIgnoreCase("admin")) {
                        reloadAdminMessageMap(player);
                        if (adminMessagePlayers.get(player)) {
                            player.sendMessage(blockListener.getPrefix() + ChatColor.AQUA + "Admin messages are " + ChatColor.DARK_GREEN + "ON");
                        } else {
                            player.sendMessage(blockListener.getPrefix() + ChatColor.AQUA + "Admin messages are " + ChatColor.RED + "OFF");
                        }
                        return true;
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
        }
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
         if(!configFile.exists()) {
             config.firstLoad();
         } else {
             loadYaml();
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

    public boolean hasPerms(Player player) {
        return (player.hasPermission("FD.admin") || player.hasPermission("*") || (getConfig().getBoolean(config.getOpsAsFDAdmin()) && player.isOp()));
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public HashMap<Player,Boolean> getAdminMessageMap() {
        return adminMessagePlayers;
    }

    private void showConfig(Player player) {
        player.sendMessage(ChatColor.AQUA + "    Diamond Ore: " + getConfigBoolean(getConfig().getBoolean(config.getBcDiamond())));
        player.sendMessage(ChatColor.AQUA + "    Gold Ore: " + getConfigBoolean(getConfig().getBoolean(config.getBcGold())));
        player.sendMessage(ChatColor.AQUA + "    Lapis Ore: " + getConfigBoolean(getConfig().getBoolean(config.getBcLapis())));
        player.sendMessage(ChatColor.AQUA + "    Redstone Ore: " + getConfigBoolean(getConfig().getBoolean(config.getBcRedstone())));
        player.sendMessage(ChatColor.AQUA + "    Iron Ore: " + getConfigBoolean(getConfig().getBoolean(config.getBcIron())));
        player.sendMessage(ChatColor.AQUA + "    Coal Ore: " + getConfigBoolean(getConfig().getBoolean(config.getBcCoal())));
        player.sendMessage(ChatColor.AQUA + "    Mossy Cobblestone: " + getConfigBoolean( getConfig().getBoolean(config.getBcMossy())));
        player.sendMessage(ChatColor.AQUA + "    Disable in creative mode: " + getConfigBoolean(getConfig().getBoolean(config.getDisableInCreative())));
        player.sendMessage(ChatColor.AQUA + "    Disable ore mining in total darkness: " + getConfigBoolean(getConfig().getBoolean(config.getDisableMiningInTotalDarkness())));
        player.sendMessage(ChatColor.AQUA + "    Treat OPS as FD Admin: " +  getConfigBoolean(getConfig().getBoolean(config.getOpsAsFDAdmin())));
        player.sendMessage(ChatColor.AQUA + "    Kick players on trap break: " + getConfigBoolean(getConfig().getBoolean(config.getKickOnTrapBreak())));
        player.sendMessage(ChatColor.AQUA + "    Ban players on trap break: " + getConfigBoolean(getConfig().getBoolean(config.getBanOnTrapBreak())));
        player.sendMessage(ChatColor.AQUA + "    Admin alerts on all trap breaks: " + getConfigBoolean(getConfig().getBoolean(config.getAdminAlertsOnAllTrapBreaks())));
        player.sendMessage(ChatColor.AQUA + "    Random awards for finding diamonds: " + getConfigBoolean(getConfig().getBoolean(config.getAwardsForFindingDiamonds())));
        player.sendMessage(ChatColor.AQUA + "    Logging all diamond ore breaks: " + getConfigBoolean(getConfig().getBoolean(config.getLogDiamondBreaks())));
        player.sendMessage(ChatColor.AQUA + "    Random Item 1: " + ChatColor.WHITE + Material.getMaterial(getConfig().getInt(config.getRandomItem1())).toString().toLowerCase().replace("_", " "));
        player.sendMessage(ChatColor.AQUA + "    Random Item 2: " + ChatColor.WHITE + Material.getMaterial(getConfig().getInt(config.getRandomItem2())).toString().toLowerCase().replace("_", " "));
        player.sendMessage(ChatColor.AQUA + "    Random Item 3: " + ChatColor.WHITE + Material.getMaterial(getConfig().getInt(config.getRandomItem3())).toString().toLowerCase().replace("_", " "));
    }

    private String getConfigBoolean(Boolean b) {
        if(b) {
            return ChatColor.DARK_GREEN + "[On]";
        }
        return ChatColor.DARK_RED + "[Off]";
    }

}
