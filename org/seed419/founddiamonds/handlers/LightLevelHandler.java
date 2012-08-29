package org.seed419.founddiamonds.handlers;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.Node;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.util.Format;
import org.seed419.founddiamonds.util.Prefix;

/**
 * Attribute Only (Public) License
 * Version 0.a3, July 11, 2011
 * <p/>
 * Copyright (C) 2012 Blake Bartenbach <seed419@gmail.com> (@seed419)
 * <p/>
 * Anyone is allowed to copy and distribute verbatim or modified
 * copies of this license document and altering is allowed as long
 * as you attribute the author(s) of this license document / files.
 * <p/>
 * ATTRIBUTE ONLY PUBLIC LICENSE
 * TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 * <p/>
 * 1. Attribute anyone attached to the license document.
 * Do not remove pre-existing attributes.
 * <p/>
 * Plausible attribution methods:
 * 1. Through comment blocks.
 * 2. Referencing on a site, wiki, or about page.
 * <p/>
 * 2. Do whatever you want as long as you don't invalidate 1.
 *
 * @license AOL v.a3 <http://aol.nexua.org>
 */
public class LightLevelHandler {


    private FoundDiamonds fd;


    public LightLevelHandler(FoundDiamonds fd) {
        this.fd = fd;
    }


    public void handleLightLevelMonitor(final BlockDamageEvent event, final Node node, final Player player) {
        final Block block = event.getBlock();
        if (blockSeesNoLight(block)) {
            //This gives the potential x-rayer an idea that he's being watched...bad idea?
            //player.sendMessage(ChatColor.RED + "Mining in the dark is dangerous, place a torch!");
            //event.setCancelled(true);
            if (fd.getConfig().getBoolean(Config.lightLevelAdminMessages)) {
                sendLightAdminMessage(player, node);
            }
            if ((fd.getConfig().getBoolean(Config.logLightLevelViolations))) {
                fd.getLoggingHandler().logLightLevelViolation(node, player);
            }
        }

    }

    public void sendLightAdminMessage(final Player player, final Node node) {
        String lightAdminMessage = Prefix.getAdminPrefix() + " " + ChatColor.YELLOW + player.getName() +
                ChatColor.GRAY +" was mining " + node.getColor() +
                Format.getFormattedName(node.getMaterial(), 1) + ChatColor.GRAY + " below "
                + ChatColor.WHITE + fd.getConfig().getString(Config.percentOfLightRequired) + " light";
        fd.getServer().getConsoleSender().sendMessage(lightAdminMessage);
        for (Player y : fd.getServer().getOnlinePlayers()) {
            if (fd.getPermissions().hasAdminManagementPerm(y)) {
                if (y != player) {
                    y.sendMessage(lightAdminMessage);
                }
            }
        }
    }

    public boolean blockSeesNoLight(final Block block) {
        double percentage = Double.parseDouble(fd.getConfig().getString(Config.percentOfLightRequired).replaceAll("%", ""));
        double levelToDisableAt = percentage / 15.0;
        int lightLevel;
        int highestLevel = 0;
        for (BlockFace y : BlockFace.values()) {
            lightLevel = block.getRelative(y).getLightLevel();
            if (lightLevel > highestLevel) {highestLevel = lightLevel;}
            if (lightLevel > levelToDisableAt) {return false;}
        }
        return true;
    }

}
