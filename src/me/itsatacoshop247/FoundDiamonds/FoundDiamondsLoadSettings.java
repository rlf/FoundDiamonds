package me.itsatacoshop247.FoundDiamonds;

//import org.bukkit.ChatColor;

//import me.itsatacoshop247.FoundDiamonds.FoundDiamondsPluginProperties;

public class FoundDiamondsLoadSettings {
	static int onetofive;
	static int sixtoten;
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
	
	public static void loadMain(){
		String propertiesFile = FoundDiamonds.maindirectory + "MainConfig.properties";
		FoundDiamondsPluginProperties properties = new FoundDiamondsPluginProperties(propertiesFile);
		properties.load();
		
		onetofive= properties.getInteger("onetofive", 265);
		sixtoten= properties.getInteger("sixtoten", 263);
		showmessage = properties.getBoolean("showmmessage", true);
		randomitems = properties.getBoolean("randomitems", true);
		diamond = properties.getBoolean("diamond", true);
		redstone = properties.getBoolean("redstone", false);
		gold = properties.getBoolean("gold", true);
		iron = properties.getBoolean("iron", false);
		lupuslazuli = properties.getBoolean("lupuslazuli", true);
		diamondadmin = properties.getBoolean("diamond admin", false);
		redstoneadmin = properties.getBoolean("redstone admin", false);
		goldadmin = properties.getBoolean("gold admin", false);
		ironadmin = properties.getBoolean("iron admin", false);
		lupuslazuliadmin = properties.getBoolean("lupuslazuli admin", false);
		thirtysecondwait = properties.getBoolean("thirtysecondwait", true);
		logging = properties.getBoolean("Log_Ore_Finding?", false);
		broadcastmessage = properties.getString("Broadcast_Message", "@Player@ just found @BlockName@!");
		properties.save("===FoundDiamonds Main Configuration===");
	}	
}