package org.seed419.founddiamonds.handlers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.seed419.founddiamonds.*;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.file.FileHandler;
import org.seed419.founddiamonds.util.Format;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
public class LoggingHandler {


    private FoundDiamonds fd;


    public LoggingHandler(FoundDiamonds fd) {
        this.fd = fd;
    }


    public void handleLogging(Player player, Block block, boolean trapBlock, boolean kicked, boolean banned) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(FileHandler.getLogFile(), true)));
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
                } else if (!banned && !kicked) {
                    pw.println(player.getName() + " was neither kicked nor banned per the configuration.");
                }
            }
            pw.flush();
            FileHandler.close(pw);
        } catch (IOException ex) {
            fd.getLog().severe(MessageFormat.format("[{0}] Unable to write to log file {1}", FoundDiamonds.getPrefix(), ex));
        }
    }

    public void logLightLevelViolation(EventInformation ei,  int lightLevel) {
        String lightLogMsg = "[" + getFormattedDate() + "]" + " " + ei.getPlayer().getName() + " was denied mining "
                + Format.getFormattedName(ei.getMaterial(), 1) + " at" + " light level " +  lightLevel;
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(FileHandler.getLogFile(), true)));
            pw.println(lightLogMsg);
            pw.flush();
            FileHandler.close(pw);
        } catch (IOException ex) {
            fd.getLog().severe(MessageFormat.format("[{0}] Unable to write to log file {1}", FoundDiamonds.getPrefix(), ex));
        }
    }

    public void writeToCleanLog(EventInformation ei, String playerName) {
        String formattedDate = getFormattedDate();
        String message;
        if (ei.getMaterial() == Material.GLOWING_REDSTONE_ORE || ei.getMaterial() == Material.REDSTONE_ORE) {
            if (ei.getTotal() > 1) {
                message = fd.getConfig().getString(Config.bcMessage).replace("@Player@", playerName
                ).replace("@Number@", String.valueOf(ei.getTotal())).replace("@BlockName@", "redstone ores");
            } else {
                message = fd.getConfig().getString(Config.bcMessage).replace("@Player@", playerName
                ).replace("@Number@", String.valueOf(ei.getTotal())).replace("@BlockName@", "redstone ore");
            }
        } else if (ei.getMaterial() == Material.OBSIDIAN) {
            message = fd.getConfig().getString(Config.bcMessage).replace("@Player@", playerName
            ).replace("@Number@", String.valueOf(ei.getTotal())).replace("@BlockName@", "obsidian");
        } else {
            String blockName = Format.getFormattedName(ei.getMaterial(), ei.getTotal());
            if (ei.getTotal() > 1) {
                message = fd.getConfig().getString(Config.bcMessage).replace("@Player@", playerName
                ).replace("@Number@", String.valueOf(ei.getTotal())).replace("@BlockName@", blockName +
                        (ei.getMaterial() == Material.DIAMOND_ORE ? "s!" : "s"));
            } else {
                message = fd.getConfig().getString(Config.bcMessage).replace("@Player@", playerName
                ).replace("@Number@", String.valueOf(ei.getTotal())).replace("@BlockName@", blockName +
                        (ei.getMaterial() == Material.DIAMOND_ORE ? "!" : ""));
            }
        }
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(FileHandler.getCleanLog(), true)));
            pw.println("[" + formattedDate + "] " + message);
            pw.flush();
            FileHandler.close(pw);
        } catch (IOException ex) {
            fd.getLog().severe(MessageFormat.format("[{0}] Unable to write to clean log {1}", FoundDiamonds.getPrefix(), ex));
        }
    }

    private String getFormattedDate() {
        Date todaysDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss");
        return formatter.format(todaysDate);
    }

}
