/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seed419.founddiamonds;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.seed419.founddiamonds.listeners.BlockListener;

import java.util.HashSet;

/**
 *
 * @author seed419
 */
public class EventInformation {


    private BlockListener bl;
    private int total;
    private HashSet<Location> checkedLocations;
    private Node node;
    private Block block;
    private BlockBreakEvent event;
    private Player player;


    public EventInformation(BlockListener bl, BlockBreakEvent event, Node node) {
        this.total =  getTotalBlocks(event.getBlock());
        this.block = event.getBlock();
        this.node = node;
        this.player = event.getPlayer();
        this.event = event;
        this.bl = bl;
    }

    public ChatColor getColor() {
        return node.getColor();
    }

    public int getTotal() {
        return total;
    }

    public Material getMaterial() {
        return node.getMaterial();
    }

    public Block getBlock() {
        return block;
    }

    public BlockBreakEvent getEvent() {
        return event;
    }

    public Player getPlayer() {
        return player;
    }

    private int getTotalBlocks(Block origBlock) {
        this.total = 0;
        checkedLocations = new HashSet<Location>();
        checkedLocations.add(origBlock.getLocation());
        for (BlockFace y : BlockFace.values()) {
            Block check = origBlock.getRelative(y);
            Location checkLoc = check.getLocation();
            if ((check.getType() == origBlock.getType() && !checkedLocations.contains(checkLoc) && bl.isAnnounceable(checkLoc)) ||
                    ((FoundDiamonds.isRedstone(origBlock) && FoundDiamonds.isRedstone(check)) && !checkedLocations.contains(checkLoc) && bl.isAnnounceable(checkLoc))) {
                bl.getCantAnnounce().add(checkLoc);
                total++;
                checkedLocations.add(checkLoc);
                //findLikeBlocks(origBlock, check);
                if (total >= 1000) {
                    return 1000;
                }
            } else {
                if (!checkedLocations.contains(checkLoc)) {
                    checkedLocations.add(checkLoc);
                }
            }
        }
        return total;
    }

/*    private void findLikeBlocks(Block origBlock, Block cycle) {
        for (BlockFace y : BlockFace.values()) {
            Block nextCycle = cycle.getRelative(y);
            Location nextLoc = nextCycle.getLocation();
            if  ((nextCycle.getType() == origBlock.getType() && !checkedLocations.contains(nextLoc) && !totalBlocks.contains(nextLoc) && bl.isAnnounceable(nextLoc)) ||
                    ((FoundDiamonds.isRedstone(origBlock) && FoundDiamonds.isRedstone(nextCycle)) && !checkedLocations.contains(nextCycle) && !totalBlocks.contains(nextCycle) && bl.isAnnounceable(nextCycle))) {
                blockList.add(secondCycle);
                FoundDiamonds.getAnnouncedBlocks().add(secondCycle.getLocation());
                //System.out.println("Total+=" + secondCycle.getType().name() + " X: "+ secondCycle.getX()
                // + " Y:" + secondCycle.getY() + " Z:" + secondCycle.getZ());
                if (blockList.size() >= 1000) {
                    return;
                }
                findLikeBlocks(origBlock, secondCycle);
            } else {
                if (!checkedBlocks.contains(secondCycle)) {
                    checkedBlocks.add(secondCycle);
                }
            }
        }
    }*/

}
