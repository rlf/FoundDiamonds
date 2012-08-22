package org.seed419.founddiamonds.listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.seed419.founddiamonds.*;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.handlers.*;
import org.seed419.founddiamonds.sql.MySQL;
import org.seed419.founddiamonds.util.Format;
import org.seed419.founddiamonds.util.PluginUtils;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class BlockBreakListener implements Listener  {


    private FoundDiamonds fd;
    private MySQL mysql;
    private TrapHandler trap;
    private LoggingHandler logging;
    private BlockPlaceListener bpl;
    private PotionHandler potions;
    private ItemHandler items;
    private static final Logger log = Logger.getLogger("FoundDiamonds");
    private HashSet<Location> cantAnnounce = new HashSet<Location>();
    private List<Player> recievedAdminMessage = new LinkedList<Player>();
    private boolean consoleReceived;
    private boolean debug;


    public BlockBreakListener(FoundDiamonds instance, MySQL mysql, TrapHandler trap, LoggingHandler logging, BlockPlaceListener bpl, PotionHandler potions, ItemHandler items) {
        this.mysql = mysql;
        this.fd = instance;
        this.trap = trap;
        this.logging = logging;
        this.bpl = bpl;
        this.potions = potions;
        this.items = items;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        debug = fd.getConfig().getBoolean(Config.debug);

        if (!WorldHandler.isEnabledWorld(event.getPlayer())) {
            if (debug) {log.info(FoundDiamonds.getDebugPrefix() + " Cancelling: User is not in a FD enabled world.");}
            return;
        }

        if (event.getEventName().equalsIgnoreCase("FakeBlockBreakEvent")) { return; }

        if (trap.isTrapBlock(event.getBlock().getLocation())) {
            trap.handleTrapBlockBreak(event);
            return;
        }

        if (!isValidGameMode(event.getPlayer())) {
            if (debug) { log.info(FoundDiamonds.getDebugPrefix() + " Cancelling: User is in creative mode."); }
            return;
        }

        Material mat = event.getBlock().getType();

        if (Permissions.hasPerms(event.getPlayer(), "fd.broadcast")) {
            if (!isAnnounceable(event.getBlock().getLocation())) {
                removeAnnouncedOrPlacedBlock(event.getBlock().getLocation());
                if (debug) {log.info(FoundDiamonds.getDebugPrefix() + " Cancelling: Block already announced or placed.  Removing block from memory.");}
                return;
            }
            Node broadcastNode = Node.getNodeByMaterial(ListHandler.getBroadcastedBlocks(), mat);
            EventInformation broadcastEvent;
            if (broadcastNode != null) {
                    broadcastEvent = new EventInformation(this, event, broadcastNode, true);
                    handleBroadcast(broadcastEvent);
            }
        }

        //Check if block is admin message material
        Node adminNode = Node.getNodeByMaterial(ListHandler.getAdminMessageBlocks(), mat);
        if (adminNode != null) {
                EventInformation adminEvent = new EventInformation(this, event, adminNode, true);
                sendAdminMessage(adminEvent);
        }

        // Worry about logging here.  Right now this only logs diamond ore
        if (mat == Material.DIAMOND_ORE) {
            if (fd.getConfig().getBoolean(Config.logDiamondBreaks)) {
                logging.handleLogging(event.getPlayer(), event.getBlock(), false, false, false);
            }
        }

        //reset message checks after successful event
        recievedAdminMessage.clear();
        consoleReceived = false;
    }



    public void removeAnnouncedOrPlacedBlock(Location loc) {
        if (fd.getConfig().getBoolean(Config.mysqlEnabled)) {
            mysql.removePlacedBlock(loc);
        } else if (bpl.getFlatFilePlacedBlocks().contains(loc)) {
            bpl.getFlatFilePlacedBlocks().remove(loc);
        }
        if (cantAnnounce.contains(loc)) {
            cantAnnounce.remove(loc);
        }
    }




    /*Admin messages*/
    private void sendAdminMessage(EventInformation adminEvent) {
        String adminMessage = FoundDiamonds.getAdminPrefix() + " " + ChatColor.YELLOW + adminEvent.getPlayer().getName() +
                ChatColor.DARK_RED + " just found " + adminEvent.getColor() +
                (adminEvent.getTotal() == 500 ? "over 500 " :String.valueOf(adminEvent.getTotal())) + " " +
                Format.getFormattedName(adminEvent.getMaterial(), adminEvent.getTotal());
        fd.getServer().getConsoleSender().sendMessage(adminMessage);
        consoleReceived = true;
        for (Player y : fd.getServer().getOnlinePlayers()) {
            if (Permissions.hasPerms(y, "fd.admin") && y != adminEvent.getPlayer()) {
                y.sendMessage(adminMessage);
                recievedAdminMessage.add(y);
                if (debug) {log.info(FoundDiamonds.getDebugPrefix() + "Sent admin message to " + y.getName());}
            } else {
                if (debug) {log.info(FoundDiamonds.getDebugPrefix() + y.getName() + " doesn't have the permission fd.admin");}
            }
        }
    }

    private void sendLightAdminMessage(EventInformation ei, int lightLevel) {
        String lightAdminMessage = FoundDiamonds.getAdminPrefix() + " " + ChatColor.YELLOW + ei.getPlayer().getName() +
                ChatColor.GRAY +" was denied mining " + ei.getColor() +
                Format.getFormattedName(ei.getMaterial(), 1) + ChatColor.GRAY + " at" + " light level "
                + ChatColor.WHITE +  lightLevel;
        fd.getServer().getConsoleSender().sendMessage(lightAdminMessage);
        for (Player y : fd.getServer().getOnlinePlayers()) {
            if (Permissions.hasPerms(y, "fd.admin")) {
                if (y != ei.getPlayer()) {
                    y.sendMessage(lightAdminMessage);
                    if (debug) {log.info(FoundDiamonds.getDebugPrefix() + "Sent admin message to " + y.getName());}
                } else {
                    if (debug) {log.info(FoundDiamonds.getDebugPrefix() +y.getName() + " was not sent an admin message because it was them who was denied mining.");}
                }
            } else {
                if (debug) {log.info(FoundDiamonds.getDebugPrefix() + y.getName() + " doesn't have the permission fd.admin");}
            }
        }
    }

    /*Broadcasts*/
    // This looks sloppy but it needs to be in this order for the output.  Whatever.
    private void handleBroadcast(EventInformation ei) {
        if (ei.getMaterial() == Material.DIAMOND_ORE) {
            if (fd.getConfig().getBoolean(Config.potionsForFindingDiamonds)) {
                int randomInt = (int) (Math.random()*100);
                if (randomInt <= fd.getConfig().getInt(Config.chanceToGetPotion)) {
                    int randomNumber = (int)(Math.random()*225);
                    if (randomNumber >= 0 && randomNumber <= 225) {
                        potions.handleRandomPotions(ei.getPlayer(), randomNumber);
                    }
                }
            }
        }
        broadcastFoundBlock(ei);
        if (ei.getMaterial() == Material.DIAMOND_ORE) {
            if (fd.getConfig().getBoolean(Config.itemsForFindingDiamonds)) {
                int randomInt = (int) (Math.random()*100);
                if (randomInt <= fd.getConfig().getInt(Config.chanceToGetItem)) {
                    int randomNumber = (int)(Math.random()*150);
                    if (randomNumber >= 0 && randomNumber <= 150) {
                        items.handleRandomItems(ei.getPlayer(), randomNumber);
                    }
                }
            }
        }
    }

    private void broadcastFoundBlock(EventInformation ei) {
        String playerName = getBroadcastName(ei.getPlayer());
        String matName = Format.getFormattedName(ei.getMaterial(), ei.getTotal());
        String message = fd.getConfig().getString(Config.bcMessage).replace("@Prefix@", FoundDiamonds.getPrefix() + ei.getColor()).replace("@Player@",
                playerName +  (fd.getConfig().getBoolean(Config.useOreColors) ? ei.getColor() : "")).replace("@Number@",
                (ei.getTotal() == 500 ? "over 500" :String.valueOf(ei.getTotal()))).replace("@BlockName@", matName);
        String formatted = PluginUtils.customTranslateAlternateColorCodes('&', message);

        //Prevent redunant output to the console if an admin message was already sent.
        if (!consoleReceived) {
            fd.getServer().getConsoleSender().sendMessage(formatted);
        }

        for (Player x : fd.getServer().getOnlinePlayers()) {
            if (Permissions.hasPerms(x,"fd.broadcast") && WorldHandler.isEnabledWorld(x)) {
                if (!recievedAdminMessage.contains(x)) {
                    x.sendMessage(formatted);
                    if (debug) {log.info(FoundDiamonds.getDebugPrefix() + "Sent broadcast to " + x.getName());}
                } else if (debug) {
                    log.info(FoundDiamonds.getDebugPrefix() + x.getName() + "recieved an admin message already, so not broadcasting to " + x.getName());
                }
            } else {
                if (debug) {
                    if (!x.hasPermission("fd.broadcast")) {
                        log.info(FoundDiamonds.getDebugPrefix() + x.getName() + " does not have permission 'fd.broadcast'.  Not broadcasting to " + x.getName());
                    }
                    if (!WorldHandler.isEnabledWorld(x)) {
                        log.info(FoundDiamonds.getDebugPrefix() + x.getName() + " is not in an enabled world, so not broadcasting to  " + x.getName());
                    }
                }
            }
        }

        //write to log if cleanlogging.
        if (fd.getConfig().getBoolean(Config.cleanLog)) {
            logging.writeToCleanLog(ei, playerName);
        }
    }

    private String getBroadcastName(Player player) {
        if (fd.getConfig().getBoolean(Config.useNick)) {
            return player.getDisplayName();
        } else {
            return player.getName();
        }
    }



    /*
     * Other Methods
     */
    private boolean isValidGameMode(Player player) {
        return !((player.getGameMode() == GameMode.CREATIVE) && (fd.getConfig().getBoolean(Config.disableInCreative)));
    }

    public boolean isAnnounceable(Location loc) {
        if (fd.getConfig().getBoolean(Config.mysqlEnabled)) {
            return !mysql.blockWasPlaced(loc) && !cantAnnounce.contains(loc);
        } else {
            return !cantAnnounce.contains(loc) && !bpl.getFlatFilePlacedBlocks().contains(loc);
        }
    }

    public HashSet<Location> getCantAnnounce() {
        return cantAnnounce;
    }



    /*
     * Light Methods
     */
    public boolean blockSeesNoLight(EventInformation ei) {
        double percentage = Double.parseDouble(fd.getConfig().getString(Config.percentOfLightRequired).replaceAll("%", ""));
        double levelToDisableAt = percentage / 15.0;
        DecimalFormat dform = new DecimalFormat("#.##");
        String formattedLightLevel = dform.format(levelToDisableAt);
        int lightLevel = 0;
        int highestLevel = 0;
        for (BlockFace y : BlockFace.values()) {
            lightLevel = ei.getBlock().getRelative(y).getLightLevel();
            if (lightLevel > highestLevel) {
                highestLevel = lightLevel;
            }
            if (lightLevel > levelToDisableAt) {
                if (debug) {
                    log.info(FoundDiamonds.getDebugPrefix() + " " + ei.getPlayer().getName() + " just mined "
                            + Format.getFormattedName(ei.getMaterial(), 1) + " at light level " + highestLevel
                            + ".  We are disabling ore mining at light level " + formattedLightLevel + " or "
                            + percentage + "%");
                }
                return false;
            }
        }
        sendLightAdminMessage(ei, highestLevel);
        if ((fd.getConfig().getBoolean(Config.logLightLevelViolations))) {
            logging.logLightLevelViolation(ei, highestLevel);
        }
        if (debug) {
            log.info(FoundDiamonds.getDebugPrefix() + ei.getPlayer().getName() + " was denied mining "
                    + Format.getFormattedName(ei.getMaterial(), 1) + " at light level " + highestLevel
                    + ".  We are disabling ore mining at light level "  + formattedLightLevel + " or " + percentage
                    + "%");
        }
        return true;
    }

}