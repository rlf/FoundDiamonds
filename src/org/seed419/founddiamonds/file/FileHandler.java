package org.seed419.founddiamonds.file;

import org.bukkit.Location;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.handlers.TrapHandler;
import org.seed419.founddiamonds.handlers.WorldHandler;
import org.seed419.founddiamonds.listeners.BlockPlaceListener;

import java.io.*;
import java.text.MessageFormat;
import java.util.Collection;

import static org.seed419.founddiamonds.FoundDiamonds.getLog;


public class FileHandler {


    private FoundDiamonds fd;
    private WorldHandler wm;
    private BlockPlaceListener bpl;
    private static File logs;
    private File traps;
    private static File cleanLog;
    //private File configFile;
    private FileUtils fileUtils;
    private File placed;
    private TrapHandler trap;
    private boolean printed = false;


    public FileHandler(FoundDiamonds f, WorldHandler wm, BlockPlaceListener bpl, TrapHandler trap, FileUtils fileUtils) {
        this.fd = f;
        this.wm = wm;
        this.trap = trap;
        this.bpl = bpl;
        this.fileUtils = fileUtils;
    }

    /*For the love of FUCK do not change this*/
    public void initFileVariables() {
        logs = new File(fd.getDataFolder(), "log.txt");
        traps = new File(fd.getDataFolder(), ".traps");
        placed = new File(fd.getDataFolder(), ".placed");
        //configFile = new File(fd.getDataFolder(), "org/seed419/founddiamonds/resources/config.yml");
        cleanLog = new File(fd.getDataFolder(), "cleanlog.txt");
    }

    public void checkFiles() {
        boolean firstrun = false;
        if (!fd.getDataFolder().exists()) {
            firstrun = true;
            try {
                verfiyFileCreation(fd.getDataFolder().mkdirs(), fd.getDataFolder());
            } catch (Exception ex) {
                getLog().severe(MessageFormat.format("Couldn't create plugins/FoundDiamonds folder {0}", ex));
                fd.getServer().getPluginManager().disablePlugin(fd);
            }
        }
        if (!logs.exists()) {
            try {
                verfiyFileCreation(logs.createNewFile(), logs);
            } catch (Exception ex) {
                getLog().severe(MessageFormat.format("Unable to create log file, {0}", ex));
            }
        }
        if (traps.exists()) {
            readBlocksFromFile(traps, trap.getTrapBlocks());
        }
        if (placed.exists() && !fd.getConfig().getBoolean(Config.mysqlEnabled)) {
            readBlocksFromFile(placed, bpl.getFlatFilePlacedBlocks());
        }
        fd.getConfig().options().copyDefaults(true);
        if (firstrun) {
            wm.addAllWorlds();
        }
        fd.saveConfig();
        if (fd.getConfig().getBoolean(Config.cleanLog)) {
            try {
                verfiyFileCreation(cleanLog.createNewFile(), cleanLog);
            } catch (IOException ex) {
                getLog().severe(MessageFormat.format("Unable to create log file, {0}", ex));
            }
        }
    }

    public void verfiyFileCreation(boolean b, File file) {
        if (!b) {
            getLog().severe(MessageFormat.format("Failed to create {0}!", file.getName()));
        }
    }

    public boolean writeBlocksToFile(File file, Collection<Location> blockList, String info) {
        if (blockList.size() > 0) {
            if (fd.getDataFolder().exists()) {
                PrintWriter out = null;
                try {
                    if (!file.exists()) {
                        boolean success = file.createNewFile();
                        if (!success) {
                            getLog().severe(MessageFormat.format("[{0}] Couldn't create file to store blocks in", file.getName()));
                        }
                    }
                    try {
                        out =  new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
                        out.write("# " + info);
                        out.println();
                        for (Location m : blockList) {
                            out.write(m.getWorld().getName() + ";" + m.getX() + ";" + m.getY() + ";" + m.getZ());
                            out.println();
                        }
                    } catch (IOException ex) {
                        getLog().severe(MessageFormat.format("Error writing blocks to {0}!", file.getName()));
                    } finally {
                        fileUtils.close(out);
                    }
                    return true;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return false;
                }
            } else {
                if (!printed) {
                    getLog().warning("Plugin folder not found.  Did you delete it?");
                    printed = true;
                }
                return false;
            }
        } else {
            if (file.exists()) {
                boolean deletion = file.delete();
                if (deletion) {
                    getLog().info("Deleted an empty unused FoundDiamonds file, for the sake of cleanliness");
                }
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
                        getLog().severe(MessageFormat.format("Invalid block in file.  Please delete {0}", file.getName()));
                    }
                }
                strLine = b.readLine();
            }
        } catch (Exception ex) {
            getLog().severe(MessageFormat.format("Problem reading from {0}.  Please delete it.", file.getName()));
        } finally {
            fileUtils.close(b);
        }
    }

    public static File getLogFile() {
        return logs;
    }

    public static File getCleanLog() {
        return cleanLog;
    }

    public void saveFlatFileData() {
        String info = "This file stores your trap block locations.";
        boolean temp = writeBlocksToFile(traps, trap.getTrapBlocks(), info);
        boolean temp2 = true;
        if (!fd.getConfig().getBoolean(Config.mysqlEnabled)) {
            String info5 = "This file stores blocks that won't be announced because players placed them.";
            temp2 = writeBlocksToFile(placed, bpl.getFlatFilePlacedBlocks(), info5);
        }
        if (temp && temp2) {
            getLog().info("Data successfully saved.");
        } else {
            getLog().warning("Couldn't save blocks to files!");
            getLog().warning("You could try deleting .placed and .traps if they exist");
        }
    }

}
