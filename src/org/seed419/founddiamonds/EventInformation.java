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
import org.bukkit.event.block.BlockDamageEvent;
import org.seed419.founddiamonds.listeners.BlockBreakListener;
import org.seed419.founddiamonds.util.PluginUtils;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author seed419
 */
public class EventInformation {


    private final BlockFace[] horizFaces = {BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH,
            BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.DOWN,
            BlockFace.UP};
    private final BlockFace[] upperFaces = {BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH,
            BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.UP};
    private final BlockFace[] LowerFaces = {BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH,
            BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.DOWN};
    private BlockBreakListener bbl;
    private int total;
    private Node node;
    private Block block;
    private Player player;


    public EventInformation(BlockBreakListener bbl, BlockBreakEvent event, Node node, boolean total) {
        this.bbl = bbl;
        this.block = event.getBlock();
        if (total) {this.total =  getTotalBlocks(this.block);}
        this.node = node;
        this.player = event.getPlayer();
    }

    public EventInformation(BlockDamageEvent bde, Node node, boolean total) {
        this.block = bde.getBlock();
        if (total) {this.total =  getTotalBlocks(this.block);}
        this.node = node;
        this.player = bde.getPlayer();
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

    public Player getPlayer() {
        return player;
    }

    private int getTotalBlocks(Block original) {
        HashSet<Location> blocks = new HashSet<Location>();
        blocks.add(original.getLocation());
        cycleHorizontalFaces(original.getType(), original, blocks);
        return blocks.size() >= 500 ? 500 : blocks.size();
    }

    private void cycleHorizontalFaces(Material mat, Block original, Set<Location> list) {
        if (list.size() >= 500) { return; }
        findLikeBlocks(horizFaces, original, mat, list);
        if (list.size() >= 500) { return; }
        Block upper = original.getRelative(BlockFace.UP);
        //System.out.println("CHECKING UPPER BLOCKS");
        findLikeBlocks(upperFaces, upper, mat, list);
        if (list.size() >= 500) { return; }
        Block lower = original.getRelative(BlockFace.DOWN);
        //System.out.println("CHECKING LOWER BLOCKS");
        findLikeBlocks(LowerFaces, lower, mat, list);
    }

    private void findLikeBlocks(BlockFace[] faces, Block passed, Material originalMaterial, Set<Location> alreadyAdded) {
        //System.out.println("Passed Block @ X:" + passed.getX() + " Y:" + passed.getY() + " Z:" + passed.getZ());
        for (BlockFace y : faces) {
            Block var = passed.getRelative(y);
            //System.out.println("X:" + var.getX() + " Y:" + var.getY() + " Z:" + var.getZ() + " Type: " + Format.material(var.getType()) + " Face: " + y.name());
            //TODO This checks the same location a few times...
            //@TODO SEEMS FIXED - confirmed?
            //System.out.println("Checking relative blocks...");
            if (var.getType() == originalMaterial && !alreadyAdded.contains(var.getLocation()) && bbl.isAnnounceable(var.getLocation())
                    || PluginUtils.isRedstone(var) && PluginUtils.isRedstone(originalMaterial) && bbl.isAnnounceable(var.getLocation())
                    && !alreadyAdded.contains(var.getLocation())) {
                bbl.getCantAnnounce().add(var.getLocation());
                alreadyAdded.add(var.getLocation());
                if (alreadyAdded.size() >= 500) { return; }
                cycleHorizontalFaces(originalMaterial, var, alreadyAdded);
            }
        }
    }

    /*    private void cycleHorizontalFaces(Material mat, Block original, Set<Block> list) {
        findLikeBlocks(horizFaces, original, mat, list);
        if (list.size() >= 500) { return; }
        Block upper = original.getRelative(BlockFace.UP);
        System.out.println("CHECKING UPPER BLOCKS");
        findLikeBlocks(horizFaces, upper, mat, list);
        if (list.size() >= 500) { return; }
        Block lower = original.getRelative(BlockFace.DOWN);
        System.out.println("CHECKING LOWER BLOCKS");
        findLikeBlocks(horizFaces, lower, mat, list);
    }

    private void findLikeBlocks(BlockFace[] faces, Block passed, Material mat, Set<Block> alreadyAdded) {
        System.out.println("Passed Block @ X:" + passed.getX() + " Y:" + passed.getY() + " Z:" + passed.getZ());
        for (BlockFace y : faces) {
            Block var = passed.getRelative(y);
            System.out.println("X:" + var.getX() + " Y:" + var.getY() + " Z:" + var.getZ() + " Type: " + Format.material(var.getType()) + " Face: " + y.name());
            //ATTEMPTED FIX = needs testing.  FFS I hope this works...
            //TODO This checks the same location a few times...
            System.out.println("Checking relative blocks...");
            if (var.getType() == mat && !alreadyAdded.contains(var) && bbl.isAnnounceable(var.getLocation())
                    || PluginUtils.isRedstone(var) && PluginUtils.isRedstone(mat) && bbl.isAnnounceable(var.getLocation())
                    && !alreadyAdded.contains(var)) {

                bbl.getCantAnnounce().add(var.getLocation());
                alreadyAdded.add(var);

                if (alreadyAdded.size() >= 500) { return; }
                cycleHorizontalFaces(mat, var, alreadyAdded);
            }
        }
    }*/
}
