package me.itsatacoshop247.FoundDiamonds;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FoundDiamonds extends JavaPlugin {
	static final String maindirectory = "plugins/FoundDiamonds/";
	static File Blocks = new File(maindirectory + "Blocks.txt");
	static File logs = new File(maindirectory + "logs.txt");
        
	private static final Logger log = Logger.getLogger("Minecraft");
	private final FoundDiamondsBlockListener blocklistener = new FoundDiamondsBlockListener(this);
	public String pName;

    @Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_BREAK, blocklistener,Event.Priority.Normal, this);
		log.info("[FoundDiamonds]: Enabled");
		PluginDescriptionFile pdf = this.getDescription();
		pName = pdf.getName();
		new File(maindirectory).mkdir();
		if(Blocks.exists())
			try {
				Blocks.createNewFile();
			} catch (IOException e) {
                            log.log(Level.SEVERE, "[FoundDiamonds]: Unable to create configuration file!", e);
			}
		if(!logs.exists())
			try{
				logs.createNewFile();
			}catch (IOException e) {
                            log.log(Level.SEVERE, "[FoundDiamonds]: Unable to create log file!", e);
			}
		//setupPermissions();
		FoundDiamondsLoadSettings.loadMain();
	}

    @Override
	public void onDisable() {
		log.info("[FoundDiamonds]: Disabled");
               
                    }
}
