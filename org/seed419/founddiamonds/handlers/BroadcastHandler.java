package org.seed419.founddiamonds.handlers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.Node;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.util.Format;
import org.seed419.founddiamonds.util.PluginUtils;
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
public class BroadcastHandler {


    private FoundDiamonds fd;


    public BroadcastHandler(FoundDiamonds fd) {
        this.fd = fd;
    }

    public void handleBroadcast(final BlockBreakEvent event, final Node node, final Player player) {
        final int blockTotal = fd.getBlockCounter().getTotalBlocks(event.getBlock());
        broadcastFoundBlock(player, node, blockTotal);
        if (node.getMaterial() == Material.DIAMOND_ORE) {
            if (fd.getConfig().getBoolean(Config.potionsForFindingDiamonds)) {
                fd.getPotionHandler().handlePotions(player);
            }
            if (fd.getConfig().getBoolean(Config.itemsForFindingDiamonds)) {
                fd.getItemHandler().handleRandomItems(player);
            }
        }
    }


    private void broadcastFoundBlock(final Player player, final Node node, final int blockTotal) {
        String playerName = getBroadcastName(player);
        String matName = Format.getFormattedName(node.getMaterial(), blockTotal);
        String message = fd.getConfig().getString(Config.bcMessage).replace("@Prefix@", Prefix.getChatPrefix() + node.getColor()).replace("@Player@",
                playerName +  (fd.getConfig().getBoolean(Config.useOreColors) ? node : "")).replace("@Number@",
                (blockTotal) == 500 ? "over 500" :String.valueOf(blockTotal)).replace("@BlockName@", matName);
        String formatted = PluginUtils.customTranslateAlternateColorCodes('&', message);
        fd.getServer().getConsoleSender().sendMessage(formatted);
        for (Player x : fd.getServer().getOnlinePlayers()) {
            if (fd.getPermissions().hasPerm(x, "fd.broadcast") && fd.getWorldHandler().isEnabledWorld(x)) {
                if (!fd.getAdminMessageHandler().getRecievedAdminMessage().contains(x.getName())) {
                    x.sendMessage(formatted);
                }
            }
        }

        if (fd.getConfig().getBoolean(Config.cleanLog)) {
            fd.getLoggingHandler().writeToCleanLog(node, blockTotal, playerName);
        }
    }

    private String getBroadcastName(Player player) {
        if (fd.getConfig().getBoolean(Config.useNick)) {
            return player.getDisplayName();
        } else {
            return player.getName();
        }
    }


}
