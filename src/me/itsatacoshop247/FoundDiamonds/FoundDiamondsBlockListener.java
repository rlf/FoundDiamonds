package me.itsatacoshop247.FoundDiamonds;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Date;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.ItemStack;

public class FoundDiamondsBlockListener extends BlockListener  {
	public static FoundDiamonds plugin;
	private long lastTimediamonds=0;
	private long lastTimeredstone=0;
	private long lastTimeiron=0;
	private long lastTimegold=0;
	private long lastTimelapis=0;
	private String playername;
	private String blockname;
        public static final Logger log = Logger.getLogger("Minecraft");
        
	public FoundDiamondsBlockListener(FoundDiamonds instance) {
		plugin = instance;
	}
    //@SuppressWarnings("deprecation")
    @Override
	public void onBlockBreak(BlockBreakEvent event){
		
		int randomnumber = (int)(Math.random()*1000);
		
		Block block = event.getBlock();
		Player player = event.getPlayer();
		
		this.playername = event.getPlayer().getName();
		this.blockname = event.getBlock().getType().toString();
		this.blockname = blockname.toLowerCase().replace("_"," ");
		
		Date todaysDate = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss");
		String formattedDate = formatter.format(todaysDate);
                
      if ((block.getType() == Material.DIAMOND_ORE) && (isTrapBlock(player, block))) {
        plugin.getServer().broadcastMessage(ChatColor.DARK_RED + player.getName() + " just broke a FoundDiamonds trap block");
        removeTrapBlockLine(block);
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter("plugins/FoundDiamonds/logs.txt", true));
        out.write("");
        out.newLine();
        out.write("TRAP BLOCK ACTIVATED");
        out.newLine();
        out.write("[" + formattedDate + "] " + "Trap block broken by " + player.getName() + " at (x-" + block.getX() + ", y-" + block.getY() + ", z-" + block.getZ() + ")");
        out.newLine();
        out.write("");
        out.newLine();
        out.close();
      } catch (IOException localIOException) {
          log.log(Level.SEVERE, "Trap broken!  Unable to log to file!", localIOException);
      }
      if (FoundDiamondsLoadSettings.kickontrapbreak) {
        player.kickPlayer(this.playername);
      }
      if (FoundDiamondsLoadSettings.banontrapbreak){
        player.setBanned(true);
      }
      return;
    }
//logging		
		if(block.getType() == Material.DIAMOND_ORE && FoundDiamondsLoadSettings.logging){
			try {
			    BufferedWriter out = new BufferedWriter(new FileWriter("plugins/FoundDiamonds/logs.txt", true));
			    out.write("["+formattedDate+"] "+ block.getType() + " broken by "+player.getName()+" at (x-"+block.getX()+", y-"+block.getY()+", z-"+block.getZ()+")");
			    out.newLine();
			    out.close();
			} catch (IOException e) {
                            log.log(Level.SEVERE, "Unable to log Diamond block to file!", e);
			}
		}
             
//admin messages
		if(block.getType() == Material.DIAMOND_ORE && FoundDiamondsLoadSettings.diamondadmin){
			for(Player p: plugin.getServer().getOnlinePlayers()){
				if(p.hasPermission("FD.admin")){
				p.sendMessage(ChatColor.DARK_RED + player.getName() + " just found Diamonds(AdminMsg)");
				}
			}
		}
		if(block.getType() == Material.IRON_ORE  && FoundDiamondsLoadSettings.ironadmin){
			for(Player p: plugin.getServer().getOnlinePlayers()){
				if(p.hasPermission("FD.admin")){
				p.sendMessage(ChatColor.DARK_RED + player.getName() + " just found Iron(AdminMsg)");
				}
			}
		}
		if(block.getType() == Material.REDSTONE_ORE  && FoundDiamondsLoadSettings.redstoneadmin){
			for(Player p: plugin.getServer().getOnlinePlayers()){
				if(p.hasPermission("FD.admin")){
				p.sendMessage(ChatColor.DARK_RED + player.getName() + " just found Redstone(AdminMsg)");
				}
			}
		}
		if(block.getType() == Material.GOLD_ORE  && FoundDiamondsLoadSettings.goldadmin){
			for(Player p: plugin.getServer().getOnlinePlayers()){
				if(p.hasPermission("FD.admin")){
				p.sendMessage(ChatColor.DARK_RED + player.getName() + " just found Gold(AdminMsg)");
				}
			}
		}
		if(block.getType() == Material.LAPIS_ORE  && FoundDiamondsLoadSettings.lupuslazuliadmin){
			for(Player p: plugin.getServer().getOnlinePlayers()){
				if(p.hasPermission("FD.admin")){
				p.sendMessage(ChatColor.DARK_RED + player.getName() + " just found Lapis Lazul(AdminMsg)");
				}
			}
		}
