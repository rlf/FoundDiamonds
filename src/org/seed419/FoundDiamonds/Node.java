package org.seed419.FoundDiamonds;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: seed419
 * Date: 4/13/12
 * Time: 12:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class Node {


    private Material mat;
    private ChatColor color;


    public Node(Material mat, ChatColor color) {
        this.mat = mat;
        this.color = color;
    }

    public ChatColor getColor() {
        return color;
    }

    public Material getMaterial() {
        return mat;
    }

    public static Node parseConfigBlock(String cb) {
        String[] bi = cb.toString().split(":");
        for (String x : bi) {
             System.out.println("bi: " + x);
        }
        //Parse the material string
        Material matchAttempt = parseMaterial(bi[0]);
        if (matchAttempt == null) {
            return null;
        }
        if (matchAttempt.isBlock()) {
            ChatColor color = null;
            if (bi.length == 1) {
                // No color specified - attempt to get default
                color = BlockColor.getBlockColor(matchAttempt);
            } else if (bi.length == 2) {
                // parse the color
                color = parseChatColor(bi[1]);
                if (color == null) {
                    return null;
                }
            }
            return new Node(matchAttempt, color);
        }
        return null;
    }


    public static Material parseMaterial(String matName) {
        Material matchAttempt;
        try {
            int id = Integer.parseInt(matName);
            matchAttempt = Material.getMaterial(id);
        } catch (NumberFormatException ex) {
            matchAttempt = Material.matchMaterial(matName);
        } catch (Exception ex) {
            return null;
        }
        return matchAttempt;
    }

    public static ChatColor parseChatColor(String colorName) {
        ChatColor color;
        String re = colorName.replace(" ","_").toUpperCase();
/*        int unneededUnderscore = re.lastIndexOf("_");
        StringBuilder sb2 = new StringBuilder(re);
        sb2.deleteCharAt(unneededUnderscore);
        String colorString = sb2.toString();*/
        try {
            color = ChatColor.valueOf(re);
            return color;
        } catch (IllegalArgumentException ex) {
             return null;
        }
    }

    @Override
    public String toString() {
        return Format.material(this.getMaterial()) + ":" + Format.chatColor(this.getColor());
    }
}
