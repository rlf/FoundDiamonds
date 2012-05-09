/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seed419.FoundDiamonds;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.LinkedList;

/**
 *
 * @author seed419
 */
public class EventInformation {


    private int total;
    private LinkedList<Block> blockList;
    private LinkedList<Block> checkedBlocks;
    private Node node;
/*    private Location blockLoc;*/
    private Block block;
    private BlockBreakEvent event;
    private Player player;


    public EventInformation(BlockBreakEvent event, Node node) {
        //this.blockLoc = event.getBlock().getLocation();
        this.total =  getTotalBlocks(event.getBlock());
        this.block = event.getBlock();
        this.node = node;
        this.player = event.getPlayer();
        this.event = event;
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

/*    public Location getBlockLocation() {
        return blockLoc;
    }*/

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
        blockList = new LinkedList<Block>();
        checkedBlocks = new LinkedList<Block>();
        blockList.add(origBlock);
        for (BlockFace y : BlockFace.values()) {
            Block cycle = origBlock.getRelative(y);
            if ((cycle.getType() == origBlock.getType() && !blockList.contains(cycle)
                    && !checkedBlocks.contains(cycle) && !wasPlaced(cycle)) ||
                    ((FoundDiamonds.isRedstone(origBlock) && FoundDiamonds.isRedstone(cycle)) &&
                            !blockList.contains(cycle) && !checkedBlocks.contains(cycle) && !wasPlaced(cycle))) {
                FoundDiamonds.getAnnouncedBlocks().add(cycle.getLocation());
                //System.out.println("Total+=" + cycle.getType().name() + " X: "+ cycle.getX()
                // + " Y:" + cycle.getY() + " Z:" + cycle.getZ());
                blockList.add(cycle);
                checkCyclesRelative(origBlock, cycle);
                if (blockList.size() >= 1000) {
                    return 1000;
                }
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
            if ((secondCycle.getType() == origBlock.getType() && !blockList.contains(secondCycle)
                    && !checkedBlocks.contains(secondCycle) && !wasPlaced(secondCycle)) ||
                    (FoundDiamonds.isRedstone(origBlock) && FoundDiamonds.isRedstone(secondCycle)
                            && (!blockList.contains(secondCycle) && !checkedBlocks.contains(secondCycle)
                            && !wasPlaced(secondCycle)))) {
                blockList.add(secondCycle);
                FoundDiamonds.getAnnouncedBlocks().add(secondCycle.getLocation());
                //System.out.println("Total+=" + secondCycle.getType().name() + " X: "+ secondCycle.getX()
                // + " Y:" + secondCycle.getY() + " Z:" + secondCycle.getZ());
                if (blockList.size() >= 1000) {
                    return;
                }
                checkCyclesRelative(origBlock, secondCycle);
            } else {
                if (!checkedBlocks.contains(secondCycle)) {
                    checkedBlocks.add(secondCycle);
                }
            }
        }
    }

    private boolean wasPlaced(Block block) {
        return (FoundDiamonds.getPlacedBlocks().contains(block.getLocation()));
    }

}
