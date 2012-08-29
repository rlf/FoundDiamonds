package org.seed419.founddiamonds;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.util.PluginUtils;

import java.util.HashSet;
import java.util.Set;

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
        cycleHorizontalFaces(original.getType(), original, blocks);
        return blocks.size() >= 500 ? 500 : blocks.size();
    }

    private void cycleHorizontalFaces(Material mat, Block original, Set<Location> list) {
        if (list.size() >= 500) { return; }
        findLikeBlocks(horizontalFaces, original, mat, list);
        if (list.size() >= 500) { return; }
        Block upper = original.getRelative(BlockFace.UP);
        findLikeBlocks(upperFaces, upper, mat, list);
        if (list.size() >= 500) { return; }
        Block lower = original.getRelative(BlockFace.DOWN);
        findLikeBlocks(LowerFaces, lower, mat, list);
    }

    private void findLikeBlocks(BlockFace[] faces, Block passed, Material originalMaterial, Set<Location> alreadyAdded) {
        for (BlockFace y : faces) {
            Block var = passed.getRelative(y);
            if (var.getType() == originalMaterial && !alreadyAdded.contains(var.getLocation()) && isAnnounceable(var.getLocation())
                    || PluginUtils.isRedstone(var) && PluginUtils.isRedstone(originalMaterial) && isAnnounceable(var.getLocation())
                    && !alreadyAdded.contains(var.getLocation())) {
                counted.add(var.getLocation());
                alreadyAdded.add(var.getLocation());
                if (alreadyAdded.size() >= 500) { return; }
                cycleHorizontalFaces(originalMaterial, var, alreadyAdded);
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

    public void removeAnnouncedOrPlacedBlock(Location loc) {
        if (fd.getConfig().getBoolean(Config.mysqlEnabled)) {
            fd.getMySQL().removePlacedBlock(loc);
        } else if (fd.getBlockPlaceListener().getFlatFilePlacedBlocks().contains(loc)) {
            fd.getBlockPlaceListener().getFlatFilePlacedBlocks().remove(loc);
        } else {
            counted.remove(loc);
        }
    }
}
