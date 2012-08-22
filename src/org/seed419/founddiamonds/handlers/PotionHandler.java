package org.seed419.founddiamonds.handlers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.listeners.PlayerDamageListener;

/**
 * Attribute Only (Public) License
 * Version 0.a3, July 11, 2011
 * <p/>
 * Copyright (C) 2012 Blake Bartenbach <seed419@gmail.com> (@seed419)
 * <p/>
 * Anyone is allowed to copy and distribute verbatim or modified
 * copies of this license document and altering is allowed as long
 * as you attribute the author(s) of this license document / files.
 * <p/>
 * ATTRIBUTE ONLY PUBLIC LICENSE
 * TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 * <p/>
 * 1. Attribute anyone attached to the license document.
 * Do not remove pre-existing attributes.
 * <p/>
 * Plausible attribution methods:
 * 1. Through comment blocks.
 * 2. Referencing on a site, wiki, or about page.
 * <p/>
 * 2. Do whatever you want as long as you don't invalidate 1.
 *
 * @license AOL v.a3 <http://aol.nexua.org>
 */
public class PotionHandler {


    private JavaPlugin plugin;
    private PlayerDamageListener pdl;


    public PotionHandler(JavaPlugin plugin, PlayerDamageListener pdl) {
        this.plugin = plugin;
        this.pdl = pdl;
    }

    public void handleRandomPotions(Player player, int randomNumber) {
        PotionEffect potion;
        String potionMessage;
        int strength = plugin.getConfig().getInt(Config.potionStrength);
        if (randomNumber < 25) {
            potion = new PotionEffect(PotionEffectType.SPEED, 3000, strength);
            potionMessage = plugin.getConfig().getString(Config.speed);
        } else if (randomNumber >= 25 && randomNumber < 50) {
            potion = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 3000, strength);
            potionMessage = plugin.getConfig().getString(Config.strength);
        } else if (randomNumber >=50 && randomNumber < 100) {
            potion = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3000, strength);
            potionMessage = plugin.getConfig().getString(Config.resist);
        } else if (randomNumber >=100 && randomNumber < 125) {
            potion = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 3000, strength);
            potionMessage = plugin.getConfig().getString(Config.fireresist);
        } else if (randomNumber >=125 && randomNumber < 150) {
            potion = new PotionEffect(PotionEffectType.FAST_DIGGING, 3000, strength);
            potionMessage = plugin.getConfig().getString(Config.fastdig);
        } else if (randomNumber >=150 && randomNumber < 175) {
            potion = new PotionEffect(PotionEffectType.WATER_BREATHING, 3000, strength);
            potionMessage = plugin.getConfig().getString(Config.waterbreathe);
        } else if (randomNumber >=175 && randomNumber < 200) {
            potion = new PotionEffect(PotionEffectType.REGENERATION, 3000, strength);
            potionMessage = plugin.getConfig().getString(Config.regeneration);
        } else {
            potion = new PotionEffect(PotionEffectType.JUMP, 3000, strength);
            potionMessage = plugin.getConfig().getString(Config.jump);
        }
        givePotions(player, potion, potionMessage);
    }

    private void givePotions(Player player, PotionEffect potion, String potionMsg) {
        if (plugin.getConfig().getBoolean(Config.awardAllPotions)) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (!p.hasPotionEffect(potion.getType()) && WorldHandler.isEnabledWorld(p)) {
                    p.addPotionEffect(potion);
                    if (potion.getType() == PotionEffectType.JUMP) {
                        pdl.addJumpPotionPlayer(p);
                    }
                    p.sendMessage(FoundDiamonds.getPrefix() + ChatColor.DARK_RED + " " + potionMsg);
                }
            }
            sendPotionMessageToConsole(potion);
        } else {
            player.addPotionEffect(potion);
            if (potion.getType() == PotionEffectType.JUMP) {
                pdl.addJumpPotionPlayer(player);
            }
            player.sendMessage(FoundDiamonds.getPrefix() + ChatColor.DARK_RED + " " + potionMsg);
        }
    }

    private void sendPotionMessageToConsole(PotionEffect potion) {
        if (potion.getType() == PotionEffectType.SPEED) {
            plugin.getLogger().info(FoundDiamonds.getLoggerPrefix() + " A speed potion has been awarded to the players");
        } else if (potion.getType() == PotionEffectType.FIRE_RESISTANCE) {
            plugin.getLogger().info(FoundDiamonds.getLoggerPrefix() + " A fire resistance potion has been awarded to the players");
        } else if (potion.getType() == PotionEffectType.INCREASE_DAMAGE) {
            plugin.getLogger().info(FoundDiamonds.getLoggerPrefix() + " An attack buff potion has been awarded to the players");
        } else if (potion.getType() == PotionEffectType.JUMP) {
            plugin.getLogger().info(FoundDiamonds.getLoggerPrefix() + " A jump potion has been awarded to the players");
        } else if (potion.getType() == PotionEffectType.DAMAGE_RESISTANCE) {
            plugin.getLogger().info(FoundDiamonds.getLoggerPrefix() + " A damage resistance potion has been awarded to the players");
        } else if (potion.getType() == PotionEffectType.FAST_DIGGING) {
            plugin.getLogger().info(FoundDiamonds.getLoggerPrefix() + " A fast digging potion has been awarded to the players");
        } else if (potion.getType() == PotionEffectType.REGENERATION) {
            plugin.getLogger().info(FoundDiamonds.getLoggerPrefix() + " A regeneration potion has been awarded to the players");
        } else if (potion.getType() == PotionEffectType.WATER_BREATHING) {
            plugin.getLogger().info(FoundDiamonds.getLoggerPrefix() + " A water breathing potion has been awarded to the players");
        }

    }


}
