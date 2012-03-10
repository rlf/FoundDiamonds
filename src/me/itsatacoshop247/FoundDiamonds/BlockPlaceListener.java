/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.itsatacoshop247.FoundDiamonds;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 *
 * @author seed419
 */
public class BlockPlaceListener implements Listener {


    private FoundDiamonds fd;


    public BlockPlaceListener(FoundDiamonds fd) {
        this.fd = fd;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (fd.getEnabledBlocks().contains(event.getBlock().getType())) {
            fd.addToPlacedBlocks(event.getBlock().getLocation());
        }
    }
}
