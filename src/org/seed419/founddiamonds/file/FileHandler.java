package org.seed419.founddiamonds.file;

import org.bukkit.Location;
import org.seed419.founddiamonds.FoundDiamonds;

import java.io.*;
import java.text.MessageFormat;
import java.util.Collection;

public class FileHandler {


    //todo this clearly still need testing and refactoring
    private FoundDiamonds fd;
    private static File logs;
    private File traps;
    private static File cleanLog;
    //private File configFile;
    private File placed;
    private boolean printed = false;


    public FileHandler(FoundDiamonds f) {
        this.fd = f;
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
                fd.getLog().severe(MessageFormat.format("Couldn't create plugins/FoundDiamonds folder {0}", ex));
                fd.getServer().getPluginManager().disablePlugin(fd);
            }
        }
        if (!logs.exists()) {
            try {
                verfiyFileCreation(logs.createNewFile(), logs);
            } catch (Exception ex) {
                fd.getLog().severe(MessageFormat.format("Unable to create log file, {0}", ex));
            }
        }
        if (traps.exists()) {
            readBlocksFromFile(traps, fd.getTrapHandler().getTrapBlocks());
        }
        if (placed.exists() && !fd.getConfig().getBoolean(Config.mysqlEnabled)) {
            readBlocksFromFile(placed, fd.getBlockPlaceListener().getFlatFilePlacedBlocks());
        }
        fd.getConfig().options().copyDefaults(true);
        if (firstrun) {
            fd.getWorldHandler().addAllWorlds();
        }
        fd.saveConfig();
        if (fd.getConfig().getBoolean(Config.cleanLog)) {
            try {
                verfiyFileCreation(cleanLog.createNewFile(), cleanLog);
            } catch (IOException ex) {
                fd.getLog().severe(MessageFormat.format("Unable to create log file, {0}", ex));
            }
        }
    }

    public void verfiyFileCreation(boolean b, File file) {
        if (!b) {
            fd.getLog().severe(MessageFormat.format("Failed to create {0}!", file.getName()));
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
                            fd.getLog().severe(MessageFormat.format("[{0}] Couldn't create file to store blocks in", file.getName()));
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
                        fd.getLog().severe(MessageFormat.format("Error writing blocks to {0}!", file.getName()));
                    } finally {
                        fd.getFileUtils().close(out);
                    }
                    return true;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return false;
                }
            } else {
                if (!printed) {
                    fd.getLog().warning("Plugin folder not found.  Did you delete it?");
                    printed = true;
                }
                return false;
            }
        } else {
            if (file.exists()) {
                boolean deletion = file.delete();
                if (deletion) {
                    fd.getLog().info("Deleted an empty unused FoundDiamonds file, for the sake of cleanliness");
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
                        fd.getLog().severe(MessageFormat.format("Invalid block in file.  Please delete {0}", file.getName()));
                    }
                }
                strLine = b.readLine();
            }
        } catch (Exception ex) {
            fd.getLog().severe(MessageFormat.format("Problem reading from {0}.  Please delete it.", file.getName()));
        } finally {
            fd.getFileUtils().close(b);
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
        boolean temp = writeBlocksToFile(traps, fd.getTrapHandler().getTrapBlocks(), info);
        boolean temp2 = true;
        if (!fd.getConfig().getBoolean(Config.mysqlEnabled)) {
            String info5 = "This file stores blocks that won't be announced because players placed them.";
            temp2 = writeBlocksToFile(placed, fd.getBlockPlaceListener().getFlatFilePlacedBlocks(), info5);
        }
        if (temp && temp2) {
            fd.getLog().info("Data successfully saved.");
        } else {
            fd.getLog().warning("Couldn't save blocks to files!");
            fd.getLog().warning("You could try deleting .placed and .traps if they exist");
        }
    }

}
