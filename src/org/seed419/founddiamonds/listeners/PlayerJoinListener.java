/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seed419.founddiamonds.listeners;

import java.util.Date;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.seed419.founddiamonds.Config;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.PlaytimeManager;

/**
 *
 * @author proxa
 */
public class PlayerJoinListener implements Listener {
    
    
    private FoundDiamonds fd;
    
    
    public PlayerJoinListener(FoundDiamonds fd) {
        this.fd = fd;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (fd.getConfig().getBoolean(Config.mysqlEnabled)) {
            PlaytimeManager.insertEntry(event.getPlayer(), new Date());
        }
    }
    
}
