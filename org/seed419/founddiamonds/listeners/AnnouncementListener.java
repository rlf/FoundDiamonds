package org.seed419.founddiamonds.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.seed419.founddiamonds.EventInformation;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.Node;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.util.Prefix;

import java.util.HashSet;

public class AnnouncementListener implements Listener  {


    private FoundDiamonds fd;
    private HashSet<Location> cantAnnounce = new HashSet<Location>();
    private boolean consoleReceived = false;
    private boolean debug;

    //TODO refactor, call seperate classes for these events.  Still needs work.
    public AnnouncementListener(FoundDiamonds fd) {
        this.fd = fd;
    }

    /*
    Check for enabled world
    Check for creative mode
    Check to see if the block is placed, or announceable.
    Finally, broadcast the block.
     */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {

        final Player player = event.getPlayer();

        if (!fd.getWorldHandler().isEnabledWorld(player)) { return; }

        debug = fd.getConfig().getBoolean(Config.debug);
        final Location loc = event.getBlock().getLocation();

        if (!fd.getWorldHandler().isValidGameMode(player)) { return; }

        if (!isAnnounceable(loc)) {
            removeAnnouncedOrPlacedBlock(loc);
            if (debug) {fd.getLog().info(Prefix.getDebugPrefix() + "Cancelling: Block already announced or placed.");}
            return;
        }

        final Material mat = event.getBlock().getType();

        if (fd.getPermissions().hasMonitorPerm(player)) {
            Node adminNode = Node.getNodeByMaterial(fd.getListHandler().getAdminMessageBlocks(), mat);
            if (adminNode != null) {
                EventInformation adminEvent = new EventInformation(this, event, adminNode, true);
                fd.getAdminMessageHandler().sendAdminMessage(adminEvent);
                consoleReceived = true;
            }
        }

        if (fd.getPermissions().hasBroadcastPerm(player)) {
            Node broadcastNode = Node.getNodeByMaterial(fd.getListHandler().getBroadcastedBlocks(), mat);
            if (broadcastNode != null) {
                EventInformation broadcastEvent = new EventInformation(this, event, broadcastNode, true);
                fd.getBroadcastHandler().handleBroadcast(broadcastEvent, consoleReceived);
            }
        }

        // Worry about logging here.  Right now this only logs diamond ore
        if (mat == Material.DIAMOND_ORE) {
            if (fd.getConfig().getBoolean(Config.logDiamondBreaks)) {
                fd.getLoggingHandler().handleLogging(event.getPlayer(), event.getBlock(), false, false, false);
            }
        }
        //reset message checks after successful event
        fd.getAdminMessageHandler().getRecievedAdminMessage().clear();
        consoleReceived = false;
    }

    public void removeAnnouncedOrPlacedBlock(Location loc) {
        if (fd.getConfig().getBoolean(Config.mysqlEnabled)) {
            fd.getMySQL().removePlacedBlock(loc);
        } else if (fd.getBlockPlaceListener().getFlatFilePlacedBlocks().contains(loc)) {
            fd.getBlockPlaceListener().getFlatFilePlacedBlocks().remove(loc);
        }
        if (cantAnnounce.contains(loc)) {
            cantAnnounce.remove(loc);
        }
    }

    public boolean isAnnounceable(Location loc) {
        if (fd.getConfig().getBoolean(Config.mysqlEnabled)) {
            return !fd.getMySQL().blockWasPlaced(loc) && !cantAnnounce.contains(loc);
        } else {
            return !cantAnnounce.contains(loc) && !fd.getBlockPlaceListener().getFlatFilePlacedBlocks().contains(loc);
        }
    }

    public HashSet<Location> getCantAnnounce() {
        return cantAnnounce;
    }

}