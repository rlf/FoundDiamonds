package me.itsatacoshop247.FoundDiamonds;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class FoundDiamondsBlockListener implements Listener  {
    
    
    private FoundDiamonds fd;
    private FoundDiamondsSettings settings;
    private long lastTimeDiamonds = 0;
    private long lastTimeRedstone = 0;
    private long lastTimeIron = 0;
    private long lastTimeGold = 0;
    private long lastTimeLapis = 0;
    private long lastTimeMossy = 0;
    private String playername;
    private String blockname;
    private static final Logger log = Logger.getLogger("FoundDiamonds");
    private String admin = "*.*";
    private String prefix = ChatColor.AQUA + "[FD] ";
    private List<Block> blockList;
    private List<Block> checkedBlocks;
    
        
    public FoundDiamondsBlockListener(FoundDiamonds instance, FoundDiamondsSettings settings) {
        fd = instance;
        this.settings = settings;
    }
        
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (monitoredMaterial(block)) {
            Player player = event.getPlayer();
            if (fd.getEnabledWorlds().contains(player.getWorld().getName())) {
                if (player.getGameMode() == GameMode.CREATIVE && settings.disableInCreativeMode()) {
                    return;
                }
                if (settings.darkMiningDisabled() && miningInTotalDarkness(player, block)) {
                    event.setCancelled(true);
                    player.sendMessage(prefix + ChatColor.DARK_RED + "Mining in total darkness is dangerous, place a torch!");
                    return;
                }    
                if (isTrapBlock(block)) {
                    handleTrapBlock(player, block);
                    return;
                }
                playername = event.getPlayer().getName();
                blockname = block.getType().toString().toLowerCase().replace("_", " ");
                int secondsWait = (settings.getWaitTime() * 1000);
                handleBlock(block, secondsWait);
            }
        }
    }
        
    public int getRandomAmount(){
        Random rand = new Random();
        int amount = rand.nextInt(5);
        return amount;
    }
    
    private boolean isTrapBlock(Block blocky) {
        int count = 0;
        int x = blocky.getX();
        int y = blocky.getY();
        int z = blocky.getZ();
        String check = x + ";" + y + ";" + z;
        try {
            FileInputStream fstream = new FileInputStream(fd.getTrapsFile());
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                if (strLine.equals(check)) {
                    count = 1;
                }
            }
            in.close();
        } catch (Exception localException) {
            log.log(Level.SEVERE, prefix + "Unable to read Trap blocks from file!", localException);
        }
    return count > 0;
    }

    private void removeTrapBlockLine(Block blockz) {
        int x = blockz.getX();
        int y = blockz.getY();
        int z = blockz.getZ();
        String lineToRemove = x + ";" + y + ";" + z;
        try {
            File inputFile = new File("plugins/FoundDiamonds/traplocations.txt");
            File tempFile = new File("plugins/FoundDiamonds/traplocationstemp.txt");
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
            String currentLine;      
            while ((currentLine = reader.readLine()) != null) {
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
        } catch (Exception localException) {
            log.log(Level.SEVERE, prefix + "Unable to write trap blocks to file!", localException);
        }
    }
    
    private void handleTrapBlock(Player player, Block block) {
        if(settings.trapBlockAdminMsg()) {
            for (Player x: fd.getServer().getOnlinePlayers()) {
                if(hasPerms(x) && (x != player)) {
                    x.sendMessage(prefix + ChatColor.DARK_RED + player.getName() + " just broke a FoundDiamonds trap block");
                }
            }
        } 
        if (hasPerms(player)) {
            player.sendMessage(prefix + ChatColor.AQUA + "FoundDiamonds trap block removed");   
        } else {
            fd.getServer().broadcastMessage(prefix + ChatColor.DARK_RED + player.getName() + " just broke a FoundDiamonds trap block");
        } 
        removeTrapBlockLine(block);
        if(settings.loggingIsEnabled()) {
            handleLogging(player, block);
        }    
        if (settings.kickOnTrapBreak()  && !hasPerms(player)) {
            player.kickPlayer("You broke a FoundDiamonds trap block");
        }
        if (settings.banOnTrapBreak() && !hasPerms(player)) {
            player.setBanned(true);
        }
    }
    
    private void handleLogging(Player player, Block block) {
        Date todaysDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss");
        String formattedDate = formatter.format(todaysDate);
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("plugins/FoundDiamonds/logs.txt", true));
            out.write("");
            out.newLine();
            out.write("Trap block broken");
            out.newLine();
            out.write("[" + formattedDate + "] " + "Trap block broken by " + player.getName() + " at (x-" + block.getX() + ", y-" + block.getY() + ", z-" + block.getZ() + ")");
            out.newLine();
            out.write("");
            out.newLine();
            out.close();
        } catch (IOException localIOException) {
            log.log(Level.SEVERE, "Trap broken!  Unable to log to file!", localIOException);
        }
    }
        
    public boolean hasPerms(Player player) {
        return (player.hasPermission("FD.admin") || player.hasPermission(admin) || (settings.opsHavePerms() && player.isOp()));
    }
    
    private void handleRandomItems(int randomNumber) {
        int randomItem;
        if (randomNumber < 50) {
            randomItem = settings.getRandomItem1();
        } else if (randomNumber >= 50 && randomNumber < 100) {
            randomItem = settings.getRandomItem2();
        } else {
            randomItem = settings.getRandomItem3();
        }
        broadcastRandomItem(randomItem);
        giveItems(randomItem, getRandomAmount());
    }
    
    private void broadcastRandomItem(int item) {
        fd.getServer().broadcastMessage(prefix + "Everyone else got some " + ChatColor.GRAY + Material.getMaterial(item).name().toLowerCase().replace("_", " ") + "s");
    }
    
    private void giveItems(int item, int amount) {
        for(Player p: fd.getServer().getOnlinePlayers()) {
            p.getInventory().addItem(new ItemStack(item, amount));
            p.updateInventory();
        }  
    }
    
    private boolean monitoredMaterial(Block block) {
        return fd.getEnabledBlocks().contains(block.getType());
    }

    private void handleBlock(Block block, int secondsWait) {
        String total = String.valueOf(getTotalBlocks(block));
        if (block.getType() == Material.DIAMOND_ORE && settings.broadcastDiamond() && (System.currentTimeMillis()-lastTimeDiamonds > secondsWait)) {
            lastTimeDiamonds = System.currentTimeMillis();
            broadcastFoundBlock(ChatColor.AQUA, total);
            if (settings.randomItems()) {
                int randomNumber = (int)(Math.random()*1000);
                if (randomNumber >= 0 && randomNumber <= 150) {
                    handleRandomItems(randomNumber);
                }
            }
        } else if (block.getType() == Material.GLOWING_REDSTONE_ORE && settings.broadcastRedstone() && (System.currentTimeMillis()-lastTimeRedstone > secondsWait)) {
            lastTimeRedstone = System.currentTimeMillis();
            broadcastFoundBlock(ChatColor.DARK_RED, total);
        } else if (block.getType() == Material.REDSTONE_ORE && settings.broadcastRedstone() && (System.currentTimeMillis()-lastTimeRedstone > secondsWait)) { 
            lastTimeRedstone = System.currentTimeMillis();
            broadcastFoundBlock(ChatColor.DARK_RED, total);
        } else if (block.getType() == Material.MOSSY_COBBLESTONE && settings.broadcastMossy() && (System.currentTimeMillis()-lastTimeMossy > secondsWait)) { 
            lastTimeMossy = System.currentTimeMillis();
            broadcastFoundBlock(ChatColor.DARK_GREEN, total);
        } else if (block.getType() == Material.GOLD_ORE && settings.broadcastGold() && (System.currentTimeMillis()-lastTimeGold > secondsWait)) {
            lastTimeGold = System.currentTimeMillis();
            broadcastFoundBlock(ChatColor.GOLD, total);
        } else if (block.getType() == Material.IRON_ORE && settings.broadcastGold() && (System.currentTimeMillis()-lastTimeIron > secondsWait)) {
            lastTimeIron = System.currentTimeMillis();
            broadcastFoundBlock(ChatColor.GRAY, total);
        } else if (block.getType() == Material.LAPIS_ORE && settings.broadcastLapis() && (System.currentTimeMillis()-lastTimeLapis > secondsWait)) {
            lastTimeLapis = System.currentTimeMillis();
            broadcastFoundBlock(ChatColor.BLUE, total);
        }
    }
    
    private void broadcastFoundBlock(ChatColor color, String total) {
        if (Integer.parseInt(total) > 1) {
            fd.getServer().broadcastMessage(prefix + color + settings.getBroadcastmessage().replace("@Player@", playername).replace("@Number@", total).replace("@BlockName@", blockname + "s"));
        } else {
            fd.getServer().broadcastMessage(prefix + color + settings.getBroadcastmessage().replace("@Player@", playername).replace("@Number@", total).replace("@BlockName@", blockname));
        }    
    }

    private boolean miningInTotalDarkness(Player player, Block block) {
        for (BlockFace y : BlockFace.values()) {
            if (block.getRelative(y).getLightLevel() != 0) {
                return false;
            } 
        }
        return true;
    }
    
    private int getTotalBlocks(Block block) {
        blockList = new LinkedList<Block>();
        checkedBlocks = new LinkedList<Block>();
        blockList.add(block);
        for (BlockFace y : BlockFace.values()) {
            Block cycle = block.getRelative(y);
            if (cycle.getType() == block.getType() && !blockList.contains(cycle) && !checkedBlocks.contains(cycle)) {
                blockList.add(cycle);
                checkCyclesRelative(cycle);
            } else {
                if (!checkedBlocks.contains(cycle)) {
                    checkedBlocks.add(cycle);
                }
            }
        }
        return blockList.size();
    }

    private void checkCyclesRelative(Block cycle) {
        for (BlockFace y : BlockFace.values()) {
            Block secondCycle = cycle.getRelative(y);
            if (secondCycle.getType() == cycle.getType() && !blockList.contains(secondCycle) && !checkedBlocks.contains(secondCycle)) {
                blockList.add(secondCycle);
                checkCyclesRelative(secondCycle);
            } else {
                if (!checkedBlocks.contains(secondCycle)) {
                    checkedBlocks.add(secondCycle);
                }
            }
        }
    }
    
 }
  

