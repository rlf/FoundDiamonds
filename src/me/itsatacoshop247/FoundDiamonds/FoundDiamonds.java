package me.itsatacoshop247.FoundDiamonds;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FoundDiamonds extends JavaPlugin {
	static String mainDir = "plugins/FoundDiamonds/";
	static File logs = new File(mainDir + "logs.txt");
        static File traps = new File(mainDir + "traplocations.txt");
        static File traptemp = new File(mainDir + "traplocationstemp.txt");
        static File worlds = new File(mainDir + "worlds.txt");
        static List<World> worldList;
        static LinkedList<String> enabledWorlds = new LinkedList<String>();
        public static final Logger log = Logger.getLogger("FoundDiamonds");
	private final FoundDiamondsBlockListener blocklistener = new FoundDiamondsBlockListener(this);
	public String pName;
        public Material trap;
        public String pFullName;
        PluginDescriptionFile pdf;
        private String admin = "*.*";

    @Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_BREAK, blocklistener,Event.Priority.Normal, this);
		pdf = this.getDescription();
		pName = pdf.getName();
                pFullName = pdf.getFullName();
		new File(mainDir).mkdir();
		if(!logs.exists())
			try{
				logs.createNewFile();
			}catch (IOException e) {
                            log.log(Level.SEVERE, pName + " Unable to create log file!", e);
			}
                if(!traps.exists())
                   	try{
				traps.createNewFile();
			}catch (IOException e) {
                            log.log(Level.SEVERE, pName + " Unable to create traps file!", e);
			} 
                if(!traptemp.exists())
                   	try{
				traptemp.createNewFile();
			}catch (IOException e) {
                            log.log(Level.SEVERE, pName + " Unable to create traptemp file!", e);
			}
                if(worlds.exists()){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(worlds));
                        try {
                            String worldLine;
                            StringBuilder sb = new StringBuilder();
                        
                            while((worldLine = br.readLine()) != null){
                                if(!worldLine.startsWith("#")){
                                    sb.append(worldLine);
                                }
                            }
                            String worldString = sb.toString();
                            String[] listOfWorlds = worldString.split(",");
                            for(String y: listOfWorlds){
                                enabledWorlds.add(y);
                            }
                        
                        }catch (IOException ex) {
                            Logger.getLogger(FoundDiamonds.class.getName()).log(Level.SEVERE, "Unable to parse worlds.txt", ex);
                        }
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(FoundDiamonds.class.getName()).log(Level.SEVERE, "Unable to find worlds.txt", ex);
                    }
                }
                if(!worlds.exists()){
                    try{
                        worlds.createNewFile();
                        worldList = getServer().getWorlds();
//this works
                        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(worlds)));
                        out.println("#List of enabled worlds:");
                        out.println("#Separate them with a comma like so:  world,world_nether,mainworld,poopworld");
                        
                        for(World y : worldList){
                            out.write(y.getName() + ",");
                        }
                        out.flush();
                    }catch(IOException e){
                        log.log(Level.SEVERE, pName + " Unable to create worlds file!", e);
                    }
                }
		FoundDiamondsLoadSettings.loadMain();
                log.info(MessageFormat.format("{0} STARTED", pFullName));
	}

    @Override
	public void onDisable() {
		log.info(MessageFormat.format("{0} Disabled", pFullName));             
                    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){

    if ((sender instanceof Player)) {
      Player player = (Player)sender;

      if ((commandLabel.equalsIgnoreCase("settrap")) && (player.hasPermission("FD.admin")) || (player.hasPermission(admin)) || (FoundDiamondsLoadSettings.opstxt && player.isOp())){
        Location first = player.getLocation();
        
        trap = Material.DIAMOND_ORE;    
        if(args.length > 0){
                String item = args[0];
                Material temp = Material.matchMaterial(item);
            if(temp != null && temp.isBlock()){
                trap = temp;
            }else{
                player.sendMessage(ChatColor.RED + "Unrecognized item");
                return false;
                
            }
       }

        int x = first.getBlockX();
        int y = first.getBlockY();
        int z = first.getBlockZ();
        World world = player.getWorld();
        player.sendMessage(ChatColor.DARK_RED + "FoundDiamonds trap set.");
        int randomnumber = (int)(Math.random() * 100.0D);
        if (randomnumber <= 49) {
          Block block1 = world.getBlockAt(x, y - 1, z);
          Block block2 = world.getBlockAt(x, y - 2, z);
          Block block3 = world.getBlockAt(x + 1, y - 2, z); 

          block1.setType(trap);
          block1.setType(trap);
          block2.setType(trap);
          block3.setType(trap);
          
          try {
            BufferedWriter out = new BufferedWriter(new FileWriter(traps, true));
            out.write(block1.getX() + ";" + block1.getY() + ";" + block1.getZ());
            out.newLine();
            out.write(block2.getX() + ";" + block2.getY() + ";" + block2.getZ());
            out.newLine();
            out.write(block3.getX() + ";" + block3.getY() + ";" + block3.getZ());
            out.newLine();
            out.close();
          } catch (IOException ex) {
              log.log(Level.SEVERE, pName + " Unable to write trap locations to file!", ex);
          }
          return true;
        }
        if ((randomnumber >= 50) && (randomnumber <= 75)) {
          Block block1 = world.getBlockAt(x, y - 1, z);
          Block block2 = world.getBlockAt(x, y - 2, z + 1);
          Block block3 = world.getBlockAt(x - 1, y - 2, z);
          Block block4 = world.getBlockAt(x, y - 2, z);
          
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
          } catch (IOException ex) {
              log.log(Level.SEVERE, pName + " Unable to write trap locations to file!", ex);
          }

          return true;
        }
        if (randomnumber >= 76) {
          Block block1 = world.getBlockAt(x, y - 1, z);
          Block block2 = world.getBlockAt(x - 1, y - 2, z);
          Block block3 = world.getBlockAt(x , y - 2, z);
          Block block4 = world.getBlockAt(x -1, y - 1, z);

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
          } catch (IOException ex) {
              log.log(Level.SEVERE, pName + " Unable to write trap locations to file!", ex);
          }
          return true;
        }
        return false;
    }
  }
  return false;
 }
//    
//    public List<World> getEnabledWorlds(){
//        
//    }
}
