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
public class BlockColor {


    private BlockColor() {

    }

    public static ChatColor getBlockColor(Material mat) {
        if (mat == Material.DIAMOND_ORE || mat == Material.DIAMOND_BLOCK) {
            return ChatColor.AQUA;
        } else if (mat == Material.REDSTONE_ORE || mat == Material.GLOWING_REDSTONE_ORE || mat == Material.NETHER_WARTS
                || mat == Material.REDSTONE_TORCH_ON || mat == Material.REDSTONE_LAMP_OFF) {
            return ChatColor.DARK_RED;
        } else if (mat == Material.MOSSY_COBBLESTONE || mat == Material.LEAVES || mat == Material.VINE || mat == Material.LONG_GRASS
                || mat == Material.WATER_LILY) {
            return ChatColor.DARK_GREEN;
        } else if (mat == Material.GOLD_ORE || mat == Material.PUMPKIN || mat == Material.JACK_O_LANTERN || mat == Material.GLOWSTONE
                || mat == Material.CROPS || mat == Material.GOLD_BLOCK) {
            return ChatColor.GOLD;
        } else if (mat == Material.IRON_ORE || mat == Material.CLAY_BRICK || mat == Material.CAULDRON || mat == Material.IRON_FENCE
                || mat == Material.STONE || mat == Material.SMOOTH_BRICK || mat == Material.COBBLESTONE || mat == Material.CLAY ||
                mat == Material.GRAVEL || mat == Material.DISPENSER || mat == Material.FURNACE || mat == Material.BURNING_FURNACE ||
                mat == Material.COBBLESTONE_STAIRS || mat == Material.IRON_DOOR_BLOCK || mat == Material.STONE_BUTTON) {
            return ChatColor.GRAY;
        } else if (mat == Material.LAPIS_ORE || mat == Material.LAPIS_BLOCK) {
            return ChatColor.BLUE;
        } else if (mat == Material.COAL_ORE || mat == Material.MOB_SPAWNER || mat == Material.BROWN_MUSHROOM
                || mat == Material.SOUL_SAND) {
            return ChatColor.DARK_GRAY;
        } else if (mat == Material.OBSIDIAN || mat == Material.MYCEL || mat == Material.PORTAL) {
            return ChatColor.DARK_PURPLE;
        } else if (mat == Material.MELON_BLOCK || mat == Material.SUGAR_CANE_BLOCK || mat == Material.CACTUS || mat == Material.GRASS
                || mat == Material.SAPLING) {
            return ChatColor.GREEN;
        } else if (mat == Material.BRICK || mat == Material.BRICK_STAIRS || mat == Material.RED_MUSHROOM || mat == Material.RED_ROSE
                || mat == Material.NETHERRACK || mat == Material.TNT) {
            return ChatColor.RED;
        } else if (mat == Material.SPONGE || mat == Material.YELLOW_FLOWER || mat == Material.SAND || mat == Material.SANDSTONE
                || mat == Material.TORCH) {
            return ChatColor.YELLOW;
        }
        return ChatColor.WHITE;
    }

}
