/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seed419.FoundDiamonds;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

/**
 *
 * @author seed419
 */
public class EventInformation {


    private int total;
    private Node node;
    private Location blockLoc;
    private Block block;
    private BlockBreakEvent event;
    private Player player;


    public EventInformation(BlockBreakEvent event, Node node) {
        this.blockLoc = event.getBlock().getLocation();
        this.block = event.getBlock();
        this.node = node;
    }

    public ChatColor getColor() {
        return node.getColor();
    }

    //TODO wat...
    public int getTotal() {
        return total;
    }

    public Material getMaterial() {
        return node.getMaterial();
    }

    public Location getBlockLocation() {
        return blockLoc;
    }

    public Block getBlock() {
        return block;
    }

    //TODO do we need to get the event?  Or should event-related functions take place here?
    public BlockBreakEvent getEvent() {
        return event;
    }

    public Player getPlayer() {
        return player;
    }

}
