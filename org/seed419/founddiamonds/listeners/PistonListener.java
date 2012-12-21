package org.seed419.founddiamonds.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
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

public class PistonListener implements Listener {


    private FoundDiamonds fd;


    public PistonListener(FoundDiamonds fd) {
        this.fd = fd;
    }


    @EventHandler
    void onPistonRetract(final BlockPistonRetractEvent event) {
        if (fd.getTrapHandler().isTrapBlock(event.getRetractLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onPistonExtend(final BlockPistonExtendEvent event) {
        for (Block x : event.getBlocks()) {
            if (fd.getTrapHandler().isTrapBlock(x.getLocation())) {
                event.setCancelled(true);
            }
        }
    }

}
