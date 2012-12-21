package org.seed419.founddiamonds.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
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

public class PlayerDamageListener implements Listener {


    private FoundDiamonds fd;


    public PlayerDamageListener(FoundDiamonds fd) {
        this.fd = fd;
    }

    @EventHandler
    void onPlayerDamage(final EntityDamageEvent event) {
        if ((event.getCause() == DamageCause.FALL) && (event.getEntity() instanceof Player)) {
            final Player player = (Player) event.getEntity();
            if (fd.getPotionHandler().playerHasJumpPotion(player)) {
                event.setCancelled(true);
            }
        }
    }
}
