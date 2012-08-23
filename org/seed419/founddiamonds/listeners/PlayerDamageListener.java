/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seed419.founddiamonds.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;

/**
 *
 * @author seed419
 */
public class PlayerDamageListener implements Listener {


    private final HashMap<String, Boolean> jumpPotion = new HashMap<String,Boolean>();


    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if ((event.getCause() == DamageCause.FALL) && (event.getEntity() instanceof Player)) {
            Player player = (Player) event.getEntity();
            if (jumpPotion.containsKey(player.getName()) && player.hasPotionEffect(PotionEffectType.JUMP)) {
                event.setCancelled(true);
            } else {
                jumpPotion.put(player.getName(), false);
            }
        }
    }

    public void addJumpPotionPlayer(Player player) {
        jumpPotion.put(player.getName(), true);
    }
}
