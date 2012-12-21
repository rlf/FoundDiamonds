package org.seed419.founddiamonds.handlers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.util.Format;
import org.seed419.founddiamonds.util.Prefix;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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

public class LoggingHandler {


    private FoundDiamonds fd;


    public LoggingHandler(FoundDiamonds fd) {
        this.fd = fd;
    }

    public void handleLogging(Player player, Block block, boolean trapBlock, boolean kicked, boolean banned) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fd.getFileHandler().getLogFile(), true)));
            pw.print("[" + getFormattedDate() + "]");
            if (trapBlock) {
                pw.print(" [TRAP BLOCK]");
            }
            pw.println(" " + block.getType().name().toLowerCase().replace("_", " ") + " broken by "
                    + player.getName() + " at (x: " + block.getX() + ", y: " + block.getY() + ", z: " + block.getZ()
                    + ") in " + player.getWorld().getName());
            if (trapBlock) {
                pw.print("[" + getFormattedDate() + "]" + " [ACTION TAKEN] ");
                if (kicked && !banned) {
                    pw.println(player.getName() + " was kicked from the sever per the configuration.");
                } else if (banned && !kicked) {
                    pw.println(player.getName() + " was banned from the sever per the configuration.");
                } else if (banned && kicked) {
                    pw.println(player.getName() + " was kicked and banned from the sever per the configuration.");
                } else {
                    pw.println(player.getName() + " was neither kicked nor banned per the configuration.");
                }
            }
            pw.flush();
            fd.getFileUtils().close(pw);
        } catch (IOException ex) {
            fd.getLog().severe(MessageFormat.format("Unable to write to log file {0}", ex));
        }
    }

    public void logLightLevelViolation(final Material mat, final Player player) {
        String lightLogMsg = "[" + getFormattedDate() + "]" + " " + player.getName() + " mined "
                + Format.getFormattedName(mat, 1) + " below " +  fd.getConfig().getString(Config.percentOfLightRequired) +
                " light";
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fd.getFileHandler().getLogFile(), true)));
            pw.println(lightLogMsg);
            pw.flush();
            fd.getFileUtils().close(pw);
        } catch (IOException ex) {
            fd.getLog().severe(MessageFormat.format("Unable to write to light level violation to log.txt file {0}", ex));
        }
    }

    public void writeToCleanLog(final String matName, final int blockTotal, final String playerName) {
        String formattedDate = getFormattedDate();
        String message = fd.getConfig().getString(Config.bcMessage).replace("@Prefix@", Prefix.getChatPrefix()).replace("@Player@",
                playerName).replace("@Number@",
                (blockTotal) == 500 ? "over 500" :String.valueOf(blockTotal)).replace("@BlockName@", matName);
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fd.getFileHandler().getCleanLog(), true)));
            pw.println("[" + formattedDate + "] " + message);
            pw.flush();
            fd.getFileUtils().close(pw);
        } catch (IOException ex) {
            fd.getLog().severe(MessageFormat.format("Unable to write to clean log {0}", ex));
        }
    }

    private String getFormattedDate() {
        Date todaysDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss");
        return formatter.format(todaysDate);
    }

}
