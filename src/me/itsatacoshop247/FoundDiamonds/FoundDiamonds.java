package me.itsatacoshop247.FoundDiamonds;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
	static final String maindirectory = "plugins/FoundDiamonds/";
        static final String disposaldirectory = "plugins/FoundDiamonds/temp";
	static File Blocks = new File(maindirectory + "Blocks.txt");
	static File logs = new File(maindirectory + "logs.txt");
	public static final Logger log = Logger.getLogger("Minecraft");
	private final FoundDiamondsBlockListener blocklistener = new FoundDiamondsBlockListener(this);
	public String pName;

    @Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_BREAK, blocklistener,Event.Priority.Normal, this);
		log.info("[FoundDiamonds] STARTED");
		PluginDescriptionFile pdf = this.getDescription();
		pName = pdf.getName();
                new File(disposaldirectory).mkdir();
		new File(maindirectory).mkdir();
		if(Blocks.exists())
			try {
				Blocks.createNewFile();
			} catch (IOException e) {
                            log.log(Level.SEVERE, "[FoundDiamonds] Unable to create configuration file!", e);
			}
		if(!logs.exists())
			try{
				logs.createNewFile();
			}catch (IOException e) {
                            log.log(Level.SEVERE, "[FoundDiamonds] Unable to create log file!", e);
			}
		FoundDiamondsLoadSettings.loadMain();
	}

    @Override
	public void onDisable() {
		log.info("[FoundDiamonds] Disabled");
               
                    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
  {

    if ((sender instanceof Player)) {
      Player player = (Player)sender;

      if ((commandLabel.equalsIgnoreCase("settrap")) && player.hasPermission("FD.admin")){
        Location first = player.getLocation();
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

          block1.setTypeId(56);
          block2.setTypeId(56);
          block3.setTypeId(56);
          try {
            BufferedWriter out = new BufferedWriter(new FileWriter("plugins/FoundDiamonds/traplocations.txt", true));
            out.write(block1.getX() + ";" + block1.getY() + ";" + block1.getZ());
            out.newLine();
            out.write(block2.getX() + ";" + block2.getY() + ";" + block2.getZ());
            out.newLine();
            out.write(block3.getX() + ";" + block3.getY() + ";" + block3.getZ());
            out.newLine();
            out.close();
          } catch (IOException e) {
              log.log(Level.SEVERE, pName + " Unable to write trap locations to file!", e);
          }
          return true;
        }
        if ((randomnumber >= 50) && (randomnumber <= 75)) {
          Block block1 = world.getBlockAt(x, y - 1, z);
          Block block2 = world.getBlockAt(x, y - 2, z + 1);
          Block block3 = world.getBlockAt(x - 1, y - 2, z);

          block1.setTypeId(56);
          block2.setTypeId(56);
          block3.setTypeId(56);
          try {
            BufferedWriter out = new BufferedWriter(new FileWriter("plugins/FoundDiamonds/traplocations.txt", true));
            out.write(block1.getX() + ";" + block1.getY() + ";" + block1.getZ());
            out.newLine();
            out.write(block2.getX() + ";" + block2.getY() + ";" + block2.getZ());
            out.newLine();
            out.write(block3.getX() + ";" + block3.getY() + ";" + block3.getZ());
            out.newLine();
            out.close();
          } catch (IOException e) {
              log.log(Level.SEVERE, pName + " Unable to write trap locations to file!", e);
          }

          return true;
        }
        if (randomnumber >= 76) {
          Block block1 = world.getBlockAt(x, y - 1, z);
          Block block2 = world.getBlockAt(x - 1, y - 2, z);
          Block block3 = world.getBlockAt(x + 2, y - 3, z);

          block1.setTypeId(56);
          block2.setTypeId(56);
          block3.setTypeId(56);
          try {
            BufferedWriter out = new BufferedWriter(new FileWriter("plugins/FoundDiamonds/traplocations.txt", true));
            out.write(block1.getX() + ";" + block1.getY() + ";" + block1.getZ());
            out.newLine();
            out.write(block2.getX() + ";" + block2.getY() + ";" + block2.getZ());
            out.newLine();
            out.write(block3.getX() + ";" + block3.getY() + ";" + block3.getZ());
            out.newLine();
            out.close();
          } catch (IOException e) {
              log.log(Level.SEVERE, pName + " Unable to write trap locations to file!", e);
          }
          return true;
        }
        return false;
      }
    }
    return false;
  }
}
