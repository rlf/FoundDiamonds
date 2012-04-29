/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seed419.FoundDiamonds.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.seed419.FoundDiamonds.Config;
import org.seed419.FoundDiamonds.FoundDiamonds;

/**
 *
 * @author seed419
 */
public class JoinListener implements Listener {


    private FoundDiamonds fd;


    public JoinListener(FoundDiamonds fd) {
        this.fd = fd;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (fd.hasPerms(event.getPlayer(), "fd.admin")) {
            fd.getAdminMessageMap().put(event.getPlayer(), true);
        }
    }

}
