package org.seed419.founddiamonds.handlers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.util.Format;
import org.seed419.founddiamonds.util.Prefix;

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
public class ItemHandler {


    private FoundDiamonds fd;


    public ItemHandler(FoundDiamonds fd) {
        this.fd = fd;
    }

    public void handleRandomItems(Player player, int randomNumber) {
        int randomItem;
        if (randomNumber < 50) {
            randomItem = fd.getConfig().getInt(Config.randomItem1);
        } else if (randomNumber >= 50 && randomNumber < 100) {
            randomItem = fd.getConfig().getInt(Config.randomItem2);
        } else {
            randomItem = fd.getConfig().getInt(Config.randomItem3);
        }
        int amount = getRandomItemAmount();
        giveItems(player, randomItem, amount);
    }

    @SuppressWarnings("deprecation")
    private void giveItems(Player player, int item, int amount) {
        if (fd.getConfig().getBoolean(Config.awardAllItems)) {
            for(Player p: fd.getServer().getOnlinePlayers()) {
                if (fd.getWorldHandler().isEnabledWorld(p)) {
                    p.sendMessage(Prefix.getChatPrefix() + ChatColor.GRAY + " Everyone else got " + amount +
                            " " + Format.getFormattedName(Material.getMaterial(item), amount));
                    p.getInventory().addItem(new ItemStack(item, amount));
                    p.updateInventory();
                }
            }
        } else {
            player.sendMessage(Prefix.getChatPrefix() + ChatColor.GRAY + " You got " + amount +
                    " " + Format.getFormattedName(Material.getMaterial(item), amount));
            player.getInventory().addItem(new ItemStack(item, amount));
            player.updateInventory();
        }
    }

    private int getRandomItemAmount(){
        return ((fd.getConfig().getInt(Config.maxItems)) + 1);
    }
}
