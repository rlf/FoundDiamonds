package org.seed419.founddiamonds.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.Node;
import org.seed419.founddiamonds.file.Config;

public class AnnouncementListener implements Listener  {


    private FoundDiamonds fd;


    public AnnouncementListener(FoundDiamonds fd) {
        this.fd = fd;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {

        final Player player = event.getPlayer();

        if (!fd.getWorldHandler().isEnabledWorld(player)) { return; }

        final Location loc = event.getBlock().getLocation();

        if (!fd.getWorldHandler().isValidGameMode(player)) { return; }

        if (!fd.getBlockCounter().isAnnounceable(loc)) {
            fd.getBlockCounter().removeAnnouncedOrPlacedBlock(loc);
            return;
        }

        final Material mat = event.getBlock().getType();

        if (fd.getPermissions().hasMonitorPerm(player)) {
            Node adminNode = Node.getNodeByMaterial(fd.getListHandler().getAdminMessageBlocks(), mat);
            if (adminNode != null) {
                fd.getAdminMessageHandler().sendAdminMessage(event, adminNode, player);
                return;
            }
        }

        if (fd.getPermissions().hasBroadcastPerm(player)) {
            Node broadcastNode = Node.getNodeByMaterial(fd.getListHandler().getBroadcastedBlocks(), mat);
            if (broadcastNode != null) {
                fd.getBroadcastHandler().handleBroadcast(event, broadcastNode, player);
            }
        }

        // Worry about logging here.  Right now this only logs diamond ore
        //TODO won't get logged if admin message block
        if (mat == Material.DIAMOND_ORE) {
            if (fd.getConfig().getBoolean(Config.logDiamondBreaks)) {
                fd.getLoggingHandler().handleLogging(event.getPlayer(), event.getBlock(), false, false, false);
            }
        }
        //reset message checks after successful event
        fd.getAdminMessageHandler().getRecievedAdminMessage().clear();
    }
}