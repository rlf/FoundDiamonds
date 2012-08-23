package org.seed419.founddiamonds;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.file.FileHandler;
import org.seed419.founddiamonds.file.FileUtils;
import org.seed419.founddiamonds.handlers.*;
import org.seed419.founddiamonds.listeners.*;
import org.seed419.founddiamonds.metrics.MetricsLite;
import org.seed419.founddiamonds.sql.MySQL;

import java.io.IOException;
import java.util.logging.Logger;

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


    private static Logger log;
    private final FileUtils fu = new FileUtils(this);
    private final MySQL mysql = new MySQL(this);
    private final ListHandler lh = new ListHandler(this);
    private final Permissions p = new Permissions(this);
    private final PlayerDamageListener pdl = new PlayerDamageListener();
    private final WorldHandler wm = new WorldHandler(this);
    private final BlockPlaceListener bpl = new BlockPlaceListener(this, mysql);
    private final LoggingHandler logging = new LoggingHandler(this, fu);
    private final TrapHandler trap = new TrapHandler(this, logging);
    private final FileHandler fh = new FileHandler(this, wm, bpl, trap, fu);
    private final PotionHandler potions = new PotionHandler(this, pdl);
    private final ItemHandler itemHandler = new ItemHandler(this);
    private final BlockBreakListener bbl = new BlockBreakListener(this, mysql, trap, logging, bpl, potions, itemHandler);
    private final BlockDamageListener bdl = new BlockDamageListener(bbl);
    private final PistonListener pl = new PistonListener(trap);


    /*
     * Changelog:
     * Tons of refactoring, much cleaner code.  Pull requests should be much easier in the future
     * Added an option for awarding all player items/potions, or just the player who found the diamonds.
     * Removed config option for admin alerts on trap breaks.  Why would admins not want to know about this?
     * Pistons can no longer fool trap blocks
     */


    /*
     * TODO:
     * //Fix adding worlds with spaces
     * //Trap blocks in MySQL
     * //Is cleanlogging in SQL a popular request?
     * //Move light and admin messages into separate classes for fucks sake.
    * Smarter trap blocks - remember material NOT just the location!  Prevents pistons and physics from tricking them.
    * Implement Item IDs as an acceptable form of entering blocks
    * Finish set menu, integrate with main menu
    * Look into pulling stats from MC client?  Or MySQL?
    * /fd top ?
     * */

    @Override
    public void onEnable() {
        log = this.getLogger();
        fh.initFileVariables();
        fh.checkFiles();
        wm.checkWorlds();
        lh.loadAllBlocks();
        getCommand("fd").setExecutor(new CommandHandler(this, wm, trap));
        registerEvents();
        startMetrics();
        mysql.getConnection();
        log.info("Enabled");
    }

    public void registerEvents() {
        final PluginManager pm = getServer().getPluginManager();
        if (getConfig().getBoolean(Config.potionsForFindingDiamonds)) {
            pm.registerEvents(pdl, this);
        }
        pm.registerEvents(bbl, this);
        pm.registerEvents(bpl, this);
        pm.registerEvents(bdl, this);
        pm.registerEvents(pl, this);
    }

    @Override
    public void onDisable() {
        log.info("Saving all data...");
        fh.saveFlatFileData();
        log.info("Disabled");
    }

    public Logger getLog() {
        return log;
    }

    public PluginDescriptionFile getPdf() {
        return this.getDescription();
    }

    private void startMetrics() {
        if (this.getConfig().getBoolean(Config.metrics)) {
            try {
                MetricsLite metrics = new MetricsLite(this);
                metrics.start();
            } catch (IOException e) {}
        }
    }

}
