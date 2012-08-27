package org.seed419.founddiamonds;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.seed419.founddiamonds.util.BlockColor;
import org.seed419.founddiamonds.util.Format;

import java.util.List;


public class Node {


    private ItemStack item;
    private ChatColor color;


    public Node(Material mat, ChatColor color) {
        this.item = new ItemStack(mat);
        this.color = color;
    }
    
    public Node(ItemStack stack, ChatColor color) {
        this.item = stack;
        this.color = color;
    }

    public ChatColor getColor() {
        return color;
    }

    public Material getMaterial() {
        return item.getType();
    }

    public static Node parseNode(String cb) {
        String[] bi = cb.split(",");
        ItemStack material = parseItemStack(bi[0]);
        if (material == null) {
            System.out.println("Unable to match material in FoundDiamonds Config.");
            System.out.println("If you just updated, delete your config.");
            return null;
        }
        if (bi.length == 1 ) {
            return new Node(material, BlockColor.getBlockColor(material.getType()));
        } else {
            ChatColor color = parseChatColor(bi[1]);
            if (color == null) {
                System.out.println("Unrecognized color: " + bi[1]);
                return null;
            }
            return new Node(material, color);
        }
    }


    public static ItemStack parseItemStack(String material) {
        Material matchMaterial = Material.matchMaterial(material);
        if (matchMaterial != null) {return new ItemStack(matchMaterial);}
        String[] split = material.split(":");
        if (split.length == 1) {
            try {
                return new ItemStack(Integer.parseInt(split[0]));
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                return null;
            }
        } else {
            try {
                int id = Integer.parseInt(split[0]);
                short damage = (short) Integer.parseInt(split[1]);
                return new ItemStack(id, 1, damage);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    public static ChatColor parseChatColor(String colorName) {
        String re = colorName.replace(" ","_").toUpperCase();
        try {
            return ChatColor.valueOf(re);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static boolean containsMat(List<Node> list, Material mat) {
        for (Node x : list) {
            if (x.getMaterial() == mat) {
                return true;
            }
        }
        return false;
    }

/*    public static boolean containsColor(List<Node> list, ChatColor color) {
        for (Node x : list) {
            if (x.getColor() == color) {
                return true;
            }
        }
        return false;
    }*/

    public static Node getNodeByMaterial(List<Node> list, Material mat) {
        for (Node x : list) {
            if (x.getMaterial() == mat) {
                return x;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return Format.material(this.getMaterial()) + ":" + Format.chatColor(this.getColor());
    }
}
