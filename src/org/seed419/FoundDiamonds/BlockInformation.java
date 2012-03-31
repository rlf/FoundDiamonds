/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seed419.FoundDiamonds;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 *
 * @author seed419
 */
public class BlockInformation {


    private int total;
    private ChatColor color;
    private Material mat;


    public BlockInformation(int total, ChatColor color, Material mat) {
        this.total = total;
        this.color = color;
        this.mat = mat;
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

}
