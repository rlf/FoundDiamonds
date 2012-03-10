package me.itsatacoshop247.FoundDiamonds;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (fd.getPlacedBlocks().contains(block.getLocation())) {
            fd.getPlacedBlocks().remove(block.getLocation());
            return;
        }
        if (isTrapBlock(block)) {
        handleTrapBlock(player, block);
        return;
        }
        if (block.getType() == Material.DIAMOND_ORE) {
            if (fd.getConfig().getBoolean(config.getLogDiamondBreaks())) {
                handleLogging(player, block, false);
            }
            if (fd.getConfig().getBoolean(config.getDiamondAdmin())) {
                sendAdminMessage(player);
            }

        }
        if (monitoredMaterial(block)) {
            if (fd.getAnnouncedBlocks().contains(block.getLocation())) {
                fd.getAnnouncedBlocks().remove(block.getLocation());
                return;
            }
            if (fd.getEnabledWorlds().contains(player.getWorld().getName())) {
                if (player.getGameMode() == GameMode.CREATIVE && fd.getConfig().getBoolean(config.getDisableInCreative())) {
                    return;
                }
                if (fd.getConfig().getBoolean(config.getDisableMiningInTotalDarkness()) && blockSeesNoLight(block)) {
                    event.setCancelled(true);
                    player.sendMessage(prefix + ChatColor.RED + "Mining in total darkness is dangerous, place a torch!");
                    return;
                }
                String playername;
                if (fd.getConfig().getBoolean(config.getUseNick())) {
                    playername = event.getPlayer().getDisplayName();
                } else {
                    playername = event.getPlayer().getName();
                }
                String blockname = block.getType().toString().toLowerCase().replace("_", " ");
                handleBlock(player, block, playername, blockname);
            }
        }
    }

    public void sendAdminMessage(Player player) {
        for (Player x : fd.getServer().getOnlinePlayers()) {
            if (fd.getAdminMessageMap().containsKey(x)) {
                if (fd.getAdminMessageMap().get(x)) {
                    x.sendMessage(adminPrefix + ChatColor.YELLOW + player.getName() + ChatColor.WHITE
                            + " just broke a diamond block.");
                }
            }
        }
    }

    public int getRandomAmount(){
        Random rand = new Random();
        int amount = rand.nextInt(3);
        return amount;
    }

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
        if (Material.getMaterial(item) == Material.COAL) {
            fd.getServer().broadcastMessage(prefix + "Everyone else got some "
                    + ChatColor.GRAY + Material.getMaterial(item).name().toLowerCase().replace("_", " ") + "!");
        } else {
            fd.getServer().broadcastMessage(prefix + "Everyone else got some "
                    + ChatColor.GRAY + Material.getMaterial(item).name().toLowerCase().replace("_", " ") + "s!");
        }
    }

    @SuppressWarnings("deprecation")
    private void giveItems(int item, int amount) {
        for(Player p: fd.getServer().getOnlinePlayers()) {
            p.getInventory().addItem(new ItemStack(item, amount));
            p.updateInventory();
        }
    }

    private boolean monitoredMaterial(Block block) {
        return fd.getEnabledBlocks().contains(block.getType());
    }

    private void handleBlock(Player player, Block block, String playername, String blockname) {
        Material blockMaterial = block.getType();
        String total = String.valueOf(getTotalBlocks(block));
        if (blockMaterial == Material.DIAMOND_ORE && fd.getConfig().getBoolean(config.getBcDiamond())) {
            broadcastFoundBlock(blockMaterial, ChatColor.AQUA, total, playername, blockname);
            if (fd.getConfig().getBoolean(config.getAwardsForFindingDiamonds())) {
                int randomInt = (int) (Math.random()*100);
                if (randomInt <= fd.getConfig().getInt(config.getPercentTogetAwards())) {
                    int randomNumber = (int)(Math.random()*150);
                    if (randomNumber >= 0 && randomNumber <= 150) {
                        handleRandomItems(randomNumber);
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

    private void broadcastFoundBlock(Material mat, ChatColor color, String total, String name, String block) {
        if (mat == Material.GLOWING_REDSTONE_ORE || mat == Material.REDSTONE_ORE) {
            if (Integer.parseInt(total) > 1) {
                fd.getServer().broadcastMessage(prefix + color +
                        fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name
                        + color).replace("@Number@", total).replace("@BlockName@", "redstone ores"));
            } else {
                fd.getServer().broadcastMessage(prefix + color +
                        fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name
                        + color).replace("@Number@", total).replace("@BlockName@", "redstone ore"));
            }
        } else if (mat == Material.OBSIDIAN) {
                fd.getServer().broadcastMessage(prefix + color +
                        fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name
                        + color).replace("@Number@", total).replace("@BlockName@", "obsidian"));
        }else {
            if (Integer.parseInt(total) > 1) {
                fd.getServer().broadcastMessage(prefix + color +
                        fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name
                        + color).replace("@Number@", total).replace("@BlockName@", block + "s"));
            } else {
                fd.getServer().broadcastMessage(prefix + color +
                        fd.getConfig().getString(config.getBcMessage()).replace("@Player@", name
                        + color).replace("@Number@", total).replace("@BlockName@", block));
            }
        }
    }


    private boolean blockSeesNoLight(Block block) {
        for (BlockFace y : BlockFace.values()) {
            if (block.getRelative(y).getLightLevel() != 0) {
                return false;
            }
        }
        return true;
    }

    private int getTotalBlocks(Block origBlock) {
        blockList = new LinkedList<Block>();
        checkedBlocks = new LinkedList<Block>();
        blockList.add(origBlock);
        for (BlockFace y : BlockFace.values()) {
            Block cycle = origBlock.getRelative(y);
            if ((cycle.getType() == origBlock.getType() && !blockList.contains(cycle) && !checkedBlocks.contains(cycle)) ||
                    ((isRedstone(origBlock) && isRedstone(cycle)) &&
                    !blockList.contains(cycle) && !checkedBlocks.contains(cycle))) {
                fd.getAnnouncedBlocks().add(cycle.getLocation());
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
            if ((secondCycle.getType() == origBlock.getType() && !blockList.contains(secondCycle) && !checkedBlocks.contains(secondCycle)) ||
               (isRedstone(origBlock) && isRedstone(secondCycle) && (!blockList.contains(secondCycle) && !checkedBlocks.contains(secondCycle)))) {
                blockList.add(secondCycle);
                fd.getAnnouncedBlocks().add(secondCycle.getLocation());
                checkCyclesRelative(origBlock, secondCycle);
            } else {
                if (!checkedBlocks.contains(secondCycle)) {
                    checkedBlocks.add(secondCycle);
                }
            }
        }
    }


    public String getPrefix() {
        return prefix;
    }

    private boolean isRedstone(Block m) {
        return (m.getType() == Material.REDSTONE_ORE || m.getType() == Material.GLOWING_REDSTONE_ORE);
    }

 }