//timer		
		if(block.getType() == Material.DIAMOND_ORE && FoundDiamondsLoadSettings.diamond && System.currentTimeMillis()-lastTimediamonds > 25000){ //this if statement is entered when the block broken is Diamond
			if(FoundDiamondsLoadSettings.thirtysecondwait){
				lastTimediamonds = System.currentTimeMillis();
			}
//broadcast message
			if(FoundDiamondsLoadSettings.showmessage){
				plugin.getServer().broadcastMessage(ChatColor.AQUA + FoundDiamondsLoadSettings.broadcastmessage.replace("@Player@", playername).replace("@BlockName@", blockname));
			}
//random items
			if (FoundDiamondsLoadSettings.randomitems){
                            
				if(randomnumber < 50){
					if(FoundDiamondsLoadSettings.RandomItem1 == 265) {
						plugin.getServer().broadcastMessage(ChatColor.RED + "Everyone else got some " + ChatColor.GRAY + "Iron");
					} else {
						plugin.getServer().broadcastMessage(ChatColor.RED + "Everyone else got some " +Material.getMaterial(FoundDiamondsLoadSettings.RandomItem1).name());
					}
					for(Player p: plugin.getServer().getOnlinePlayers()){

						p.getInventory().addItem(new ItemStack(FoundDiamondsLoadSettings.RandomItem1, getRandomAmount()));
						p.updateInventory();
					}
				}	
                                else if(randomnumber > 50 && randomnumber < 100){
					if(FoundDiamondsLoadSettings.RandomItem2 == 263) {
						plugin.getServer().broadcastMessage(ChatColor.RED + "Everyone else got " + ChatColor.GRAY + "Coal");
					} else {
						plugin.getServer().broadcastMessage(ChatColor.RED + "Everyone else got some " +Material.getMaterial(FoundDiamondsLoadSettings.RandomItem2).name());
					}
					for(Player p: plugin.getServer().getOnlinePlayers()){

						p.getInventory().addItem(new ItemStack(FoundDiamondsLoadSettings.RandomItem2, getRandomAmount()));
						p.updateInventory();
					}
				}
                               else if(randomnumber > 100 && randomnumber < 150){
					if(FoundDiamondsLoadSettings.RandomItem3 == 341) {
						plugin.getServer().broadcastMessage(ChatColor.RED + "Everyone else got some " + ChatColor.GREEN + "Slime Balls");
					} else {
						plugin.getServer().broadcastMessage(ChatColor.RED + "Everyone else got some " + Material.getMaterial(FoundDiamondsLoadSettings.RandomItem3).name());
					}
					for(Player p: plugin.getServer().getOnlinePlayers()){
						p.getInventory().addItem(new ItemStack(FoundDiamondsLoadSettings.RandomItem3, getRandomAmount()));
						p.updateInventory();
					}
				}
			}
		}
		if(block.getType() == Material.REDSTONE_ORE && FoundDiamondsLoadSettings.redstone && System.currentTimeMillis()-lastTimeredstone > 20000){ //this if statement is entered when the block broken is Diamond
			if(FoundDiamondsLoadSettings.thirtysecondwait){
				lastTimeredstone = System.currentTimeMillis();
			}
			if(FoundDiamondsLoadSettings.showmessage){
				plugin.getServer().broadcastMessage(ChatColor.RED + FoundDiamondsLoadSettings.broadcastmessage.replace("@Player@", playername).replace("@BlockName@", blockname));
			}
		}
		if(block.getType() == Material.GOLD_ORE && FoundDiamondsLoadSettings.gold && System.currentTimeMillis()-lastTimegold > 20000){ //this if statement is entered when the block broken is Diamond
			if(FoundDiamondsLoadSettings.thirtysecondwait){
				lastTimegold = System.currentTimeMillis();
			}
			if(FoundDiamondsLoadSettings.showmessage){
				plugin.getServer().broadcastMessage(ChatColor.GOLD + FoundDiamondsLoadSettings.broadcastmessage.replace("@Player@", playername).replace("@BlockName@", blockname));
			}
		}
		if(block.getType() == Material.IRON_ORE && FoundDiamondsLoadSettings.iron && System.currentTimeMillis()-lastTimeiron > 20000){ //this if statement is entered when the block broken is Diamond
			if(FoundDiamondsLoadSettings.thirtysecondwait){
				lastTimeiron = System.currentTimeMillis();
			}
			if(FoundDiamondsLoadSettings.showmessage){
				plugin.getServer().broadcastMessage(ChatColor.DARK_GRAY + FoundDiamondsLoadSettings.broadcastmessage.replace("@Player@", playername).replace("@BlockName@", blockname));
			}
		}
		if(block.getType() == Material.LAPIS_ORE && FoundDiamondsLoadSettings.lupuslazuli && System.currentTimeMillis()-lastTimelapis > 20000){ //this if statement is entered when the block broken is Diamond
			if(FoundDiamondsLoadSettings.thirtysecondwait){
				lastTimelapis = System.currentTimeMillis();
			}
			if(FoundDiamondsLoadSettings.showmessage){
				plugin.getServer().broadcastMessage(ChatColor.DARK_BLUE + FoundDiamondsLoadSettings.broadcastmessage.replace("@Player@", playername).replace("@BlockName@", blockname));
			}
		}
		
	}
        
    public int getRandomAmount(){
        Random rand = new Random();
        int amount = rand.nextInt(5);
        return amount;
    }
    
    private boolean isTrapBlock(Player p, Block blocky) {
    int count = 0;
    int x = blocky.getX();
    int y = blocky.getY();
    int z = blocky.getZ();
    String check = x + ";" + y + ";" + z;
    try {
      FileInputStream fstream = new FileInputStream("plugins/FoundDiamonds/traplocations.txt");
      DataInputStream in = new DataInputStream(fstream);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));
      String strLine;
      while ((strLine = br.readLine()) != null)
      {
        //String strLine;
        if (strLine.equals(check)) {
          count = 1;
        }
      }

      in.close();
    }
    catch (Exception localException) {
         log.log(Level.SEVERE, "Unable to read Trap blocks from file!", localException);
    }
    return count > 0;
  }

  private void removeTrapBlockLine(Block blockz)
  {
    int x = blockz.getX();
    int y = blockz.getY();
    int z = blockz.getZ();
    String lineToRemove = x + ";" + y + ";" + z;
    try {
      File inputFile = new File("plugins/FoundDiamonds/traplocations.txt");
      //File tempFile = new File("plugins/DisposalChest/traploactionstemp.txt");
      File tempFile = new File("plugins/FoundDiamonds/Disposal/traplocationstemp.txt");

      BufferedReader reader = new BufferedReader(new FileReader(inputFile));
      PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
      String currentLine;
      
      while ((currentLine = reader.readLine()) != null)
      {
        //String currentLine;
        String trimmedLine = currentLine.trim();
        if (!trimmedLine.equals(lineToRemove)) {
          pw.println(trimmedLine);
          pw.flush();    
        }
      }
      reader.close();
      pw.close();
      inputFile.delete();
      tempFile.renameTo(inputFile);
    }
    catch (Exception localException)
    {
        log.log(Level.SEVERE, "Unable to write trap blocks to file!", localException);
    }
  }
}
