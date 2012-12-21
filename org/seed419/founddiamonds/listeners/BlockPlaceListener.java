package org.seed419.founddiamonds.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.file.Config;

import java.util.HashSet;

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

public class BlockPlaceListener implements Listener {


    private HashSet<Location> placed = new HashSet<Location>();
    private FoundDiamonds fd;


    public BlockPlaceListener(FoundDiamonds fd) {
        this.fd = fd;
    }


    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (fd.getWorldHandler().isEnabledWorld(event.getPlayer())) {
            if (isMonitoredBlock(event)) {
                addBlock(event);
            }
        }
    }

    public void addBlock(BlockPlaceEvent event) {
        if (fd.getConfig().getBoolean(Config.mysqlEnabled)) {
            fd.getMySQL().updatePlacedBlockinSQL(event.getBlock().getLocation());
        } else {
            placed.add(event.getBlock().getLocation());
        }
    }

    public boolean isMonitoredBlock(BlockPlaceEvent event) {
        final Material mat = event.getBlock().getType();
        return fd.getMapHandler().getAdminMessageBlocks().containsKey(mat) ||
                fd.getMapHandler().getBroadcastedBlocks().containsKey(mat) ||
                fd.getMapHandler().getLightLevelBlocks().containsKey(mat);
    }

    public HashSet<Location> getFlatFilePlacedBlocks() {
        return placed;
    }


}
