/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seed419.founddiamonds.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.seed419.founddiamonds.FoundDiamonds;

/**
 *
 * @author seed419
 */
public class QuitListener implements Listener {


    private FoundDiamonds fd;


    public QuitListener(FoundDiamonds fd) {
        this.fd = fd;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (fd.getAdminMessageMap().containsKey(event.getPlayer())) {
            fd.getAdminMessageMap().remove(event.getPlayer());
        }
    }

}
