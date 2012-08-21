package org.seed419.founddiamonds.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.seed419.founddiamonds.*;

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
public class BlockDamageListener implements Listener {


    private BlockBreakListener bbl;


    public BlockDamageListener(BlockBreakListener bbl) {
        this.bbl = bbl;
    }


    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        Material mat = event.getBlock().getType();
        Node lightNode = Node.getNodeByMaterial(ListHandler.getLightLevelBlocks(), mat);
        if (lightNode != null) {
            EventInformation lightEvent = new EventInformation(event, lightNode, false);
            if(!isValidLightLevel(lightEvent, event)) {
                return;
            }
        }
    }

    private boolean isValidLightLevel(EventInformation ei, BlockDamageEvent event) {
        if (Permissions.hasPerms(ei.getPlayer(), "fd.monitor")) {
            if (bbl.blockSeesNoLight(ei) && ei.getPlayer().getWorld().getEnvironment() != World.Environment.NETHER) {
                event.setCancelled(true);
                ei.getPlayer().sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " Mining in the dark is dangerous, place a torch!");
                return false;
            }
        }
        return true;
    }


}
