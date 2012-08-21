package org.seed419.founddiamonds;

import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.seed419.founddiamonds.listeners.BlockBreakListener;
import org.seed419.founddiamonds.listeners.BlockPlaceListener;
import org.seed419.founddiamonds.listeners.PlayerDamageListener;
import org.seed419.founddiamonds.metrics.MetricsLite;
import org.seed419.founddiamonds.sql.MySQL;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Logger;

/* TODO
* Smarter trap blocks - remember material NOT just the location!  Prevents pistons and physics from tricking them.
* Implement Item IDs as an acceptable form of entering blocks
* Finish set menu, integrate with main menu
* Look into pulling stats from MC client?  Or MySQL?
* /fd top ?
* /

/*  Attribute Only (Public) License
        Version 0.a3, July 11, 2011

    Copyright (C) 2012 Blake Bartenbach <seed419@gmail.com> (@seed419)

    Anyone is allowed to copy and distribute verbatim or modified
    copies of this license document and altering is allowed as long
    as you attribute the author(s) of this license document / files.

    ATTRIBUTE ONLY PUBLIC LICENSE
    TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

      1. Attribute anyone attached to the license document.
         * Do not remove pre-existing attributes.

         Plausible attribution methods:
            1. Through comment blocks.
            2. Referencing on a site, wiki, or about page.

      2. Do whatever you want as long as you don't invalidate 1.


@license AOL v.a3 <http://aol.nexua.org>*/



public class FoundDiamonds extends JavaPlugin {


    private final static String prefix = "[FD]";
    private final static String adminPrefix = ChatColor.RED + "[FD]";
    private final static String debugPrefix = "[FD Debug] ";
    private final static String loggerPrefix = "[FoundDiamonds]";

    public Logger log;

    //Todo this makes no sense being here...

    private final MySQL mysql = new MySQL(this);
    private final ListHandler lh = new ListHandler(this);
    private final Permissions p = new Permissions(this);
    private final PlayerDamageListener pdl = new PlayerDamageListener(this);
    private final WorldManager wm = new WorldManager(this);
    private final Logging logging = new Logging(this);
    private final Trap trap = new Trap(this, logging);
    private final BlockPlaceListener bpl = new BlockPlaceListener(this, mysql);
    private final BlockBreakListener bl = new BlockBreakListener(this, mysql, trap, logging, bpl, pdl);
    private final FileHandler fh = new FileHandler(this, wm, bpl, trap);

    private static PluginDescriptionFile pdf;
    private String pluginName;



    /*
     * Changelog:
     * Refactored a ton of code for cleaner and easier maintenance
     * Fixed inadvertently writing announced blocks to the .placed blocks file
     * Finally implemented MySQL functionality for placed blocks!
     * Fixed trap blocks not being persistent
     */


    /*
     * TODO:
     * Add all player or one player potions and awards.
     * Keep working on .placed in SQL
     */

    @Override
    public void onEnable() {
        log = this.getLogger();
        pdf = this.getDescription();
        pluginName = pdf.getName();

        fh.initFileVariables();
        fh.checkFiles();
        wm.checkWorlds();
        lh.loadAllBlocks();

        getCommand("fd").setExecutor(new CommandHandler(this, wm, trap));

        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this.bl, this);
        pm.registerEvents(pdl, this);
        pm.registerEvents(bpl, this);

        startMetrics();

        mysql.getConnection();

        log.info(MessageFormat.format("[{0}] Enabled", pluginName));
    }

    @Override
    public void onDisable() {
        log.info(MessageFormat.format("[{0}] Saving all data...", pluginName));
        String info = "This file stores your trap block locations.";
        String info2 = "If you have any issues with traps - feel free to delete this file.";
        boolean temp = fh.writeBlocksToFile(fh.getTrapsFile(), trap.getTrapBlocks(), info, info2);
        boolean temp2 = true;
        if (!getConfig().getBoolean(Config.mysqlEnabled)) {
            String info5 = "This file stores blocks that would be announced that players placed";
            String info6 = "If you'd like to announce these placed blocks, feel free to delete this file.";
            temp2 = fh.writeBlocksToFile(fh.getPlacedFile(), bpl.getFlatFilePlacedBlocks(), info5, info6);
        }
        if (temp && temp2) {
            log.info(MessageFormat.format("[{0}] Data successfully saved.", pluginName));
        } else {
            log.warning(MessageFormat.format("[{0}] Couldn't save blocks to files!", pluginName));
            log.warning(MessageFormat.format("[{0}] You could try deleting .placed and .traps if they exist", pluginName));
        }
        log.info(MessageFormat.format("[{0}] Disabled", pluginName));
    }

    public Logger getLog() {
        return log;
    }

    public String getPluginName() {
        return pluginName;
    }

    public static PluginDescriptionFile getPdf() {
        return pdf;
    }

    public static String getPrefix() {
        return prefix;
    }

    public static String getAdminPrefix() {
        return adminPrefix;
    }

    public static String getDebugPrefix() {
        return debugPrefix;
    }

    public static String getLoggerPrefix() {
        return loggerPrefix;
    }


    /*
     * Metrics
     */
    private void startMetrics() {
        if (this.getConfig().getBoolean(Config.metrics)) {
            try {
                MetricsLite metrics = new MetricsLite(this);
                metrics.start();
            } catch (IOException e) {}
        }
    }

}
