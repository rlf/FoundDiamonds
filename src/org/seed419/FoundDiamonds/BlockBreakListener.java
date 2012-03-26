package org.seed419.FoundDiamonds;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BlockBreakListener implements Listener  {


    private FoundDiamonds fd;
    private YAMLHandler config;
    private static final Logger log = Logger.getLogger("FoundDiamonds");
    private String prefix = ChatColor.WHITE + "[FD] ";
    private String adminPrefix = ChatColor.RED + "[FD Admin] ";
    private List<Block> blockList;
    private List<Block> checkedBlocks;


    public BlockBreakListener(FoundDiamonds instance, YAMLHandler config) {
        fd = instance;
        this.config = config;
    }




    /*
     * BlockBreakEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        //Prevent mcMMO's superbreaker from re-announcing.
        if (event.getEventName().equalsIgnoreCase("FakeBlockBreakEvent")) {
            return;
        }

        Block block = event.getBlock();
        Material mat = block.getType();
        Player player = event.getPlayer();

        //Check for trap block first, as it can be any block
        if (isTrapBlock(block)) {
            handleTrapBlock(player, block);
            return;
        }

        //Handle every other material
        if (mat == Material.DIAMOND_ORE) {
            if (wasPlacedRemove(block)) {
                return;
            }
            materialNeedsHandled(player, mat, block, event);
            if (fd.getConfig().getBoolean(config.getLogDiamondBreaks())) {
                handleLogging(player, block, false);
            }
        } else if (mat == Material.REDSTONE_ORE || mat == Material.GLOWING_REDSTONE_ORE || mat == Material.OBSIDIAN
                || mat == Material.GOLD_ORE || mat == Material.LAPIS_ORE || mat == Material.IRON_ORE ||
                mat == Material.COAL_ORE || mat == Material.MOSSY_COBBLESTONE) {
            if (wasPlacedRemove(block)) {
                return;
            }
            materialNeedsHandled(player, mat, block, event);
        }
    }




    /*
     * Main handler
     */
    private void materialNeedsHandled(Player player, Material mat, Block block, BlockBreakEvent event) {
        if (!player.hasPermission("fd.messages")) {
            isAdminMessageMaterial(player, mat, block);
        }
        if (monitoredMaterial(mat)) {
            //remove already announced blocks here.
            if (alreadyAnnounced(block.getLocation())) {
                fd.getAnnouncedBlocks().remove(block.getLocation());
                return;
            }
            if (!isValidWorld(player)) {
                return;
            }
            if (!isValidGameMode(player)) {
                return;
            }
            if (isTooDark(player, block, event)) {
                return;
            }
            String playername = getBroadcastName(player);
            String blockname = mat.toString().toLowerCase().replace("_", " ");
            handleBroadcast(player, block, playername, blockname);
        }
    }




    /*
     * Placed block methods
     */
    private boolean wasPlacedRemove(Block block) {
        if (fd.getPlacedBlocks().contains(block.getLocation())) {
            fd.getPlacedBlocks().remove(block.getLocation());
            return true;
        }
        return false;
    }

    private boolean wasPlaced(Block block) {
        return (fd.getPlacedBlocks().contains(block.getLocation()));
    }




    /*
     * Admin Message Handlers
     */

    private void isAdminMessageMaterial(Player player, Material mat, Block block) {
        if (!alreadyAnnounced(block.getLocation())) {
            if ((mat == Material.DIAMOND_ORE && fd.getConfig().getBoolean(config.getDiamondAdmin())) ||
                (mat == Material.GOLD_ORE && fd.getConfig().getBoolean(config.getGoldAdmin())) ||
                (mat == Material.LAPIS_ORE && fd.getConfig().getBoolean(config.getLapisAdmin())) ||
                (mat == Material.IRON_ORE && fd.getConfig().getBoolean(config.getIronAdmin())) ||
                (mat == Material.GLOWING_REDSTONE_ORE && fd.getConfig().getBoolean(config.getRedstoneAdmin())) ||
                (mat == Material.REDSTONE_ORE && fd.getConfig().getBoolean(config.getRedstoneAdmin()))) {
                handleAdminMessage(player, mat, block);
            }
        }
    }

    private void handleAdminMessage(Player player, Material mat, Block block) {
        int total = getTotalBlocks(block);
        String name = player.getName();
        String matName = mat.name().toLowerCase().replace("_", " ");
        for (Player x : fd.getServer().getOnlinePlayers()) {
            if (fd.getAdminMessageMap().containsKey(x)) {
                if (fd.getAdminMessageMap().get(x)) {
                    if (fd.hasPerms(x, "fd.messages")) {
                        sendAdminMessage(x, mat, total, name, matName);
                    }
                }
            }
        }
    }

    //TODO send to console
    private void sendAdminMessage(Player x, Material mat, int total, String name, String matName) {
        if (mat == Material.GLOWING_REDSTONE_ORE || mat == Material.REDSTONE_ORE) {
            if (total > 1) {
                x.sendMessage(adminPrefix  + ChatColor.YELLOW +
                fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name + ChatColor.GRAY
                ).replace("@Number@", String.valueOf(total)).replace("@BlockName@", "redstone ores"));
            } else {
                x.sendMessage(adminPrefix + ChatColor.YELLOW +
                fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name + ChatColor.GRAY
                ).replace("@Number@", String.valueOf(total)).replace("@BlockName@", "redstone ore"));
            }
        } else if (mat == Material.OBSIDIAN) {
            x.sendMessage(adminPrefix + ChatColor.YELLOW +
            fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name + ChatColor.GRAY
            ).replace("@Number@", String.valueOf(total)).replace("@BlockName@", "obsidian"));
        } else {
            if (total > 1) {
                x.sendMessage(adminPrefix + ChatColor.YELLOW +
                fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name + ChatColor.GRAY
                ).replace("@Number@", String.valueOf(total)).replace("@BlockName@", matName  + "s"));
            } else {
                x.sendMessage(adminPrefix + ChatColor.YELLOW +
                fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name + ChatColor.GRAY
                ).replace("@Number@", String.valueOf(total)).replace("@BlockName@", matName  + "s"));
            }
        }
    }




    /*
     * Trap block handlers
     */
    private boolean isTrapBlock(Block block) {
        if (fd.getTrapBlocks().contains(block.getLocation())) {
            return true;
        }
        return false;
    }

    private void removeTrapBlock(Block block) {
        fd.getTrapBlocks().remove(block.getLocation());
    }

    private void handleTrapBlock(Player player, Block block) {
        if(fd.getConfig().getBoolean(config.getAdminAlertsOnAllTrapBreaks())) {
            for (Player x: fd.getServer().getOnlinePlayers()) {
                if(fd.hasPerms(x, "FD.messages") && (x != player)) {
                    x.sendMessage(prefix + ChatColor.RED + player.getName() + " just broke a trap block");
                }
            }
        }
        if (fd.hasPerms(player, "FD.trap")) {
            player.sendMessage(prefix + ChatColor.AQUA + "Trap block removed");
        } else {
            fd.getServer().broadcastMessage(prefix + ChatColor.RED + player.getName() + " just broke a trap block");
        }
        if(fd.getConfig().getBoolean(config.getLogDiamondBreaks())) {
            handleLogging(player, block, true);
        }
        if (fd.getConfig().getBoolean(config.getKickOnTrapBreak())  && !fd.hasPerms(player, "FD.trap")) {
            player.kickPlayer(fd.getConfig().getString(config.getKickMessage()));
        }
        if (fd.getConfig().getBoolean(config.getBanOnTrapBreak()) && !fd.hasPerms(player, "FD.trap")) {
            player.setBanned(true);
        }
        removeTrapBlock(block);
    }




    /*
     * Logging Handlers
     */
    private void handleLogging(Player player, Block block, boolean trapBlock) {
        Date todaysDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss");
        String formattedDate = formatter.format(todaysDate);
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fd.getLogFile(), true));
            if (trapBlock) {
                out.write("[TRAP BLOCK]");
                out.newLine();
            }
            out.write("[" + formattedDate + "] " + block.getType().name() + " broken by "
                    + player.getName() + " at (x-" + block.getX() + ", y-" + block.getY() + ", z-" + block.getZ() + ")");
            out.newLine();
            out.close();
        } catch (IOException ex) {
            log.severe(MessageFormat.format("[{0}] Unable to write block to log file! {1}", fd.getPluginName(), ex));
        }
    }

    private void writeToCleanLog(Material mat, int total, String name, String block) {
        Date todaysDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss");
        String formattedDate = formatter.format(todaysDate);        String message;
        if (mat == Material.GLOWING_REDSTONE_ORE || mat == Material.REDSTONE_ORE) {
            if (total > 1) {
                message = fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@", "redstone ores");
            } else {
                message = fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@", "redstone ore");
            }
        } else if (mat == Material.OBSIDIAN) {
                message = fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@", "obsidian");
        } else {
            if (total > 1) {
                message = fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@", block +
                        (mat == Material.DIAMOND_ORE ? "s!" : "s"));
            } else {
                message = fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@", block +
                        (mat == Material.DIAMOND_ORE ? "!" : ""));
            }
        }
        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(fd.getCleanLog(), true));
            br.write("[" + formattedDate + "] " + message);
            br.newLine();
            br.flush();
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(BlockBreakListener.class.getName()).log(Level.SEVERE, "Couldn't write to clean log!", ex);
        }
    }




    /*
     * Random Item methods
     */

    private void handleRandomItems(int randomNumber) {
        int randomItem;
        if (randomNumber < 50) {
            randomItem = fd.getConfig().getInt(config.getRandomItem1());
        } else if (randomNumber >= 50 && randomNumber < 100) {
            randomItem = fd.getConfig().getInt(config.getRandomItem2());
        } else {
            randomItem = fd.getConfig().getInt(config.getRandomItem3());
        }
        broadcastRandomItem(randomItem);
        giveItems(randomItem, getRandomAmount());
    }

    private void broadcastRandomItem(int item) {
        if (Material.getMaterial(item) == Material.COAL || Material.getMaterial(item) == Material.OBSIDIAN) {
            fd.getServer().broadcastMessage(prefix + "Everyone else got some "
                    + ChatColor.GRAY + Material.getMaterial(item).name().toLowerCase().replace("_", " ") + "");
        } else {
            fd.getServer().broadcastMessage(prefix + "Everyone else got some "
                    + ChatColor.GRAY + Material.getMaterial(item).name().toLowerCase().replace("_", " ") + "s");
        }
    }

    @SuppressWarnings("deprecation")
    private void giveItems(int item, int amount) {
        for(Player p: fd.getServer().getOnlinePlayers()) {
            p.getInventory().addItem(new ItemStack(item, amount));
            p.updateInventory();
        }
    }

    private int getRandomAmount(){
        Random rand = new Random();
        int amount = rand.nextInt(3);
        return amount;
    }




    /*
     * Spells
     */

    private void handleRandomPotions(int randomNumber) {
        PotionEffect potion;
        if (randomNumber < 25) {
            potion = new PotionEffect(PotionEffectType.SPEED, 3000, fd.getConfig().getInt(config.getPotionStrength()));
        } else if (randomNumber >= 25 && randomNumber < 50) {
            potion = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 3000, fd.getConfig().getInt(config.getPotionStrength()));
        } else if (randomNumber >=50 && randomNumber < 100) {
            potion = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3000, fd.getConfig().getInt(config.getPotionStrength()));
        } else if (randomNumber >=100 && randomNumber < 125) {
            potion = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 3000, fd.getConfig().getInt(config.getPotionStrength()));
        } else {
            potion = new PotionEffect(PotionEffectType.JUMP, 3000, fd.getConfig().getInt(config.getPotionStrength()));
        }
        givePotions(potion);
    }

    private void givePotions(PotionEffect potion) {
        for (Player p : fd.getServer().getOnlinePlayers()) {
            if (!p.hasPotionEffect(potion.getType())) {
                p.addPotionEffect(potion);
                if (potion.getType() == PotionEffectType.JUMP) {
                    fd.getJumpPotion().put(p, Boolean.TRUE);
                }
                p.sendMessage(prefix + ChatColor.DARK_RED + "You feel an energy come over you...");
            }
        }
    }




    /*
     * Broadcasting
     */

    private void handleBroadcast(Player player, Block block, String playername, String blockname) {
        Material blockMaterial = block.getType();
        int total = getTotalBlocks(block);
        if (blockMaterial == Material.DIAMOND_ORE && fd.getConfig().getBoolean(config.getBcDiamond())) {
            broadcastFoundBlock(blockMaterial, ChatColor.AQUA, total, playername, blockname);
            if (fd.getConfig().getBoolean(config.getAwardsForFindingDiamonds())) {
                int randomInt = (int) (Math.random()*100);
                if (randomInt <= fd.getConfig().getInt(config.getPercentToGetItem())) {
                    int randomNumber = (int)(Math.random()*150);
                    if (randomNumber >= 0 && randomNumber <= 150) {
                        handleRandomItems(randomNumber);
                    }
                }
            }
            if (fd.getConfig().getBoolean(config.getPotionsForFindingDiamonds())) {
                int randomInt = (int) (Math.random()*100);
                if (randomInt <= fd.getConfig().getInt(config.getPercentToGetPotion())) {
                    int randomNumber = (int)(Math.random()*150);
                    if (randomNumber >= 0 && randomNumber <= 150) {
                        handleRandomPotions(randomNumber);
                    }
                }
            }
        } else if ((blockMaterial == Material.REDSTONE_ORE || blockMaterial == Material.GLOWING_REDSTONE_ORE)
                && fd.getConfig().getBoolean(config.getBcRedstone())) {
            broadcastFoundBlock(blockMaterial, ChatColor.DARK_RED, total, playername, blockname);
        } else if (blockMaterial == Material.MOSSY_COBBLESTONE && fd.getConfig().getBoolean(config.getBcMossy())) {
            broadcastFoundBlock(blockMaterial, ChatColor.DARK_GREEN, total, playername, blockname);
        } else if (blockMaterial == Material.GOLD_ORE && fd.getConfig().getBoolean(config.getBcGold())) {
            broadcastFoundBlock(blockMaterial, ChatColor.GOLD, total, playername, blockname);
        } else if (blockMaterial == Material.IRON_ORE && fd.getConfig().getBoolean(config.getBcIron())) {
            broadcastFoundBlock(blockMaterial, ChatColor.GRAY, total, playername, blockname);
        } else if (blockMaterial == Material.LAPIS_ORE && fd.getConfig().getBoolean(config.getBcLapis())) {
            broadcastFoundBlock(blockMaterial, ChatColor.BLUE, total, playername, blockname);
        } else if (blockMaterial == Material.COAL_ORE && fd.getConfig().getBoolean(config.getBcCoal())) {
            broadcastFoundBlock(blockMaterial, ChatColor.DARK_GRAY, total, playername, blockname);
        }else if (blockMaterial == Material.OBSIDIAN && fd.getConfig().getBoolean(config.getBcObby())) {
            broadcastFoundBlock(blockMaterial, ChatColor.DARK_PURPLE, total, playername, blockname);
        }
    }

    private void broadcastFoundBlock(Material mat, ChatColor color, int total, String name, String block) {
        String message;
        if (mat == Material.GLOWING_REDSTONE_ORE || mat == Material.REDSTONE_ORE) {
            if (total > 1) {
                message = (fd.getConfig().getBoolean(config.getIncludePrefix()) ? prefix : "") + color +
                        fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@",
                        (fd.getConfig().getBoolean(config.getUseOreColors()) ? color : "") + "redstone ores");
            } else {
                message = (fd.getConfig().getBoolean(config.getIncludePrefix()) ? prefix : "") + color +
                        fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@",
                        (fd.getConfig().getBoolean(config.getUseOreColors()) ? color : "") + "redstone ore");
            }
        } else if (mat == Material.OBSIDIAN) {
                message = (fd.getConfig().getBoolean(config.getIncludePrefix()) ? prefix : "") + color +
                        fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@",
                        (fd.getConfig().getBoolean(config.getUseOreColors()) ? color : "") + "obsidian");
        } else {
            if (total > 1) {
                message = (fd.getConfig().getBoolean(config.getIncludePrefix()) ? prefix : "") + color +
                        fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@",
                        (fd.getConfig().getBoolean(config.getUseOreColors()) ? color : "") + block +
                        (mat == Material.DIAMOND_ORE ? "s!" : "s"));
            } else {
                message = (fd.getConfig().getBoolean(config.getIncludePrefix()) ? prefix : "") + color +
                        fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@",
                        (fd.getConfig().getBoolean(config.getUseOreColors()) ? color : "") + block +
                        (mat == Material.DIAMOND_ORE ? "!" : ""));
            }
        }
        String formatted = translateAlternateColorCodes('&', message);
        fd.getServer().broadcastMessage(formatted);
        if (fd.getConfig().getBoolean(config.getCleanLog())) {
            writeToCleanLog(mat, total, name, block);
        }
    }

    private boolean monitoredMaterial(Material mat) {
        return fd.getEnabledBlocks().contains(mat);
    }

    private String getBroadcastName(Player player) {
        if (fd.getConfig().getBoolean(config.getUseNick())) {
            return player.getDisplayName();
        } else {
            return player.getName();
        }
    }




    /*
     * Craftbukkit lol
     */

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKk".indexOf(b[i+1]) > -1) {
                b[i] = ChatColor.COLOR_CHAR;
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }
        return new String(b);
    }




    /*
     * Total block counters
     */
    private int getTotalBlocks(Block origBlock) {
        blockList = new LinkedList<Block>();
        checkedBlocks = new LinkedList<Block>();
        //This sucks...how to stop mcmmo spam??
        fd.getAnnouncedBlocks().add(origBlock.getLocation());
        blockList.add(origBlock);
        for (BlockFace y : BlockFace.values()) {
            Block cycle = origBlock.getRelative(y);
            if ((cycle.getType() == origBlock.getType() && !blockList.contains(cycle) && !checkedBlocks.contains(cycle) && !wasPlaced(cycle)) ||
                    ((isRedstone(origBlock) && isRedstone(cycle)) &&
                    !blockList.contains(cycle) && !checkedBlocks.contains(cycle) && !wasPlaced(cycle))) {
                fd.getAnnouncedBlocks().add(cycle.getLocation());
                //System.out.println("Total+=" + cycle.getType().name() + " X: "+ cycle.getX() + " Y:" + cycle.getY() + " Z:" + cycle.getZ());
                blockList.add(cycle);
                checkCyclesRelative(origBlock, cycle);
            } else {
                if (!checkedBlocks.contains(cycle)) {
                    checkedBlocks.add(cycle);
                }
            }
        }
        return blockList.size();
    }

    private void checkCyclesRelative(Block origBlock, Block cycle) {
        for (BlockFace y : BlockFace.values()) {
            Block secondCycle = cycle.getRelative(y);
            if ((secondCycle.getType() == origBlock.getType() && !blockList.contains(secondCycle) && !checkedBlocks.contains(secondCycle) && !wasPlaced(secondCycle)) ||
               (isRedstone(origBlock) && isRedstone(secondCycle) && (!blockList.contains(secondCycle) && !checkedBlocks.contains(secondCycle) && !wasPlaced(secondCycle)))) {
                blockList.add(secondCycle);
                fd.getAnnouncedBlocks().add(secondCycle.getLocation());
                //System.out.println("Total+=" + secondCycle.getType().name() + " X: "+ secondCycle.getX() + " Y:" + secondCycle.getY() + " Z:" + secondCycle.getZ());
                checkCyclesRelative(origBlock, secondCycle);
            } else {
                if (!checkedBlocks.contains(secondCycle)) {
                    checkedBlocks.add(secondCycle);
                }
            }
        }
    }




    /*
     * Other Methods
     */
    public String getPrefix() {
        return prefix;
    }

    private boolean isRedstone(Block m) {
        return (m.getType() == Material.REDSTONE_ORE || m.getType() == Material.GLOWING_REDSTONE_ORE);
    }

    private boolean isValidWorld(Player player) {
        return fd.getConfig().getList(config.getEnabledWorlds()).contains(player.getWorld().getName());
    }

    private boolean isValidGameMode(Player player) {
        return !((player.getGameMode() == GameMode.CREATIVE) && (fd.getConfig().getBoolean(config.getDisableInCreative())));
    }

    private boolean alreadyAnnounced(Location loc) {
        return (fd.getAnnouncedBlocks().contains(loc));
    }




    /*
     * Light Methods
     */
    private boolean blockSeesNoLight(Block block) {
        for (BlockFace y : BlockFace.values()) {
            if (block.getRelative(y).getLightLevel() != 0) {
                return false;
            }
        }
        return true;
    }

    private boolean isTooDark(Player player, Block block, BlockBreakEvent event) {
        if (fd.getConfig().getBoolean(config.getDisableMiningInTotalDarkness()) && blockSeesNoLight(block)) {
            event.setCancelled(true);
            player.sendMessage(prefix + ChatColor.RED + "Mining in total darkness is dangerous, place a torch!");
            return true;
        }
        return false;
    }

}