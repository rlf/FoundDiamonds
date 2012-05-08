package org.seed419.FoundDiamonds.Listeners.BlockBreakListeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.seed419.FoundDiamonds.Config;
import org.seed419.FoundDiamonds.FoundDiamonds;

/**
 * Created with IntelliJ IDEA.
 * User: seed419
 * Date: 5/1/12
 * Time: 4:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class TrapBlockListener implements Listener {


    private FoundDiamonds fd;
    private boolean debug;


    public TrapBlockListener(FoundDiamonds instance) {
        this.fd = instance;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        //Check for debug mode
        debug = fd.getConfig().getBoolean(Config.debug);

        //Prevent mcMMO's superbreaker from re-announcing.
        if (event.getEventName().equalsIgnoreCase("FakeBlockBreakEvent")) {
            return;
        }

        if (fd.getTrapBlocks().contains(event.getBlock().getLocation())) {

        }

    }
}
