package org.seed419.founddiamonds.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.file.Config;

/**
 * Attribute Only (Public) License
 * Version 0.a3, July 11, 2011
 *
 * Copyright (C) 2012 Blake Bartenbach <seed419@gmail.com> (@seed419)
 *
 * Anyone is allowed to copy and distribute verbatim or modified
 * copies of this license document and altering is allowed as long
 * as you attribute the author(s) of this license document / files.
 *
 * ATTRIBUTE ONLY PUBLIC LICENSE
 * TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 * 1. Attribute anyone attached to the license document.
 * Do not remove pre-existing attributes.
 *
 * Plausible attribution methods:
 * 1. Through comment blocks.
 * 2. Referencing on a site, wiki, or about page.
 *
 * 2. Do whatever you want as long as you don't invalidate 1.
 *
 * @license AOL v.a3 <http://aol.nexua.org>
 */
public class BlockBreakListener implements Listener  {


    private FoundDiamonds fd;


    public BlockBreakListener(FoundDiamonds fd) {
        this.fd = fd;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {

        final Player player = event.getPlayer();

        if (!fd.getWorldHandler().isEnabledWorld(player)) { return; }
        if (!fd.getWorldHandler().isValidGameMode(player)) { return; }
        if (event.getEventName().equalsIgnoreCase("FakeBlockBreakEvent")) { return; }

        final Location loc = event.getBlock().getLocation();
        fd.getLightLevelHandler().checkAndClearLightLevelLocation(loc);
        if (!fd.getBlockCounter().isAnnounceable(loc)) {
            fd.getBlockCounter().removeAnnouncedOrPlacedBlock(loc);
            return;
        }

        final Material mat = event.getBlock().getType();
        int blockTotal = 0;
        int lightLevel = 99;

        if (fd.getPermissions().hasMonitorPerm(player)) {
            if (fd.getMapHandler().getAdminMessageBlocks().containsKey(mat)) {
                lightLevel = fd.getLightLevelHandler().getLightLevel(event.getBlock());
                blockTotal = fd.getBlockCounter().getTotalBlocks(event.getBlock());
                fd.getAdminMessageHandler().sendAdminMessage(mat, blockTotal, player);
            }
        }

        if (fd.getPermissions().hasBroadcastPerm(player)) {
            if (fd.getMapHandler().getBroadcastedBlocks().containsKey(mat)) {
                if (blockTotal == 0) {blockTotal = fd.getBlockCounter().getTotalBlocks(event.getBlock());}
                if (lightLevel == 99) {lightLevel = fd.getLightLevelHandler().getLightLevel(event.getBlock());}
                fd.getBroadcastHandler().handleBroadcast(mat, blockTotal, player, lightLevel);
            }
        }

        if (mat == Material.DIAMOND_ORE) {
            if (fd.getConfig().getBoolean(Config.logDiamondBreaks)) {
                fd.getLoggingHandler().handleLogging(event.getPlayer(), event.getBlock(), false, false, false);
            }
        }

        fd.getAdminMessageHandler().clearRecievedAdminMessage();
    }
}