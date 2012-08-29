package org.seed419.founddiamonds.handlers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.Node;
import org.seed419.founddiamonds.util.Format;
import org.seed419.founddiamonds.util.Prefix;

import java.util.HashSet;

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
public class AdminMessageHandler {


    private FoundDiamonds fd;
    private HashSet<String> recievedAdminMessage = new HashSet<String>();



    public AdminMessageHandler(FoundDiamonds fd) {
        this.fd = fd;
    }


    public void sendAdminMessage(final BlockBreakEvent event, final Node node, final Player player) {
        final int blockTotal = fd.getBlockCounter().getTotalBlocks(event.getBlock());
        String adminMessage = Prefix.getAdminPrefix() + " " + ChatColor.YELLOW + player.getName() +
                ChatColor.DARK_RED + " just found " + node.getColor() + (blockTotal == 500 ? "over 500 " :
                String.valueOf(blockTotal)) + " " + Format.getFormattedName(node.getMaterial(), blockTotal);
        fd.getServer().getConsoleSender().sendMessage(adminMessage);
        for (Player y : fd.getServer().getOnlinePlayers()) {
            if (fd.getPermissions().hasPerm(y, "fd.admin") && y != player) {
                y.sendMessage(adminMessage);
                recievedAdminMessage.add(y.getName());
            }
        }
    }

    public HashSet<String> getRecievedAdminMessage() {
        return recievedAdminMessage;
    }
}
