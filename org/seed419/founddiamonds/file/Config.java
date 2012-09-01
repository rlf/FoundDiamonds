package org.seed419.founddiamonds.file;
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
public class Config {

    /*Main*/
    public final static String disableInCreative = "Found Diamonds Configuration.Main.Disable in creative mode";
    public final static String opsAsFDAdmin = "Found Diamonds Configuration.Main.Give OPs all permissions";


    /*Light Level*/
    public final static String percentOfLightRequired = "Found Diamonds Configuration.Light Level.Minimum amount of light required to mine blocks";
    public final static String lightLevelAdminMessages = "Found Diamonds Configuration.Light Level.Send violation admin messages";
    public final static String silentMode = "Found Diamonds Configuration.Light Level.Silent Mode";
    public final static String lightLevelBlocks = "Found Diamonds Configuration.Light Level.Monitored Blocks";

    /*Traps*/
    public final static String kickOnTrapBreak = "Found Diamonds Configuration.Traps.Kick players on trap break";
    public final static String kickMessage = "Found Diamonds Configuration.Traps.Kick message";
    public final static String banOnTrapBreak = "Found Diamonds Configuration.Traps.Ban players on trap break";

    /*Awards.Items*/
    public final static String awardAllItems = "Found Diamonds Configuration.Awards.Items.Award all players";
    public final static String itemsForFindingDiamonds = "Found Diamonds Configuration.Awards.Items.Random items for finding diamonds";
    public final static String chanceToGetItem = "Found Diamonds Configuration.Awards.Items.Percent of time items are given";
    public final static String maxItems = "Found Diamonds Configuration.Awards.Items.Maximum number or items to give";
    public final static String randomItem1 = "Found Diamonds Configuration.Awards.Items.Random Item 1";
    public final static String randomItem2 = "Found Diamonds Configuration.Awards.Items.Random Item 2";
    public final static String randomItem3 = "Found Diamonds Configuration.Awards.Items.Random Item 3";

    /*Awards.Spells*/
    public final static String awardAllPotions = "Found Diamonds Configuration.Awards.Spells.Award all players";
    public final static String potionsForFindingDiamonds = "Found Diamonds Configuration.Awards.Spells.Random spells for finding diamonds";
    public final static String potionStrength = "Found Diamonds Configuration.Awards.Spells.Spell strength (1-5)";
    public final static String chanceToGetPotion = "Found Diamonds Configuration.Awards.Spells.Percent of time spells are casted";

    /*Awards.Spells.Messages*/
    public final static String jump = "Found Diamonds Configuration.Awards.Spells.Messages.Jump";
    public final static String fireresist = "Found Diamonds Configuration.Awards.Spells.Messages.Fire Resistance";
    public final static String strength = "Found Diamonds Configuration.Awards.Spells.Messages.Strength";
    public final static String waterbreathe = "Found Diamonds Configuration.Awards.Spells.Messages.Water Breathing";
    public final static String resist = "Found Diamonds Configuration.Awards.Spells.Messages.Resistance";
    public final static String fastdig = "Found Diamonds Configuration.Awards.Spells.Messages.Fast Digging";
    public final static String regeneration = "Found Diamonds Configuration.Awards.Spells.Messages.Regeneration";
    public final static String speed = "Found Diamonds Configuration.Awards.Spells.Messages.Speed";

    /*Broadcasts*/
    public final static String broadcastedBlocks = "Found Diamonds Configuration.Broadcasts.Broadcasted Blocks";
    public final static String bcMessage = "Found Diamonds Configuration.Broadcasts.Options.Message";
    public final static String useNick = "Found Diamonds Configuration.Broadcasts.Options.Use player nicknames";
    public final static String useOreColors = "Found Diamonds Configuration.Broadcasts.Options.Use classic ore colors for ores";

    /*Logging*/
    public final static String logTrapBreaks = "Found Diamonds Configuration.Logging.Trap breaks";
    public final static String logLightLevelViolations = "Found Diamonds Configuration.Logging.Light Level Violations";
    public final static String logDiamondBreaks = "Found Diamonds Configuration.Logging.Log all diamond ore breaks";
    public final static String cleanLog = "Found Diamonds Configuration.Logging.Clean log (all ores)";
    public final static String enabledWorlds = "Found Diamonds Configuration.Enabled Worlds";

    /*Admin Messages*/
    public final static String adminMessageBlocks = "Found Diamonds Configuration.Admin Messages.Admin Message Blocks";

    /*MySQL*/
    public final static String mysqlEnabled = "Found Diamonds Configuration.MySQL.Enabled";
    public final static String mysqlUsername = "Found Diamonds Configuration.MySQL.Username";
    public final static String mysqlPassword = "Found Diamonds Configuration.MySQL.Password";
    public final static String mysqlDatabase = "Found Diamonds Configuration.MySQL.DatabaseName";
    public final static String mysqlUrl = "Found Diamonds Configuration.MySQL.URL";
    public final static String mysqlPort = "Found Diamonds Configuration.MySQL.Port";
    public final static String mysqlPrefix = "Found Diamonds Configuration.MySQL.Prefix";

    /*Debug*/
    public final static String debug = "Found Diamonds Configuration.Debug.Enabled";

    /*Metrics*/
    public final static String metrics = "Found Diamonds Configuration.Metrics.Enabled";

}