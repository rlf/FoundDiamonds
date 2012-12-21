package org.seed419.founddiamonds.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.seed419.founddiamonds.FoundDiamonds;

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
