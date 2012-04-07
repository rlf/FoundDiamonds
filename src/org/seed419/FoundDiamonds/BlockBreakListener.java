package org.seed419.FoundDiamonds;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
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
    private List<Block> blockList;
    private List<Block> checkedBlocks;
    private List<Player> recievedAdminMessage = new LinkedList<Player>();
    private boolean consoleRecieved;
    private boolean debug = false;


    public BlockBreakListener(FoundDiamonds instance, YAMLHandler config) {
        fd = instance;
        this.config = config;
    }




    /*
     * BlockBreakEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        //Check for debug mode
        debug = fd.getConfig().getBoolean(config.debug());

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

        //Handle diamond specially because it can send rewards and spells.
        if (mat == Material.DIAMOND_ORE) {
            if (wasPlacedRemove(block)) {
                return;
            }
            materialNeedsHandled(player, mat, block, event);
            if (fd.getConfig().getBoolean(config.getLogDiamondBreaks())) {
                handleLogging(player, block, false);
            }

        //Handle any other enabled materials.
        } else if (fd.getEnabledBlocks().contains(mat)) {
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
        if (alreadyAnnounced(block.getLocation())) {
            fd.getAnnouncedBlocks().remove(block.getLocation());
            if (debug) {
                log.info(FoundDiamonds.getDebugPrefix() + "Broadcast canceled: Block already announced.");
            }
            return;
        }
        if (!player.hasPermission("fd.messages")) {
            isAdminMessageMaterial(player, mat, block);
        }
        if (monitoredMaterial(mat)) {
            if (!isValidWorld(player)) {
                if (debug) {
                    log.info(FoundDiamonds.getDebugPrefix() + "Broadcast canceled: User is not in an enabled world.");
                }
                return;
            }
            if (!isValidGameMode(player)) {
                if (debug) {
                    log.info(FoundDiamonds.getDebugPrefix() + "Broadcast canceled: User is in creative mode.");
                }
                return;
            }
            if (mat != Material.COAL_ORE) {
                if (isTooDark(player, block, event)) {
                    return;
            }
            }
            String playername = getBroadcastName(player);
            handleBroadcast(mat, block, playername);
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
                handleAdminMessage(player, block);
            }
        }
    }

    private void handleAdminMessage(Player player, Block block) {
        BlockInformation info = getBlockInformation(block);
        String playerName = player.getName();
        sendAdminMessage(info, playerName);
    }

    private void sendAdminMessage(BlockInformation b, String playerName) {
        String message = formatMessage(FoundDiamonds.getAdminPrefix(), b.getMaterial(), b.getColor(), b.getTotal(), playerName);
        //This is incredibly confusing, but must be done.
        String formatted = customTranslateAlternateColorCodes('&', message);
        fd.getServer().getConsoleSender().sendMessage(formatted);
        consoleRecieved = true;
        for (Player y : fd.getServer().getOnlinePlayers()) {
            if (fd.getAdminMessageMap().containsKey(y)) {
                if (fd.getAdminMessageMap().get(y)) {
                    y.sendMessage(formatted);
                    recievedAdminMessage.add(y);
                    if (debug) {
                        log.info(FoundDiamonds.getDebugPrefix() + "Sent admin message to " + y.getName());
                    }
                } else {
                    if (debug) {
                        log.info(FoundDiamonds.getDebugPrefix() + y.getName() + "'s admin messages are toggled off.");
                    }
                }
            } else {
                if (debug) {
                    log.info(FoundDiamonds.getDebugPrefix() + y.getName() + " doesn't have permission fd.messages");
                }
            }
        }
    }

    private void sendLightAdminMessage(Player player, Block block, int lightLevel) {
        String lightAdminMessage = FoundDiamonds.getAdminPrefix() + ChatColor.YELLOW + player.getName() +
                ChatColor.GRAY +" was denied mining " + ChatColor.YELLOW +
                block.getType().name().toLowerCase().replace("_", " ") + ChatColor.GRAY + " at"
                            + " light level " + ChatColor.WHITE +  lightLevel + ".";
        fd.getServer().getConsoleSender().sendMessage(lightAdminMessage);
        for (Player y : fd.getServer().getOnlinePlayers()) {
            if (fd.getAdminMessageMap().containsKey(y)) {
                if (fd.getAdminMessageMap().get(y)) {
                    y.sendMessage(lightAdminMessage);
                    if (debug) {
                        log.info(FoundDiamonds.getDebugPrefix() + "Sent admin message to " + y.getName());
                    }
                } else {
                    if (debug) {
                        log.info(FoundDiamonds.getDebugPrefix() + y.getName() + "'s admin messages are toggled off.");
                    }
                }
            } else {
                if (debug) {
                    log.info(FoundDiamonds.getDebugPrefix() + y.getName() + " either doesn't have permission 'fd.messages' or needs to turn them on with /fd admin");
                }
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
                    x.sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + player.getName() + " just broke a trap block");
                }
            }
        }
        if (fd.hasPerms(player, "FD.trap")) {
            player.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + "Trap block removed");
        } else {
            fd.getServer().broadcastMessage(FoundDiamonds.getPrefix() + ChatColor.RED + player.getName() + " just broke a trap block");
        }
        if(fd.getConfig().getBoolean(config.getLogTrapBreaks())) {
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
                out.write("[TRAP BLOCK] ");
            }
            out.write("" + formattedDate + " " + block.getType().name() + " broken by "
                    + player.getName() + " at (x: " + block.getX() + ", y: " + block.getY() + ", z: " + block.getZ() + ") in world: " + player.getWorld().getName());
            out.newLine();
            out.close();
        } catch (IOException ex) {
            log.severe(MessageFormat.format("[{0}] Unable to write block to log file! {1}", FoundDiamonds.getPrefix(), ex));
        }
    }

    private void writeToCleanLog(Material mat, int total, String playerName) {
        Date todaysDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss");
        String formattedDate = formatter.format(todaysDate);        String message;
        if (mat == Material.GLOWING_REDSTONE_ORE || mat == Material.REDSTONE_ORE) {
            if (total > 1) {
                message = fd.getConfig().getString(config.getBcMessage()).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@", "redstone ores");
            } else {
                message = fd.getConfig().getString(config.getBcMessage()).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@", "redstone ore");
            }
        } else if (mat == Material.OBSIDIAN) {
                message = fd.getConfig().getString(config.getBcMessage()).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@", "obsidian");
        } else {
            String blockName = mat.name().replace("_", " ");
            if (total > 1) {
                message = fd.getConfig().getString(config.getBcMessage()).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@", blockName +
                        (mat == Material.DIAMOND_ORE ? "s!" : "s"));
            } else {
                message = fd.getConfig().getString(config.getBcMessage()).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@", blockName +
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
            fd.getServer().broadcastMessage(FoundDiamonds.getPrefix() + "Everyone else got some "
                    + ChatColor.GRAY + Material.getMaterial(item).name().toLowerCase().replace("_", " ") + "");
        } else {
            fd.getServer().broadcastMessage(FoundDiamonds.getPrefix() + "Everyone else got some "
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
            if (!p.hasPotionEffect(potion.getType()) && fd.getConfig().getList(config.getEnabledWorlds()).contains(p.getWorld().getName())) {
                p.addPotionEffect(potion);
                if (potion.getType() == PotionEffectType.JUMP) {
                    fd.getJumpPotion().put(p, Boolean.TRUE);
                }
                p.sendMessage(FoundDiamonds.getPrefix() + ChatColor.DARK_RED + "You feel an energy come over you...");
            }
        }
    }




    /*
     * Broadcasting
     */

    private void handleBroadcast(Material mat, Block block, String playername) {
        if (mat == Material.DIAMOND_ORE && fd.getConfig().getBoolean(config.getBcDiamond())) {
            BlockInformation info = getBlockInformation(block);
            broadcastFoundBlock(info, playername);
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
        } else if (((mat == Material.REDSTONE_ORE || mat == Material.GLOWING_REDSTONE_ORE) && fd.getConfig().getBoolean(config.getBcRedstone())) ||
                mat == Material.MOSSY_COBBLESTONE && fd.getConfig().getBoolean(config.getBcMossy()) ||
                mat == Material.GOLD_ORE && fd.getConfig().getBoolean(config.getBcGold()) ||
                mat == Material.IRON_ORE && fd.getConfig().getBoolean(config.getBcIron()) ||
                mat == Material.LAPIS_ORE && fd.getConfig().getBoolean(config.getBcLapis()) ||
                mat == Material.COAL_ORE && fd.getConfig().getBoolean(config.getBcCoal()) ||
                mat == Material.OBSIDIAN && fd.getConfig().getBoolean(config.getBcObby())) {
            BlockInformation info = getBlockInformation(block);
            broadcastFoundBlock(info, playername);
        }
    }

    private void broadcastFoundBlock(BlockInformation b, String playerName) {
        String message = formatMessage(FoundDiamonds.getPrefix(), b.getMaterial(), b.getColor(), b.getTotal(), playerName);
        String formatted = customTranslateAlternateColorCodes('&', message);

        //Prevent redunant output to the console if an admin message was already sent.
        if (!consoleRecieved) {
            fd.getServer().getConsoleSender().sendMessage(formatted);
        }

        for (Player x : fd.getServer().getOnlinePlayers()) {
            if (!x.hasPermission("ignore.broadcasts") && isValidWorld(x)) {
                if (!recievedAdminMessage.contains(x)) {
                    x.sendMessage(formatted);
                    if (debug) {
                        log.info(FoundDiamonds.getDebugPrefix() + "Sent broadcast to " + x.getName());
                    }
                } else if (debug) {
                    log.info(FoundDiamonds.getDebugPrefix() + x.getName() + "recieved an admin message already, so not broadcasting to " + x.getName());
                }
            } else {
                if (debug) {
                    if (x.hasPermission("ignore.broadcasts")) {
                        log.info(FoundDiamonds.getDebugPrefix() + x.getName() + " has permissions 'ignore.broadcasts'.  Not broadcasting to " + x.getName());
                    }
                    if (!isValidWorld(x)) {
                        log.info(FoundDiamonds.getDebugPrefix() + x.getName() + " is not in an enabled world, so not broadcasting to  " + x.getName());
                    }
                }
            }
        }

        //reset message checks after successful broadcast
        recievedAdminMessage.clear();
        consoleRecieved = false;

        //write to log if cleanlogging.
        if (fd.getConfig().getBoolean(config.getCleanLog())) {
            writeToCleanLog(b.getMaterial(), b.getTotal(), playerName);
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
     * Total block counters
     */
    private int getTotalBlocks(Block origBlock) {
        blockList = new LinkedList<Block>();
        checkedBlocks = new LinkedList<Block>();
        //fd.getAnnouncedBlocks().add(origBlock.getLocation());
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
    private String formatMessage(String pre, Material mat, ChatColor color, int total, String playerName) {
        String message;
        if (mat == Material.GLOWING_REDSTONE_ORE || mat == Material.REDSTONE_ORE) {
            if (total > 1) {
                message = (fd.getConfig().getBoolean(config.getIncludePrefix()) ? pre : "") + color +
                        fd.getConfig().getString(config.getBcMessage()).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@",
                        (fd.getConfig().getBoolean(config.getUseOreColors()) ? color : "") + "redstone ores");
            } else {
                message = (fd.getConfig().getBoolean(config.getIncludePrefix()) ? pre : "") + color +
                        fd.getConfig().getString(config.getBcMessage()).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@",
                        (fd.getConfig().getBoolean(config.getUseOreColors()) ? color : "") + "redstone ore");
            }
        } else if (mat == Material.OBSIDIAN) {
                message = (fd.getConfig().getBoolean(config.getIncludePrefix()) ? pre : "") + color +
                        fd.getConfig().getString(config.getBcMessage()).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@",
                        (fd.getConfig().getBoolean(config.getUseOreColors()) ? color : "") + "obsidian");
        } else {
            String matName = mat.name().toLowerCase().replace("_", " ");
            if (total > 1) {
                message = (fd.getConfig().getBoolean(config.getIncludePrefix()) ? pre : "") + color +
                        fd.getConfig().getString(config.getBcMessage()).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@",
                        (fd.getConfig().getBoolean(config.getUseOreColors()) ? color : "") + matName +
                        (mat == Material.DIAMOND_ORE ? "s!" : "s"));
            } else {
                message = (fd.getConfig().getBoolean(config.getIncludePrefix()) ? pre : "") + color +
                        fd.getConfig().getString(config.getBcMessage()).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(total)).replace("@BlockName@",
                        (fd.getConfig().getBoolean(config.getUseOreColors()) ? color : "") + matName +
                        (mat == Material.DIAMOND_ORE ? "!" : ""));
            }
        }
        return message;
    }

    private BlockInformation getBlockInformation(Block block) {
        return new BlockInformation(getTotalBlocks(block), getOreColor(block.getType()), block.getType());
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

    public static String customTranslateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] charArray = textToTranslate.toCharArray();
        for (int i = 0; i < charArray.length - 1; i++) {
            if (charArray[i] == altColorChar && "0123456789AaBbCcDdEeFfKkNnRrLlMmOo".indexOf(charArray[i+1]) > -1) {
                charArray[i] = ChatColor.COLOR_CHAR;
                charArray[i+1] = Character.toLowerCase(charArray[i+1]);
            }
        }
        return new String(charArray);
    }

    public ChatColor getOreColor(Material blockMaterial) {
        if (blockMaterial == Material.DIAMOND_ORE) {
            return ChatColor.AQUA;
        } else if (blockMaterial == Material.REDSTONE_ORE || blockMaterial == Material.GLOWING_REDSTONE_ORE) {
            return ChatColor.DARK_RED;
        } else if (blockMaterial == Material.MOSSY_COBBLESTONE) {
            return ChatColor.DARK_GREEN;
        } else if (blockMaterial == Material.GOLD_ORE) {
            return ChatColor.GOLD;
        } else if (blockMaterial == Material.IRON_ORE) {
            return ChatColor.GRAY;
        } else if (blockMaterial == Material.LAPIS_ORE) {
            return ChatColor.BLUE;
        } else if (blockMaterial == Material.COAL_ORE) {
            return ChatColor.DARK_GRAY;
        }else if (blockMaterial == Material.OBSIDIAN) {
            return ChatColor.DARK_PURPLE;
        }
        return ChatColor.WHITE;
    }




    /*
     * Light Methods
     */
    private boolean blockSeesNoLight(Player player, Block block) {
        double percentage = Double.parseDouble(fd.getConfig().getString(config.getPercentOfLightRequired()).replaceAll("%", ""));
        double levelToDisableAt = percentage / 15.0;
        DecimalFormat dform = new DecimalFormat("#.##");
        String formattedLightLevel = dform.format(levelToDisableAt);
        int lightLevel = 0;
        int highestLevel = 0;
        for (BlockFace y : BlockFace.values()) {
            lightLevel = block.getRelative(y).getLightLevel();
            if (lightLevel > highestLevel) {
                highestLevel = lightLevel;
            }
            if (lightLevel > levelToDisableAt) {
                if (debug) {
                    log.info(FoundDiamonds.getDebugPrefix() + player.getName() + " just mined " + block.getType().name().toLowerCase().replaceAll("_", " ")
                        + " at light level " + highestLevel + ".  We are disabling ore mining at light level " + formattedLightLevel
                        + " or " + percentage + "%");
                }
                return false;
            }
        }
        if ((fd.getConfig().getBoolean(config.getLightLevelAdmin())) && (!fd.hasPerms(player, "fd.messages"))) {
            sendLightAdminMessage(player, block, highestLevel);
        }
        if (debug) {
            log.info(FoundDiamonds.getDebugPrefix() + player.getName() + " was denied mining "+ block.getType().name().toLowerCase().replaceAll("_", " ")
                    + " at light level " + highestLevel + ".  We are disabling ore mining at light level " + formattedLightLevel
                    + " or " + percentage + "%");
        }
        return true;
    }

    private boolean isTooDark(Player player, Block block, BlockBreakEvent event) {
        if (fd.getConfig().getBoolean(config.getDisableMiningAtLightLevel()) && blockSeesNoLight(player, block)) {
            event.setCancelled(true);
            player.sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + "Mining in the dark is dangerous, place a torch!");
            return true;
        }
        return false;
    }

}