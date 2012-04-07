/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seed419.FoundDiamonds;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author seed419
 */
public class Menus {


    //Toggle Menus
    public static void showToggle(CommandSender sender, String lightrequired) {
        sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + "[Toggle Options 1/" + FoundDiamonds.getTogglePages() + "]");
        sender.sendMessage(ChatColor.RED + "    diamond" + ChatColor.WHITE + " - Diamond broadcast");
        sender.sendMessage(ChatColor.RED + "    gold" + ChatColor.WHITE + " - Gold broadcast");
        sender.sendMessage(ChatColor.RED + "    lapis" + ChatColor.WHITE + " - Lapis broadcast");
        sender.sendMessage(ChatColor.RED + "    redstone" + ChatColor.WHITE + " - Redstone broadcast");
        sender.sendMessage(ChatColor.RED + "    iron" + ChatColor.WHITE + " - Iron broadcast");
        sender.sendMessage(ChatColor.RED + "    coal" + ChatColor.WHITE + " - Coal broadcast");
        sender.sendMessage(ChatColor.RED + "    mossy" + ChatColor.WHITE + " - Mossy broadcast");
        sender.sendMessage(ChatColor.RED + "    obby" + ChatColor.WHITE + " - Obsidian broadcast");
        sender.sendMessage(ChatColor.RED + "    darkness" + ChatColor.WHITE + " - Disable mining below " +
                ChatColor.YELLOW + lightrequired);
        sender.sendMessage("Type /fd toggle 2 to read the next page");
    }

    public static void showToggle2(CommandSender sender) {
        sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + "[Toggle Options 2/" + FoundDiamonds.getTogglePages() + "]");
        sender.sendMessage(ChatColor.RED + "    ops" + ChatColor.WHITE + " - OPs have all permissions");
        sender.sendMessage(ChatColor.RED + "    kick" + ChatColor.WHITE + " - Kick player on trap break");
        sender.sendMessage(ChatColor.RED + "    ban" + ChatColor.WHITE + " - Ban player on trap break");
        sender.sendMessage(ChatColor.RED + "    trapalerts" + ChatColor.WHITE + " - Send admin alerts on trap breaks");
        sender.sendMessage(ChatColor.RED + "    diamondadmin" + ChatColor.WHITE + " - Send admin messages on diamond breaks");
        sender.sendMessage(ChatColor.RED + "    logging" + ChatColor.WHITE + " - Log all diamond breaks to log.txt");
        sender.sendMessage(ChatColor.RED + "    usenicks" + ChatColor.WHITE + " - Use player nicknames in broadcasts");
        sender.sendMessage(ChatColor.RED + "    creative" + ChatColor.WHITE + " - Disable in creative gamemode");
        sender.sendMessage(ChatColor.RED + "    diamondadmin" + ChatColor.WHITE + " - Show admin messages for diamond");
        sender.sendMessage("Type /fd toggle 3 to read the next page");
    }

    public static void showToggle3(CommandSender sender) {
        sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + "[Toggle Options 3/" + FoundDiamonds.getTogglePages() + "]");
        sender.sendMessage(ChatColor.RED + "    goldadmin" + ChatColor.WHITE + " - Show admin messages for gold");
        sender.sendMessage(ChatColor.RED + "    redstoneadmin" + ChatColor.WHITE + " - Show admin messages for redstone");
        sender.sendMessage(ChatColor.RED + "    lapisadmin" + ChatColor.WHITE + " - Show admin messages for lapis");
        sender.sendMessage(ChatColor.RED + "    ironadmin" + ChatColor.WHITE + " - Show admin messages for iron");
        sender.sendMessage(ChatColor.RED + "    lightadmin" + ChatColor.WHITE + " - Show admin messages for light levels");
        sender.sendMessage(ChatColor.RED + "    prefix" + ChatColor.WHITE + " - Show the [FD] prefix in broadcasts");
        sender.sendMessage(ChatColor.RED + "    items" + ChatColor.WHITE + " - Random items for finding diamonds");
        sender.sendMessage(ChatColor.RED + "    spells" + ChatColor.WHITE + " - Random spells for finding diamonds");
        sender.sendMessage(ChatColor.RED + "    cleanlog" + ChatColor.WHITE + " - Clean log (all ore announcements)");
        sender.sendMessage("Type /fd toggle 4 to read the next page");
    }

    public static void showToggle4(CommandSender sender) {
        sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + "[Toggle Options 4/" + FoundDiamonds.getTogglePages() + "]");
        sender.sendMessage(ChatColor.RED + "    debug" + ChatColor.WHITE + " - Toggle debug output to the console");
    }




    //Config Menus


}
