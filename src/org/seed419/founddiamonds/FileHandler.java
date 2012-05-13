package org.seed419.founddiamonds;

import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.seed419.founddiamonds.listeners.BlockListener;

import java.io.*;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: seed419
 * Date: 5/12/12
 * Time: 7:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileHandler {


    private FoundDiamonds fd;
    private WorldManager wm;
    private BlockListener bl;
    private static File logs;
    private File traps;
    private static File cleanLog;
    private File configFile;
    private File placed;
    private boolean printed = false;


    public FileHandler(FoundDiamonds f, WorldManager wm, BlockListener bl) {
        this.fd = f;
        this.wm = wm;
        this.bl = bl;
    }

    /*For the love of FUCK do not change this*/
    public void initFileVariables() {
        logs = new File(fd.getDataFolder(), "log.txt");
        traps = new File(fd.getDataFolder(), ".traps");
        placed = new File(fd.getDataFolder(), ".placed");
        configFile = new File(fd.getDataFolder(), "config.yml");
        cleanLog = new File(fd.getDataFolder(), "cleanlog.txt");
    }

    public void checkFiles() {
        if (!fd.getDataFolder().exists()) {
            try {
                boolean success = fd.getDataFolder().mkdirs();
                if (!success) {
                    fd.getLog().severe(MessageFormat.format("[{0}] Couldn't create plugins/FoundDiamonds folder", fd.getPluginName()));
                    fd.getServer().getPluginManager().disablePlugin(fd);
                }
            } catch (Exception ex) {
                fd.getLog().severe(MessageFormat.format("[{0}] Couldn't create plugins/FoundDiamonds folder", fd.getPluginName()));
                fd.getServer().getPluginManager().disablePlugin(fd);
            }
        }
        if (!logs.exists()) {
            try {
                boolean success = logs.createNewFile();
                if (!success) {
                    fd.getLog().severe(MessageFormat.format("[{0}] Couldn't create plugins/FoundDiamonds/log.txt", fd.getPluginName()));
                }
            } catch (Exception ex) {
                fd.getLog().severe(MessageFormat.format("[{0}] Unable to create log file, {1}", fd.getPluginName(), ex));
            }
        }
        if (traps.exists()) {
            readBlocksFromFile(traps, fd.getTrapBlocks());
        }
        if (placed.exists()) {
            readBlocksFromFile(placed, bl.getCantAnnounce());
        }
        if (!configFile.exists()) {
            fd.getConfig().options().copyDefaults(true);
            fd.saveConfig();
            wm.addAllWorlds();
        } else {
            loadYaml();
        }
        if (fd.getConfig().getBoolean(Config.cleanLog)) {
            try {
                cleanLog.createNewFile();
            } catch (IOException ex) {
                fd.getLog().severe(MessageFormat.format("[{0}] Unable to create log file, {1}", fd.getPluginName(), ex));
                Logger.getLogger(FoundDiamonds.class.getName()).log(Level.SEVERE, "Couldn't create clean log file, ", ex);
            }
        }
    }

    public boolean writeBlocksToFile(File file, Collection<Location> blockList, String info, String info2) {
        if (blockList.size() > 0) {
            if (fd.getDataFolder().exists()) {
                PrintWriter out = null;
                try {
                    boolean success = file.createNewFile();
                    if (!success) {
                        fd.getLog().severe(MessageFormat.format("[{0}] Couldn't create file to store blocks in", fd.getPluginName(), file.getName()));
                        return false;
                    }
                    out =  new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
                    out.write("# " + info);
                    out.println();
                    out.write("# " + info2);
                    out.println();
                    for (Location m : blockList) {
                        out.write(m.getWorld().getName() + ";" + m.getX() + ";" + m.getY() + ";" + m.getZ());
                        out.println();
                    }
                } catch (IOException ex) {
                    fd.getLog().severe(MessageFormat.format("[{0}] Error writing blocks to file!", fd.getPluginName(), file.getName()));
                } finally {
                    close(out);
                }
                return true;
            } else {
                if (!printed) {
                    fd.getLog().warning(MessageFormat.format("[{0}] Plugin folder not found.  Did you delete it?", fd.getPluginName()));
                    printed = true;
                }
                return false;
            }
        } else {
            if (file.exists()) {
                file.delete();
            }
            return true;
        }
    }

    public void readBlocksFromFile(File file, Collection<Location> list) {
        BufferedReader b = null;
        try {
            b = new BufferedReader(new FileReader(file));
            String strLine = b.readLine();
            while (strLine != null) {
                if (!strLine.startsWith("#")) {
                    try {
                        String[] fs = strLine.split(";");
                        Location lo = new Location(fd.getServer().getWorld(fs[0]), Double.parseDouble(fs[1]),
                                Double.parseDouble(fs[2]), Double.parseDouble(fs[3]));
                        list.add(lo);
                    } catch (Exception ex) {
                        fd.getLog().severe(MessageFormat.format("[{0}] Invalid block in file.  Please delete the FoundDiamonds folder.", fd.getPluginName()));
                    }
                }
                strLine = b.readLine();
            }
        } catch (Exception ex) {
            fd.getLog().severe(MessageFormat.format("[{0}] Unable to read blocks from file, {1}", fd.getPluginName(), ex));
        } finally {
            close(b);
        }
    }

    /*
    * Configuration File
    */
    public void loadYaml() {
        try {
            fd.getConfig().options().copyDefaults(true);
            fd.getConfig().load(configFile);
        } catch (FileNotFoundException ex) {
            fd.getLog().severe(MessageFormat.format("[{0}] Couldn't find config.yml {1}", fd.getPluginName(), ex));
        } catch (IOException ex) {
            fd.getLog().severe(MessageFormat.format("[{0}] Unable to load configuration file {1}", fd.getPluginName(), ex));
        } catch (InvalidConfigurationException ex) {
            fd.getLog().severe(MessageFormat.format("[{0}] Unable to load configuration file {1}", fd.getPluginName(), ex));
        }
    }

    //TODO
    public void saveLists() {

    }

    public static File getLogFile() {
        return logs;
    }

    public static File getCleanLog() {
        return cleanLog;
    }

    public File getTrapsFile() {
        return traps;
    }

    public File getPlacedFile() {
        return placed;
    }

    public static void close(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ex) {
                Logger.getLogger(FoundDiamonds.class.getName()).log(Level.SEVERE, "Couldn't close a stream, ", ex);
            }
        }
    }


}
