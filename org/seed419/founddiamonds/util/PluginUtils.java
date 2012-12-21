package org.seed419.founddiamonds.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;

/*
Copyright 2011-2012 Blake Bartenbach

This file is part of FoundDiamonds.

FoundDiamonds is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

FoundDiamonds is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FoundDiamonds.  If not, see <http://www.gnu.org/licenses/>.
*/

public class PluginUtils {

    public static String getArgs1Plus(String[] args) {
        StringBuilder sb = new StringBuilder();
        args[0] = "";
        for (String x : args) {
            sb.append(x).append(" ");
        }
        return sb.toString().trim();
    }
    public static String getArgs2Plus(String[] args) {
        StringBuilder sb = new StringBuilder();
        args[0] = ""; args[1] = "";
        for (String x : args) {
            sb.append(x).append(" ");
        }
        return sb.toString().trim();
    }

    public static boolean isRedstone(Block m) {
        return (m.getType() == Material.REDSTONE_ORE || m.getType() == Material.GLOWING_REDSTONE_ORE);
    }

    public static boolean isRedstone(Material m) {
        return (m == Material.REDSTONE_ORE || m == Material.GLOWING_REDSTONE_ORE);
    }

    public static String customTranslateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] charArray = textToTranslate.toCharArray();
        for (int i = 0; i < charArray.length - 1; i++) {
            if (charArray[i] == altColorChar && "0123456789AaBbCcDdEeFfKkNnRrLlMmOo".indexOf(charArray[i+1]) > -1) {
                charArray[i] = ChatColor.COLOR_CHAR;
                charArray[i+1] = Character.toLowerCase(charArray[i+1]);
            }
        }
        return new String(charArray);
    }
}