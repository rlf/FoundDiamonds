package me.itsatacoshop247.FoundDiamonds;

import java.io.*;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FoundDiamonds extends JavaPlugin {
    
    
    private final String mainDir = "plugins/FoundDiamonds/";
    private File logs = new File(mainDir + "logs.txt");
    private File traps = new File(mainDir + "traplocations.txt");
    private File traptemp = new File(mainDir + "traplocationstemp.txt");
    private File worlds = new File(mainDir + "worlds.txt");
    private List<World> worldList;
    private List<Material> enabledBlocks = new LinkedList<Material>();
    private LinkedList<String> enabledWorlds = new LinkedList<String>();
    private static final Logger log = Logger.getLogger("FoundDiamonds");
    private FoundDiamondsBlockListener blockListener;
    private FoundDiamondsSettings settings;
    private String pluginName;
    private Material trap;
    private String pluginFullName;
    private PluginDescriptionFile pdf;
        

    @Override
    public void onEnable() {
        settings = new FoundDiamondsSettings(this);
        blockListener = new FoundDiamondsBlockListener(this, settings);
	PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this.blockListener, this);
	pdf = this.getDescription();
	pluginName = pdf.getName();
        pluginFullName = pdf.getFullName();
	new File(mainDir).mkdir();
        checkFiles();
        handleWorldFile();
	settings.loadMain();
        loadEnabledBlocks();
        log.info(MessageFormat.format("{0} Enabled", pluginFullName));
    }

    @Override
    public void onDisable() {
        log.info(MessageFormat.format("{0} Disabled", pluginFullName));             
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if ((sender instanceof Player)) {
            Player player = (Player)sender;
            if ((commandLabel.equalsIgnoreCase("settrap")) && blockListener.hasPerms(player)) {
                Location first = player.getLocation();
                trap = Material.DIAMOND_ORE;    
                if (args.length > 0) {
                    String item = args[0];
                    Material temp = Material.matchMaterial(item);
                    if (temp != null && temp.isBlock()) {
                        trap = temp;
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + "Unrecognized item");
                        return false;
                    }
                }
                int x = first.getBlockX();
                int y = first.getBlockY();
                int z = first.getBlockZ();
                World world = player.getWorld();
                player.sendMessage(ChatColor.AQUA + "FoundDiamonds trap set.");
                int randomnumber = (int)(Math.random() * 100.0D);
                if ((randomnumber >= 0) && randomnumber < 50) {
                    Block block1 = world.getBlockAt(x, y - 1, z);
                    Block block2 = world.getBlockAt(x, y - 2, z + 1);
                    Block block3 = world.getBlockAt(x - 1, y - 2, z);
                    Block block4 = world.getBlockAt(x, y - 2, z);
                    return handleTrapBlocks(block1, block2, block3, block4);
                } else if (randomnumber >= 50) {
                    Block block1 = world.getBlockAt(x, y - 1, z);
                    Block block2 = world.getBlockAt(x - 1, y - 2, z);
                    Block block3 = world.getBlockAt(x , y - 2, z);
                    Block block4 = world.getBlockAt(x -1, y - 1, z);
                    return handleTrapBlocks(block1, block2, block3, block4);
                }
                return false;
            } else if ((commandLabel.equalsIgnoreCase("fd")) && blockListener.hasPerms(player)) {
                player.sendMessage(ChatColor.AQUA + "FoundDiamonds Configuration");
                player.sendMessage("    This is in development.");
            }
        }
        return false;
    }
    
    public boolean handleTrapBlocks(Block block1, Block block2, Block block3, Block block4) {
        block1.setType(trap);
        block2.setType(trap);
        block3.setType(trap);
        block4.setType(trap);
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(traps, true));
            out.write(block1.getX() + ";" + block1.getY() + ";" + block1.getZ());
            out.newLine();
            out.write(block2.getX() + ";" + block2.getY() + ";" + block2.getZ());
            out.newLine();
            out.write(block3.getX() + ";" + block3.getY() + ";" + block3.getZ());
            out.newLine();
            out.write(block4.getX() + ";" + block4.getY() + ";" + block4.getZ());
            out.newLine();
            out.close();
            return true;
        } catch (IOException ex) {
            log.log(Level.SEVERE, pluginName + " Unable to write trap locations to file!", ex);
        }
        return false;
    }
    
    public void checkFiles() {
        if (!logs.exists()) {
            try {
		logs.createNewFile();
            } catch (IOException e) {
                log.log(Level.SEVERE, pluginName + " Unable to create log file!", e);
       	    }
         }        
         if (!traps.exists()) {
             try {
                traps.createNewFile();
             } catch (IOException e) {
                log.log(Level.SEVERE, pluginName + " Unable to create traps file!", e);
	   } 
         }        
         if (!traptemp.exists()) {
             try {
		traptemp.createNewFile();
	     } catch (IOException e) {
                log.log(Level.SEVERE, pluginName + " Unable to create traptemp file!", e);
             }
         } 
    }
    
    public void handleWorldFile() {
        if(worlds.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(worlds));
                try {
                    String worldLine;
                    StringBuilder sb = new StringBuilder();
                    while ((worldLine = br.readLine()) != null) {
                        if (!worldLine.startsWith("#")) {
                            sb.append(worldLine);
                        }
                    }
                    br.close();
                    String worldString = sb.toString();
                    String[] listOfWorlds = worldString.split(",");
                    enabledWorlds.addAll(Arrays.asList(listOfWorlds));
                } catch (IOException ex) {
                        Logger.getLogger(FoundDiamonds.class.getName()).log(Level.SEVERE, "Unable to parse worlds.txt", ex);
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FoundDiamonds.class.getName()).log(Level.SEVERE, "Unable to find worlds.txt", ex);
            }
        } else {
            try {
                worlds.createNewFile();
                worldList = getServer().getWorlds();
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(worlds)));
                out.println("#List of enabled worlds:");
                out.println("#Separate them with a comma like so:  world,world_nether,mainworld,poopworld");
                for (World y : worldList) {
                    out.write(y.getName() + ",");
                }
                for (World x : worldList) {
                    enabledWorlds.add(x.getName());
                }
                out.flush();
                out.close();
            } catch(IOException e) {
                log.log(Level.SEVERE, pluginName + " Unable to create worlds file!", e);
            }
        }
    }
    
    public LinkedList<String> getEnabledWorlds() {
        return enabledWorlds;
    }

    private void loadEnabledBlocks() {
        if (settings.broadcastDiamond()) {
            enabledBlocks.add(Material.DIAMOND_ORE);
        }
        if (settings.broadcastGold()) {
            enabledBlocks.add(Material.GOLD_ORE);
        }
        if (settings.broadcastIron()) {
            enabledBlocks.add(Material.IRON_ORE);
        }
        if (settings.broadcastLapis()) {
            enabledBlocks.add(Material.LAPIS_ORE);
        }
        if (settings.broadcastMossy()) {
            enabledBlocks.add(Material.MOSSY_COBBLESTONE);
        }
        if (settings.broadcastRedstone()) {
            enabledBlocks.add(Material.REDSTONE_ORE);
            enabledBlocks.add(Material.GLOWING_REDSTONE_ORE);
        }
    }
    
    public List<Material> getEnabledBlocks() {
        return enabledBlocks;
    }
    
    public File getTrapsFile() {
        return traps;
    }
    
    public String getMainDir() {
        return mainDir;
    }
    
    public String getPluginName() {
        return pluginName;
    }
}
