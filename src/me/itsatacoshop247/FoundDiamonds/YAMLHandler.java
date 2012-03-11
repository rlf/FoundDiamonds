/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.itsatacoshop247.FoundDiamonds;

/**
 *
 * @author seed419
 */
public class YAMLHandler {


    private FoundDiamonds fd;
    private String disableInCreative = "Found Diamonds Configuration.Main.Disable in creative mode";
    private String disableMiningInTotalDarkness = "Found Diamonds Configuration.Main.Disable ore mining in total darkness";
    private String opsAsFDAdmin = "Found Diamonds Configuration.Main.Give OPs all permissions";
    private String kickOnTrapBreak = "Found Diamonds Configuration.Traps.Kick players on trap break";
    //Message not configurable in game
    private String kickMessage = "Found Diamonds Configuration.Traps.Kick message";
    private String banOnTrapBreak = "Found Diamonds Configuration.Traps.Ban players on trap break";
    private String adminAlertsOnAllTrapBreaks = "Found Diamonds Configuration.Traps.Admin alerts on all trap breaks";
    private String awardsForFindingDiamonds = "Found Diamonds Configuration.Awards.Random awards for finding diamonds";
    //Chance not configurable in game
    private String chanceToGetAward = "Found Diamonds Configuration.Awards.Percent of time awards are given";
    //Random items not configurable in game
    private String randomItem1 = "Found Diamonds Configuration.Awards.Random Item 1";
    private String randomItem2 = "Found Diamonds Configuration.Awards.Random Item 2";
    private String randomItem3 = "Found Diamonds Configuration.Awards.Random Item 3";
    private String bcDiamond = "Found Diamonds Configuration.Broadcasts.Diamond Ore";
    private String bcGold = "Found Diamonds Configuration.Broadcasts.Gold Ore";
    private String bcLapis = "Found Diamonds Configuration.Broadcasts.Lapis Ore";
    private String bcRedstone = "Found Diamonds Configuration.Broadcasts.Redstone Ore";
    private String bcIron = "Found Diamonds Configuration.Broadcasts.Iron Ore";
    private String bcCoal = "Found Diamonds Configuration.Broadcasts.Coal Ore";
    private String bcMossy = "Found Diamonds Configuration.Broadcasts.Mossy Cobblestone";
    private String bcObby = "Found Diamonds Configuration.Broadcasts.Obsidian";
    //Message not configurable in game
    private String bcMessage = "Found Diamonds Configuration.Broadcasts.Message";
    private String useNick = "Found Diamonds Configuration.Broadcasts.Use player nicknames";
    private String logDiamondBreaks = "Found Diamonds Configuration.Logging.Log all diamond ore breaks";
    //Enabled worlds not configurable in game
    private String enabledWorlds = "Found Diamonds Configuration.Enabled Worlds";
    private String diamondAdmin = "Found Diamonds Configuration.Admin Messages.Diamond Ore";
    private String goldAdmin = "Found Diamonds Configuration.Admin Messages.Gold Ore";
    private String lapisAdmin = "Found Diamonds Configuration.Admin Messages.Lapis Ore";
    private String ironAdmin = "Found Diamonds Configuration.Admin Messages.Iron Ore";


    public YAMLHandler(FoundDiamonds fd) {
        this.fd = fd;
    }

    public void firstLoad() {
        fd.getConfig().options().copyDefaults(true);
        fd.saveConfig();
    }

    public String getUseNick() {
        return useNick;
    }

    public String getGoldAdmin() {
        return goldAdmin;
    }

    public String getLapisAdmin() {
        return lapisAdmin;
    }

    public String getIronAdmin() {
        return ironAdmin;
    }
    
    public String getBcCoal() {
        return bcCoal;
    }

    public String getPercentTogetAwards() {
        return chanceToGetAward;
    }

    public String getDiamondAdmin() {
        return diamondAdmin;
    }

    public String getEnabledWorlds() {
        return enabledWorlds;
    }

    public void reloadConfiguration() {
        fd.reloadConfig();
    }

    public String getDisableInCreative() {
        return disableInCreative;
    }

    public String getKickMessage() {
        return kickMessage;
    }

    public String getDisableMiningInTotalDarkness() {
        return disableMiningInTotalDarkness;
    }

    public String getOpsAsFDAdmin() {
        return opsAsFDAdmin;
    }

    public String getKickOnTrapBreak() {
        return kickOnTrapBreak;
    }

    public String getBanOnTrapBreak() {
        return banOnTrapBreak;
    }

    public String getAdminAlertsOnAllTrapBreaks() {
        return adminAlertsOnAllTrapBreaks;
    }

    public String getAwardsForFindingDiamonds() {
        return awardsForFindingDiamonds;
    }

    public String getRandomItem1() {
        return randomItem1;
    }

    public String getRandomItem2() {
        return randomItem2;
    }

    public String getRandomItem3() {
        return randomItem3;
    }

    public String getBcDiamond() {
        return bcDiamond;
    }

    public String getBcGold() {
        return bcGold;
    }

    public String getBcLapis() {
        return bcLapis;
    }

    public String getBcRedstone() {
        return bcRedstone;
    }

    public String getBcIron() {
        return bcIron;
    }

    public String getBcMossy() {
        return bcMossy;
    }

    public String getBcMessage() {
        return bcMessage;
    }

    public String getLogDiamondBreaks() {
        return logDiamondBreaks;
    }

    public String getBcObby() {
        return bcObby;
    }

}
