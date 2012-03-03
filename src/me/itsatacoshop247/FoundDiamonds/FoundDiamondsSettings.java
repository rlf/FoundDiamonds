package me.itsatacoshop247.FoundDiamonds;

public class FoundDiamondsSettings {
    
    
    private int RandomItem1;
    private int RandomItem2;
    private int RandomItem3;
    private boolean randomItems;
    private boolean diamondBC;
    private boolean redstoneBC;
    private boolean goldBC;
    private boolean ironBC;
    private boolean lapisBC;
    private boolean mossyBC;
    private boolean logging;
    private String broadcastMessage;
    private boolean kickOnTrapBreak;
    private boolean banOnTrapBreak;
    private boolean trapBlockAdminMsg;
    private boolean opsHavePerms;
    private boolean disableDarkMining;
    private boolean disableInCreative;
    private int waitTime;
    private FoundDiamonds fd;
    
    
    public FoundDiamondsSettings(FoundDiamonds fd) {
        this.fd = fd;
    }
    
    
    public void loadMain() {
        String propertiesFile = fd.getMainDir() + "FoundDiamonds.properties";
        FoundDiamondsPluginProperties properties = new FoundDiamondsPluginProperties(fd, propertiesFile);
        properties.load();
        disableInCreative = properties.getBoolean("DisableInCreativeMode", true);
        disableDarkMining = properties.getBoolean("DisableTotalDarknessMining", true);
        trapBlockAdminMsg = properties.getBoolean("TrapBlockAdminAlerts", false);
        waitTime = properties.getInteger("SecondsBetweenFoundOreBroadcasts", 20);
        opsHavePerms = properties.getBoolean("TreatOPSAsFD.Admin", true);
        randomItems = properties.getBoolean("RandomAwardsForFindingDiamonds", true);
        RandomItem1= properties.getInteger("RandomItem1", 265);
        RandomItem2= properties.getInteger("RandomItem2", 263);
        RandomItem3= properties.getInteger("RandomItem3", 341);
        broadcastMessage = properties.getString("BroadcastMessage", "@Player@ just found @Number@ @BlockName@!");
        diamondBC = properties.getBoolean("BroadcastForDiamond", true);
        redstoneBC = properties.getBoolean("BroadcastForRedstone", false);
        mossyBC = properties.getBoolean("BroadcastForMossy", true);
        goldBC = properties.getBoolean("BroadcastForGold", true);
        ironBC = properties.getBoolean("BroadcastForIron", false);
        lapisBC = properties.getBoolean("BroadcastForLapis", true);
        logging = properties.getBoolean("LogOreFinding", false);
        kickOnTrapBreak = properties.getBoolean("KickOnTrapBreak", true);
        banOnTrapBreak = properties.getBoolean("BanOnTrapBreak", false);
        properties.save("===[FoundDiamonds] Configuration===");
    }
    
    public boolean disableInCreativeMode() {
        return disableInCreative;
    }
    
    public boolean darkMiningDisabled() {
        return disableDarkMining;
    }

    public int getRandomItem1() {
        return RandomItem1;
    }

    public int getRandomItem2() {
        return RandomItem2;
    }

    public int getRandomItem3() {
        return RandomItem3;
    }

    public boolean randomItems() {
        return randomItems;
    }

    public boolean broadcastDiamond() {
        return diamondBC;
    }

    public boolean broadcastRedstone() {
        return redstoneBC;
    }

    public boolean broadcastGold() {
        return goldBC;
    }

    public boolean broadcastIron() {
        return ironBC;
    }

    public boolean broadcastLapis() {
        return lapisBC;
    }

    public boolean loggingIsEnabled() {
        return logging;
    }

    public String getBroadcastmessage() {
        return broadcastMessage;
    }

    public boolean kickOnTrapBreak() {
        return kickOnTrapBreak;
    }

    public boolean banOnTrapBreak() {
        return banOnTrapBreak;
    }

    public boolean trapBlockAdminMsg() {
        return trapBlockAdminMsg;
    }

    public boolean opsHavePerms() {
        return opsHavePerms;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public boolean broadcastMossy() {
        return mossyBC;
    }

}