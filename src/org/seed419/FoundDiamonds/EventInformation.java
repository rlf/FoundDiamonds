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
    private ChatColor color;
    private Material mat;
    private Location loc;
    private Block block;
    private BlockBreakEvent event;
    private Player player;
    private String formattedMatName;


    public EventInformation(Material mat, ChatColor color) {
        this.color = color;
        this.mat = mat;
        formattedMatName = mat.name().toLowerCase().replace("_", " ");
    }

    public ChatColor getColor() {
        return color;
    }

    public int getTotal() {
        return total;
    }

    public Material getMaterial() {
        return mat;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public Location getLocation() {
        return loc;
    }

    public void setLocation(Location loc) {
        this.loc = loc;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public BlockBreakEvent getEvent() {
        return event;
    }

    public void setEvent(BlockBreakEvent event) {
        this.event = event;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getMatName() {
        return formattedMatName;
    }

}
