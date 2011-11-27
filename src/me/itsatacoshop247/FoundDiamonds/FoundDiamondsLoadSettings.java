package me.itsatacoshop247.FoundDiamonds;

//import org.bukkit.ChatColor;

//import me.itsatacoshop247.FoundDiamonds.FoundDiamondsPluginProperties;

public class FoundDiamondsLoadSettings {
	static int RandomItem1;
	static int RandomItem2;
        static int RandomItem3;
	static boolean showmessage;
	static boolean randomitems;
	static boolean diamond;
	static boolean redstone;
	static boolean gold;
	static boolean iron;
	static boolean lupuslazuli;
	static boolean diamondadmin;
	static boolean redstoneadmin;
	static boolean goldadmin;
	static boolean ironadmin;
	static boolean lupuslazuliadmin;
	static boolean thirtysecondwait;
	static boolean logging;
	static String broadcastmessage;
        static boolean kickontrapbreak;
        static boolean banontrapbreak;
        static boolean trapblockadmin;
        static boolean opstxt;
        static int waittime;
        static boolean mossy;
	
	public static void loadMain(){
		String propertiesFile = FoundDiamonds.mainDir + "FoundDiamonds.properties";
		FoundDiamondsPluginProperties properties = new FoundDiamondsPluginProperties(propertiesFile);
		properties.load();
                
                trapblockadmin = properties.getBoolean("TrapBlockAdminAlerts", false);
                waittime = properties.getInteger("WaitTimeBetweenBroadcasts", 20);
                opstxt = properties.getBoolean("TreatOPSAsFD.Admin", true);
		randomitems = properties.getBoolean("RandomAwardsForFindingOres", true);
		RandomItem1= properties.getInteger("RandomItem1", 265);
		RandomItem2= properties.getInteger("RandomItem2", 263);
                RandomItem3= properties.getInteger("RandomItem3", 341);
		showmessage = properties.getBoolean("BroadcastWhenPlayersFindOres", true);
                broadcastmessage = properties.getString("BroadcastMessage", "@Player@ just found @BlockName@!");
                thirtysecondwait = properties.getBoolean("WaitBetweenBroadcasts", true);
		diamond = properties.getBoolean("BroadcastForDiamond", true);
		redstone = properties.getBoolean("BroadcastForRedstone", false);
                mossy = properties.getBoolean("BroadcastForMossy", true);
		gold = properties.getBoolean("BroadcastForGold", true);
		iron = properties.getBoolean("BroadcastForIron", false);
		lupuslazuli = properties.getBoolean("BroadcastForLapis", true);
		diamondadmin = properties.getBoolean("DiamondAdminAlerts", false);
		redstoneadmin = properties.getBoolean("RedstoneAdminAlerts", false);
		goldadmin = properties.getBoolean("GoldAdminAlerts", false);
		ironadmin = properties.getBoolean("IronAdminAlerts", false);
		lupuslazuliadmin = properties.getBoolean("LapisAdminAlert", false);
		logging = properties.getBoolean("LogOreFinding", false);
                kickontrapbreak = properties.getBoolean("KickOnTrapBreak", true);
                banontrapbreak = properties.getBoolean("BanOnTrapBreak", false);
                
		properties.save("===[FoundDiamonds] Configuration===");
	}	
}