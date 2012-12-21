package org.seed419.founddiamonds.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
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

public class TrapListener implements Listener {

	private FoundDiamonds fd;

	public TrapListener(FoundDiamonds fd) {
		this.fd = fd;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	void onBlockBreak(final BlockBreakEvent event) {
		if (!fd.getWorldHandler().isEnabledWorld(event.getPlayer())) {
			return;
		}
		final Location loc = event.getBlock().getLocation();
		if (fd.getTrapHandler().isTrapBlock(loc)) {
			fd.getTrapHandler().handleTrapBlockBreak(event);
			event.setCancelled(true);
		}
	}

}
