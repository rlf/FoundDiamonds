package org.seed419.founddiamonds.handlers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.file.Config;

import java.util.HashMap;

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

public class PotionHandler {


    private FoundDiamonds fd;
    private final HashMap<String, Boolean> jumpPotion = new HashMap<String,Boolean>();


    public PotionHandler(FoundDiamonds fd) {
        this.fd = fd;
    }

    public void handlePotions(final Player player) {
        int randomInt = (int) (Math.random()*100);
        if (randomInt <= fd.getConfig().getInt(Config.chanceToGetPotion)) {
            int randomNumber = (int)(Math.random()*225);
            if (randomNumber >= 0 && randomNumber <= 225) {
                selectRandomPotion(player, randomNumber);
            }
        }
    }

    private void selectRandomPotion(Player player, int randomNumber) {
        PotionEffect potion;
        String potionMessage;
        int strength = fd.getConfig().getInt(Config.potionStrength);
        if (randomNumber < 25) {
            potion = new PotionEffect(PotionEffectType.SPEED, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.speed);
        } else if (randomNumber >= 25 && randomNumber < 50) {
            potion = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.strength);
        } else if (randomNumber >=50 && randomNumber < 100) {
            potion = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.resist);
        } else if (randomNumber >=100 && randomNumber < 125) {
            potion = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.fireresist);
        } else if (randomNumber >=125 && randomNumber < 150) {
            potion = new PotionEffect(PotionEffectType.FAST_DIGGING, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.fastdig);
        } else if (randomNumber >=150 && randomNumber < 175) {
            potion = new PotionEffect(PotionEffectType.WATER_BREATHING, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.waterbreathe);
        } else if (randomNumber >=175 && randomNumber < 200) {
            potion = new PotionEffect(PotionEffectType.REGENERATION, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.regeneration);
        } else {
            potion = new PotionEffect(PotionEffectType.JUMP, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.jump);
        }
        givePotions(player, potion, potionMessage);
    }

    private void givePotions(Player player, PotionEffect potion, String potionMsg) {
        if (fd.getConfig().getBoolean(Config.awardAllPotions)) {
            for (Player p : fd.getServer().getOnlinePlayers()) {
                if (!p.hasPotionEffect(potion.getType()) && fd.getWorldHandler().isEnabledWorld(p)) {
                    p.addPotionEffect(potion);
                    if (potion.getType() == PotionEffectType.JUMP) {
                        jumpPotion.put(p.getName(), true);
                    }
                    p.sendMessage(ChatColor.DARK_RED + potionMsg);
                }
            }
            sendPotionMessageToConsole(potion);
        } else {
            player.addPotionEffect(potion);
            if (potion.getType() == PotionEffectType.JUMP) {
                jumpPotion.put(player.getName(), true);
            }
            player.sendMessage(ChatColor.DARK_RED + potionMsg);
        }
    }

    private void sendPotionMessageToConsole(PotionEffect potion) {
        if (potion.getType() == PotionEffectType.SPEED) {
            fd.getLog().info("A speed potion has been awarded to the players");
        } else if (potion.getType() == PotionEffectType.FIRE_RESISTANCE) {
            fd.getLog().info("A fire resistance potion has been awarded to the players");
        } else if (potion.getType() == PotionEffectType.INCREASE_DAMAGE) {
            fd.getLog().info("An attack buff potion has been awarded to the players");
        } else if (potion.getType() == PotionEffectType.JUMP) {
            fd.getLog().info("A jump potion has been awarded to the players");
        } else if (potion.getType() == PotionEffectType.DAMAGE_RESISTANCE) {
            fd.getLog().info("A damage resistance potion has been awarded to the players");
        } else if (potion.getType() == PotionEffectType.FAST_DIGGING) {
            fd.getLog().info("A fast digging potion has been awarded to the players");
        } else if (potion.getType() == PotionEffectType.REGENERATION) {
            fd.getLog().info("A regeneration potion has been awarded to the players");
        } else if (potion.getType() == PotionEffectType.WATER_BREATHING) {
            fd.getLog().info("A water breathing potion has been awarded to the players");
        }
    }

    public boolean playerHasJumpPotion(final Player player) {
        if (jumpPotion.containsKey(player.getName()) && jumpPotion.get(player.getName()) && player.hasPotionEffect(PotionEffectType.JUMP)) {
            return true;
        } else {
            jumpPotion.put(player.getName(), false);
            return false;
        }
    }
}