package org.seed419.founddiamonds.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.seed419.founddiamonds.FoundDiamonds;

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
public class BlockDamageListener implements Listener {


    private FoundDiamonds fd;


    public BlockDamageListener(FoundDiamonds fd) {
        this.fd = fd;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onBlockDamage(final BlockDamageEvent event) {
        final Player player = event.getPlayer();
        if (!fd.getWorldHandler().isEnabledWorld(player)) { return; }
        if (!fd.getWorldHandler().isValidGameMode(player)) { return; }
        final Material mat = event.getBlock().getType();
        if (fd.getPermissions().hasMonitorPerm(player)) {
            if (fd.getMapHandler().getLightLevelBlocks().containsKey(mat)) {
                fd.getLightLevelHandler().handleLightLevelMonitor(event, mat, player);
            }
        }
    }
}
