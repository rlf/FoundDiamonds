/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seed419.founddiamonds.handlers;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.Permissions;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author seed419
 */
public class TrapHandler {


    private FoundDiamonds fd;
    private LoggingHandler logging;
    private final Set<Location> trapBlocks = new HashSet<Location>();



    public TrapHandler(FoundDiamonds fd, LoggingHandler logging) {
        this.fd = fd;
        this.logging = logging;
    }


    public void handleTrap(Player player, String[] args) {
        Location playerLoc = player.getLocation();
        Material trap;
        String item;
        int depth=0;
        if (args.length == 1) {
            trap = Material.DIAMOND_ORE;
            item = "Diamond ore";
        } else if (args.length == 2) {	//either trap block specified, old format, or depth specified, assuming diamond blocks
            item = args[1];
            trap = Material.matchMaterial(item);
            if(trap==null) {
            	try {
            		depth = Integer.parseInt(args[1]);
            	}catch(NumberFormatException ex) {
            		player.sendMessage(ChatColor.RED + "Please specifiy a valid number as depth");
            		return;
            	}
            	item = "Diamond ore";
            	trap = Material.DIAMOND_ORE;
            }
        } else if (args.length == 3) {	//either new block format specification, or depth + old block formatting
            item = args[1] + "_" + args[2];
            trap = Material.matchMaterial(item);
            if(trap == null) {
            	try {
            		depth = Integer.parseInt(args[2]);
            	}catch(NumberFormatException ex) {
               		player.sendMessage(ChatColor.RED + "Please specifiy a valid number as depth");
            		return;
            	}
            	item = args[1];
            	trap = Material.matchMaterial(item);
            }
        }else if(args.length == 4) {	//new block format + depth
            item = args[1] + "_" + args[2];
            trap = Material.matchMaterial(item);
            try {
        		depth = Integer.parseInt(args[3]);
        	}catch(NumberFormatException ex) {
           		player.sendMessage(ChatColor.RED + "Please specifiy a valid number as depth");
        		return;
        	}
        }
        	else {
            player.sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " Invalid number of arguments");
            player.sendMessage(ChatColor.RED + "Is it a block and a valid item? Try /fd trap gold ore");
            return;
        }
        if (trap != null && trap.isBlock()) {
            getTrapLocations(player, playerLoc, trap, depth);
        } else {
            player.sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " Unable to set a trap with '" + item + "'");
            player.sendMessage(ChatColor.RED + "Is it a block and a valid item? Try /fd trap gold ore");
        }
    }

    private void getTrapLocations(Player player, Location playerLoc, Material trap, int depth) {
        int x = playerLoc.getBlockX();
        int y = playerLoc.getBlockY() - depth;
        int maxHeight = player.getWorld().getMaxHeight();
        if ((y - 2) < 0) {
            player.sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " I can't place a trap down there, sorry.");
            return;
        } else if ((y - 1) > maxHeight) {
            player.sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " I can't place a trap this high, sorry.");
            return;
        }
        int z = playerLoc.getBlockZ();
        World world = player.getWorld();
        if (trap == Material.EMERALD_ORE) {
            Block block = world.getBlockAt(x, (y-1), z);
            setEmeraldTrap(player, block);
            return;
        }
        int randomnumber = (int)(Math.random() * 100);
        if ((randomnumber >= 0) && randomnumber < 50) {
            Block block1 = world.getBlockAt(x, y - 1, z);
            Block block2 = world.getBlockAt(x, y - 2, z + 1);
            Block block3 = world.getBlockAt(x - 1, y - 2, z);
            Block block4 = world.getBlockAt(x, y - 2, z);
            handleTrapBlocks(player, trap, block1, block2, block3, block4);
        } else if (randomnumber >= 50) {
            Block block1 = world.getBlockAt(x, y - 1, z);
            Block block2 = world.getBlockAt(x - 1, y - 2, z);
            Block block3 = world.getBlockAt(x , y - 2, z);
            Block block4 = world.getBlockAt(x -1, y - 1, z);
            handleTrapBlocks(player, trap, block1, block2, block3, block4);
        }
    }

    private void setEmeraldTrap(Player player, Block block) {
        trapBlocks.add(block.getLocation());
        block.setType(Material.EMERALD_ORE);
        player.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + " TrapHandler set using " + Material.EMERALD_ORE.name().toLowerCase().replace("_", " "));
    }

    private void handleTrapBlocks(Player player, Material trap, Block block1, Block block2, Block block3, Block block4) {
        trapBlocks.add(block1.getLocation());
        trapBlocks.add(block2.getLocation());
        trapBlocks.add(block3.getLocation());
        trapBlocks.add(block4.getLocation());
        block1.setType(trap);
        block2.setType(trap);
        block3.setType(trap);
        block4.setType(trap);
        player.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + " TrapHandler set using " + trap.name().toLowerCase().replace("_", " "));
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public Set<Location> getTrapBlocks() {
        return trapBlocks;
    }

    public boolean checkForTrapBlock(BlockBreakEvent event) {
        if (getTrapBlocks().contains(event.getBlock().getLocation())) {
            handleTrapBlock(event.getPlayer(), event.getBlock(), event);
            return true;
        }
        return false;
    }

    private void removeTrapBlock(Block block) {
        getTrapBlocks().remove(block.getLocation());
    }

    private void handleTrapBlock(Player player, Block block, BlockBreakEvent event) {
        if(fd.getConfig().getBoolean(Config.adminAlertsOnAllTrapBreaks)) {
            for (Player x: fd.getServer().getOnlinePlayers()) {
                if(Permissions.hasPerms(x, "fd.admin") && (x != player)) {
                    x.sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " " + player.getName()
                            + " just broke a trap block");
                }
            }
        }
        if (Permissions.hasPerms(player, "fd.trap")) {
            player.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + " TrapHandler block removed");
            event.setCancelled(true);
            block.setType(Material.AIR);
            removeTrapBlock(block);
        } else {
            fd.getServer().broadcastMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " " +  player.getName()
                    + " just broke a trap block");
            event.setCancelled(true);
            boolean banned = false;
            boolean kicked = false;
            if (fd.getConfig().getBoolean(Config.kickOnTrapBreak)) {
                player.kickPlayer(fd.getConfig().getString(Config.kickMessage));
                kicked = true;
            }
            if (fd.getConfig().getBoolean(Config.banOnTrapBreak)) {
                player.setBanned(true);
                banned = true;
            }
            if((fd.getConfig().getBoolean(Config.logTrapBreaks))) {
                logging.handleLogging(player, block, true, kicked, banned);
            }
        }
    }

}
