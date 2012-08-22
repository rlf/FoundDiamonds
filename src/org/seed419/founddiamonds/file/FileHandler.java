package org.seed419.founddiamonds.file;

import org.bukkit.Location;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.handlers.TrapHandler;
import org.seed419.founddiamonds.handlers.WorldHandler;
import org.seed419.founddiamonds.listeners.BlockPlaceListener;

import java.io.*;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;


public class FileHandler {


    private FoundDiamonds fd;
    private WorldHandler wm;
    private BlockPlaceListener bpl;
    private static File logs;
    private File traps;
    private static File cleanLog;
    private File configFile;
    private File placed;
    private TrapHandler trap;
    private boolean printed = false;


    public FileHandler(FoundDiamonds f, WorldHandler wm, BlockPlaceListener bpl, TrapHandler trap) {
        this.fd = f;
        this.wm = wm;
        this.trap = trap;
        this.bpl = bpl;
    }

    /*For the love of FUCK do not change this*/
    public void initFileVariables() {
        logs = new File(fd.getDataFolder(), "log.txt");
        traps = new File(fd.getDataFolder(), ".traps");
        placed = new File(fd.getDataFolder(), ".placed");
        configFile = new File(fd.getDataFolder(), "org/seed419/founddiamonds/resources/config.yml");
        cleanLog = new File(fd.getDataFolder(), "cleanlog.txt");
    }

    public void checkFiles() {
        boolean firstrun = false;
        if (!fd.getDataFolder().exists()) {
            firstrun = true;
            try {
                verfiyFileCreation(fd.getDataFolder().mkdirs(), "main plugin folder");
            } catch (Exception ex) {
                fd.getLog().severe(MessageFormat.format("[{0}] Couldn't create plugins/FoundDiamonds folder", fd.getPluginName()));
                fd.getServer().getPluginManager().disablePlugin(fd);
            }
        }
        if (!logs.exists()) {
            try {
                verfiyFileCreation(logs.createNewFile(), "logs.txt");
            } catch (Exception ex) {
                fd.getLog().severe(MessageFormat.format("[{0}] Unable to create log file, {1}", fd.getPluginName(), ex));
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
                verfiyFileCreation(cleanLog.createNewFile(), "cleanlog.txt");
            } catch (IOException ex) {
                fd.getLog().severe(MessageFormat.format("[{0}] Unable to create log file, {1}", fd.getPluginName(), ex));
                Logger.getLogger(FoundDiamonds.class.getName()).log(Level.SEVERE, "Couldn't create clean log file, ", ex);
            }
        }
    }

    public void verfiyFileCreation(boolean b, String name) {
        if (!b) {
            fd.getLog().severe(MessageFormat.format("[{0}] Failed to create " + name + "!", fd.getPluginName()));
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
                            fd.getLog().severe(MessageFormat.format("[{0}] Couldn't create file to store blocks in", fd.getPluginName()));
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
                        fd.getLog().severe(MessageFormat.format("[{0}] Error writing blocks to file!", fd.getPluginName(), file.getName()));
                    } finally {
                        close(out);
                    }
                    return true;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return false;
                }
            } else {
                if (!printed) {
                    fd.getLog().warning(MessageFormat.format("[{0}] Plugin folder not found.  Did you delete it?", fd.getPluginName()));
                    printed = true;
                }
                return false;
            }
        } else {
            if (file.exists()) {
                boolean deletion = file.delete();
                if (deletion) {
                    fd.getLog().info(MessageFormat.format("[{0}] Deleted an empty, obsolete file.", fd.getPluginName()));
                    fd.getLog().info(MessageFormat.format("[{0}] What a kind and thoughtful developer that seed419 guy is", fd.getPluginName()));
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
