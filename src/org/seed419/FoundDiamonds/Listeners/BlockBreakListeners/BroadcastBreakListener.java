package org.seed419.FoundDiamonds.Listeners.BlockBreakListeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.seed419.FoundDiamonds.Config;
import org.seed419.FoundDiamonds.EventInformation;
import org.seed419.FoundDiamonds.FoundDiamonds;
import org.seed419.FoundDiamonds.Node;

/**
 * Created with IntelliJ IDEA.
 * User: seed419
 * Date: 5/1/12
 * Time: 2:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class BroadcastBreakListener implements Listener {


    private FoundDiamonds fd;
    private boolean debug;


    public BroadcastBreakListener(FoundDiamonds instance) {
        this.fd = instance;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        //Check for debug mode
        debug = fd.getConfig().getBoolean(Config.debug);
        Material mat = event.getBlock().getType();

        //Prevent mcMMO's superbreaker from re-announcing.
        if (event.getEventName().equalsIgnoreCase("FakeBlockBreakEvent")) {
            return;
        }

        Node node = Node.getNodeByMaterial(fd.getBroadcastedBlocks(), mat);

        if (node == null) {
            return;
        }

        EventInformation ei = new EventInformation(event, node);


    }
}
