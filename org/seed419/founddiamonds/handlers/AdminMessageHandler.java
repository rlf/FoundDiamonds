package org.seed419.founddiamonds.handlers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.util.Format;
import org.seed419.founddiamonds.util.Prefix;

import java.util.HashSet;

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

public class AdminMessageHandler {


    private FoundDiamonds fd;
    private HashSet<String> receivedAdminMessage = new HashSet<String>();


    public AdminMessageHandler(FoundDiamonds fd) {
        this.fd = fd;
    }


    public void sendAdminMessage(final Material mat, final int blockTotal, final Player player) {
        String adminMessage = Prefix.getAdminPrefix() + " " + ChatColor.YELLOW + player.getName() +
                ChatColor.DARK_RED + " just found " + fd.getMapHandler().getAdminMessageBlocks().get(mat) + (blockTotal == 500 ? "over 500 " :
                String.valueOf(blockTotal)) + " " + Format.getFormattedName(mat, blockTotal);
        fd.getServer().getConsoleSender().sendMessage(adminMessage);


        for (Player y : fd.getServer().getOnlinePlayers()) {
            if (fd.getPermissions().hasAdminMessagePerm(y) && y != player) {
                y.sendMessage(adminMessage);
                receivedAdminMessage.add(y.getName());
            }
        }
    }

    public void clearReceivedAdminMessage() {
        receivedAdminMessage.clear();
    }

    public boolean receivedAdminMessage(Player player) {
        return receivedAdminMessage.contains(player.getName());
    }
}
