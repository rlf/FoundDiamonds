package org.seed419.founddiamonds;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.util.PluginUtils;

import java.util.HashSet;
import java.util.Set;
/**
 * Attribute Only (Public) License
 * Version 0.a3, July 11, 2011
 *
 * Copyright (C) 2012 Blake Bartenbach <seed419@gmail.com> (@seed419)
 *
 * Anyone is allowed to copy and distribute verbatim or modified
 * copies of this license document and altering is allowed as long
 * as you attribute the author(s) of this license document / files.
 *
 * ATTRIBUTE ONLY PUBLIC LICENSE
 * TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 * 1. Attribute anyone attached to the license document.
 * Do not remove pre-existing attributes.
 *
 * Plausible attribution methods:
 * 1. Through comment blocks.
 * 2. Referencing on a site, wiki, or about page.
 *
 * 2. Do whatever you want as long as you don't invalidate 1.
 *
 * @license AOL v.a3 <http://aol.nexua.org>
 */
public class BlockCounter {


    private FoundDiamonds fd;
    private HashSet<Location> counted = new HashSet<Location>();
    private final BlockFace[] horizontalFaces = {BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH,
            BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.DOWN,
            BlockFace.UP};
    private final BlockFace[] upperFaces = {BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH,
            BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.UP};
    private final BlockFace[] LowerFaces = {BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH,
            BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.DOWN};


    public BlockCounter(FoundDiamonds fd) {
        this.fd = fd;
    }

    public int getTotalBlocks(Block original) {
        HashSet<Location> blocks = new HashSet<Location>();
        blocks.add(original.getLocation());
        cycleHorizontalFaces(original.getType(), original, blocks, true);
        return blocks.size() >= 500 ? 500 : blocks.size();
    }

    public HashSet<Location> getAllLikeBlockLocations(Block original) {
        HashSet<Location> blocks = new HashSet<Location>();
        blocks.add(original.getLocation());
        cycleHorizontalFaces(original.getType(), original, blocks, false);
        return blocks;
    }

    private void cycleHorizontalFaces(Material mat, Block original, Set<Location> blocks, boolean counting) {
        if (blocks.size() >= 500) { return; }
        findLikeBlocks(horizontalFaces, original, mat, blocks, counting);
        if (blocks.size() >= 500) { return; }
        Block upper = original.getRelative(BlockFace.UP);
        findLikeBlocks(upperFaces, upper, mat, blocks, counting);
        if (blocks.size() >= 500) { return; }
        Block lower = original.getRelative(BlockFace.DOWN);
        findLikeBlocks(LowerFaces, lower, mat, blocks, counting);
    }

    private void findLikeBlocks(BlockFace[] faces, Block passed, Material originalMaterial, Set<Location> blocks, boolean counting) {
        for (BlockFace y : faces) {
            Block var = passed.getRelative(y);
            if (var.getType() == originalMaterial && !blocks.contains(var.getLocation()) && isAnnounceable(var.getLocation())
                    || PluginUtils.isRedstone(var) && PluginUtils.isRedstone(originalMaterial) && isAnnounceable(var.getLocation())
                    && !blocks.contains(var.getLocation())) {
                if (counting) {
                    counted.add(var.getLocation());
                }
                blocks.add(var.getLocation());
                if (blocks.size() >= 500) { return; }
                cycleHorizontalFaces(originalMaterial, var, blocks, counting);
            }
        }
    }

    public boolean wasCounted(Location loc) {
        return counted.contains(loc);
    }

    public boolean isAnnounceable(Location loc) {
        if (fd.getConfig().getBoolean(Config.mysqlEnabled)) {
            return !fd.getMySQL().blockWasPlaced(loc) && !wasCounted(loc);
        } else {
            return !wasCounted(loc) && !fd.getBlockPlaceListener().getFlatFilePlacedBlocks().contains(loc);
        }
    }

    public void removeAnnouncedOrPlacedBlock(final Location loc) {
        if (fd.getConfig().getBoolean(Config.mysqlEnabled)) {
            fd.getMySQL().removePlacedBlock(loc);
        } else if (fd.getBlockPlaceListener().getFlatFilePlacedBlocks().contains(loc)) {
            fd.getBlockPlaceListener().getFlatFilePlacedBlocks().remove(loc);
        }
        if (counted.contains(loc)) {
            counted.remove(loc);
        }
    }
}
