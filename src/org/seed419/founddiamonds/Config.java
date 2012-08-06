/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seed419.founddiamonds;

/**
 *
 * @author seed419
 */
public class Config {


    private FoundDiamonds fd;

    /*Main*/
    public static String disableInCreative = "Found Diamonds Configuration.Main.Disable in creative mode";
    public static String opsAsFDAdmin = "Found Diamonds Configuration.Main.Give OPs all permissions";


    /*Light Level*/
    public static String percentOfLightRequired = "Found Diamonds Configuration.Light Level.Minimum amount of light required to mine blocks";
    public static String lightLevelBlocks = "Found Diamonds Configuration.Light Level.Monitored Blocks";

    /*Traps*/
    public static String kickOnTrapBreak = "Found Diamonds Configuration.Traps.Kick players on trap break";
    public static String kickMessage = "Found Diamonds Configuration.Traps.Kick message";
    public static String banOnTrapBreak = "Found Diamonds Configuration.Traps.Ban players on trap break";
    public static String adminAlertsOnAllTrapBreaks = "Found Diamonds Configuration.Traps.Admin alerts on all trap breaks";

    /*Awards.Items*/
    public static String itemsForFindingDiamonds = "Found Diamonds Configuration.Awards.Items.Random items for finding diamonds";
    public static String chanceToGetItem = "Found Diamonds Configuration.Awards.Items.Percent of time items are given";
    public static String maxItems = "Found Diamonds Configuration.Awards.Items.Maximum number or items to give";
    public static String randomItem1 = "Found Diamonds Configuration.Awards.Items.Random Item 1";
    public static String randomItem2 = "Found Diamonds Configuration.Awards.Items.Random Item 2";
    public static String randomItem3 = "Found Diamonds Configuration.Awards.Items.Random Item 3";

    /*Awards.Spells*/
    public static String potionsForFindingDiamonds = "Found Diamonds Configuration.Awards.Spells.Random spells for finding diamonds";
    public static String potionStrength = "Found Diamonds Configuration.Awards.Spells.Spell strength (1-5)";
    public static String chanceToGetPotion = "Found Diamonds Configuration.Awards.Spells.Percent of time spells are casted";

    /*Awards.Spells.Messages*/
    public static String jump = "Found Diamonds Configuration.Awards.Spells.Messages.Jump";
    public static String fireresist = "Found Diamonds Configuration.Awards.Spells.Messages.Fire Resistance";
    public static String strength = "Found Diamonds Configuration.Awards.Spells.Messages.Strength";
    public static String waterbreathe = "Found Diamonds Configuration.Awards.Spells.Messages.Water Breathing";
    public static String resist = "Found Diamonds Configuration.Awards.Spells.Messages.Resistance";
    public static String fastdig = "Found Diamonds Configuration.Awards.Spells.Messages.Fast Digging";
    public static String regeneration = "Found Diamonds Configuration.Awards.Spells.Messages.Regeneration";
    public static String speed = "Found Diamonds Configuration.Awards.Spells.Messages.Speed";

    /*Broadcasts*/
    public static String broadcastedBlocks = "Found Diamonds Configuration.Broadcasts.Broadcasted Blocks";
    public static String bcMessage = "Found Diamonds Configuration.Broadcasts.Options.Message";
    public static String useNick = "Found Diamonds Configuration.Broadcasts.Options.Use player nicknames";
    public static String useOreColors = "Found Diamonds Configuration.Broadcasts.Options.Use classic ore colors for ores";

    /*Logging*/
    public static String logTrapBreaks = "Found Diamonds Configuration.Logging.Trap breaks";
    public static String logLightLevelViolations = "Found Diamonds Configuration.Logging.Light Level Violations";
    public static String logDiamondBreaks = "Found Diamonds Configuration.Logging.Log all diamond ore breaks";
    public static String cleanLog = "Found Diamonds Configuration.Logging.Clean log (all ores)";
    public static String enabledWorlds = "Found Diamonds Configuration.Enabled Worlds";

    /*Admin Messages*/
    public static String adminMessageBlocks = "Found Diamonds Configuration.Admin Messages.Admin Message Blocks";

    /*MySQL*/
    public static String mysqlEnabled = "Found Diamonds Configuration.MySQL.Enabled";
    public static String mysqlUsername = "Found Diamonds Configuration.MySQL.Username";
    public static String mysqlPassword = "Found Diamonds Configuration.MySQL.Password";
    public static String mysqlDatabase = "Found Diamonds Configuration.MySQL.DatabaseName";
    public static String mysqlUrl = "Found Diamonds Configuration.MySQL.URL";
    public static String mysqlPort = "Found Diamonds Configuration.MySQL.Port";
    public static String mysqlPrefix = "Found Diamonds Configuration.MySQL.Prefix";

    /*Debug*/
    public static String debug = "Found Diamonds Configuration.Debug.Enabled";

    /*Metrics*/
    public static String metrics = "Found Diamonds Configuration.Metrics.Enabled";



    public Config(FoundDiamonds fd) {
        this.fd = fd;
    }

}