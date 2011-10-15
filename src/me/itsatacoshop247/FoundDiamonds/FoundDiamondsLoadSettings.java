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
	
	public static void loadMain(){
		String propertiesFile = FoundDiamonds.maindirectory + "FoundDiamonds.properties";
		FoundDiamondsPluginProperties properties = new FoundDiamondsPluginProperties(propertiesFile);
		properties.load();
                
		randomitems = properties.getBoolean("Random awards for finding ores?: ", true);
		RandomItem1= properties.getInteger("RandomItem1: ", 265);
		RandomItem2= properties.getInteger("RandomItem2: ", 263);
                RandomItem3= properties.getInteger("RandomItem3: ", 341);
		showmessage = properties.getBoolean("Broadcast when players find ores?: ", true);
                broadcastmessage = properties.getString("Broadcast_Message: ", "@Player@ just found @BlockName@!");
                thirtysecondwait = properties.getBoolean("20 seconds wait between broadcasts?: ", true);
		diamond = properties.getBoolean("Broadcast for diamond: ", true);
		redstone = properties.getBoolean("Broadcast for redstone: ", false);
		gold = properties.getBoolean("Broadcast for gold: ", true);
		iron = properties.getBoolean("Broadcast for iron: ", false);
		lupuslazuli = properties.getBoolean("Broadcast for lapis: ", true);
		diamondadmin = properties.getBoolean("Diamond admin alert: ", false);
		redstoneadmin = properties.getBoolean("Redstone admin alert: ", false);
		goldadmin = properties.getBoolean("Gold admin alert: ", false);
		ironadmin = properties.getBoolean("Iron admin alert: ", false);
		lupuslazuliadmin = properties.getBoolean("Lapis admin alert: ", false);
		logging = properties.getBoolean("Log_Ore_Finding?: ", false);
		properties.save("===[FoundDiamonds] Configuration===");
	}	
}