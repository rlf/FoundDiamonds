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
import org.seed419.founddiamonds.EventInformation;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.Node;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.handlers.ListHandler;
import org.seed419.founddiamonds.util.Format;
import org.seed419.founddiamonds.util.PluginUtils;
import org.seed419.founddiamonds.util.Prefix;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class BlockBreakListener implements Listener  {


    private FoundDiamonds fd;
    private HashSet<Location> cantAnnounce = new HashSet<Location>();
    private List<Player> recievedAdminMessage = new LinkedList<Player>();
    private boolean consoleReceived;
    private boolean debug;

    //TODO refactor, call seperate classes for these events.
    public BlockBreakListener(FoundDiamonds fd) {
        this.fd = fd;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        debug = fd.getConfig().getBoolean(Config.debug);

        if (!fd.getWorldHandler().isEnabledWorld(event.getPlayer())) {
            if (debug) {fd.getLog().info(Prefix.getDebugPrefix() + " Cancelling: User is not in a FD enabled world.");}
            return;
        }

        if (event.getEventName().equalsIgnoreCase("FakeBlockBreakEvent")) { return; }

        if (fd.getTrapHandler().isTrapBlock(event.getBlock().getLocation())) {
            fd.getTrapHandler().handleTrapBlockBreak(event);
            return;
        }

        if (!isValidGameMode(event.getPlayer())) {
            if (debug) { fd.getLog().info(Prefix.getDebugPrefix() + " Cancelling: User is in creative mode."); }
            return;
        }

        Material mat = event.getBlock().getType();

        if (fd.getPermissions().hasPerm(event.getPlayer(), "fd.broadcast")) {
            if (!isAnnounceable(event.getBlock().getLocation())) {
                removeAnnouncedOrPlacedBlock(event.getBlock().getLocation());
                if (debug) {fd.getLog().info(Prefix.getDebugPrefix() + " Cancelling: Block already announced or placed.  Removing block from memory.");}
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
                fd.getLoggingHandler().handleLogging(event.getPlayer(), event.getBlock(), false, false, false);
            }
        }

        //reset message checks after successful event
        recievedAdminMessage.clear();
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




    /*Admin messages*/
    private void sendAdminMessage(EventInformation adminEvent) {
        String adminMessage = Prefix.getAdminPrefix() + " " + ChatColor.YELLOW + adminEvent.getPlayer().getName() +
                ChatColor.DARK_RED + " just found " + adminEvent.getColor() +
                (adminEvent.getTotal() == 500 ? "over 500 " :String.valueOf(adminEvent.getTotal())) + " " +
                Format.getFormattedName(adminEvent.getMaterial(), adminEvent.getTotal());
        fd.getServer().getConsoleSender().sendMessage(adminMessage);
        consoleReceived = true;
        for (Player y : fd.getServer().getOnlinePlayers()) {
            if (fd.getPermissions().hasPerm(y, "fd.admin") && y != adminEvent.getPlayer()) {
                y.sendMessage(adminMessage);
                recievedAdminMessage.add(y);
                if (debug) {fd.getLog().info(Prefix.getDebugPrefix() + "Sent admin message to " + y.getName());}
            } else {
                if (debug) {fd.getLog().info(Prefix.getDebugPrefix() + y.getName() + " doesn't have the permission fd.admin");}
            }
        }
    }

    private void sendLightAdminMessage(EventInformation ei, int lightLevel) {
        String lightAdminMessage = Prefix.getAdminPrefix() + " " + ChatColor.YELLOW + ei.getPlayer().getName() +
                ChatColor.GRAY +" was denied mining " + ei.getColor() +
                Format.getFormattedName(ei.getMaterial(), 1) + ChatColor.GRAY + " at" + " light level "
                + ChatColor.WHITE +  lightLevel;
        fd.getServer().getConsoleSender().sendMessage(lightAdminMessage);
        for (Player y : fd.getServer().getOnlinePlayers()) {
            if (fd.getPermissions().hasPerm(y, "fd.admin")) {
                if (y != ei.getPlayer()) {
                    y.sendMessage(lightAdminMessage);
                    if (debug) {fd.getLog().info(Prefix.getDebugPrefix() + "Sent admin message to " + y.getName());}
                } else {
                    if (debug) {fd.getLog().info(Prefix.getDebugPrefix() +y.getName() + " was not sent an admin message because it was them who was denied mining.");}
                }
            } else {
                if (debug) {fd.getLog().info(Prefix.getDebugPrefix() + y.getName() + " doesn't have the permission fd.admin");}
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
                        fd.getPotionHandler().handleRandomPotions(ei.getPlayer(), randomNumber);
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
                        fd.getItemHandler().handleRandomItems(ei.getPlayer(), randomNumber);
                    }
                }
            }
        }
    }

    private void broadcastFoundBlock(EventInformation ei) {
        String playerName = getBroadcastName(ei.getPlayer());
        String matName = Format.getFormattedName(ei.getMaterial(), ei.getTotal());
        String message = fd.getConfig().getString(Config.bcMessage).replace("@Prefix@", Prefix.getChatPrefix() + ei.getColor()).replace("@Player@",
                playerName +  (fd.getConfig().getBoolean(Config.useOreColors) ? ei.getColor() : "")).replace("@Number@",
                (ei.getTotal() == 500 ? "over 500" :String.valueOf(ei.getTotal()))).replace("@BlockName@", matName);
        String formatted = PluginUtils.customTranslateAlternateColorCodes('&', message);

        //Prevent redunant output to the console if an admin message was already sent.
        if (!consoleReceived) {
            fd.getServer().getConsoleSender().sendMessage(formatted);
        }

        for (Player x : fd.getServer().getOnlinePlayers()) {
            if (fd.getPermissions().hasPerm(x, "fd.broadcast") && fd.getWorldHandler().isEnabledWorld(x)) {
                if (!recievedAdminMessage.contains(x)) {
                    x.sendMessage(formatted);
                    if (debug) {fd.getLog().info(Prefix.getDebugPrefix() + "Sent broadcast to " + x.getName());}
                } else if (debug) {
                    fd.getLog().info(Prefix.getDebugPrefix() + x.getName() + "recieved an admin message already, so not broadcasting to " + x.getName());
                }
            } else {
                if (debug) {
                    if (!x.hasPermission("fd.broadcast")) {
                        fd.getLog().info(Prefix.getDebugPrefix() + x.getName() + " does not have permission 'fd.broadcast'.  Not broadcasting to " + x.getName());
                    }
                    if (!fd.getWorldHandler().isEnabledWorld(x)) {
                        fd.getLog().info(Prefix.getDebugPrefix() + x.getName() + " is not in an enabled world, so not broadcasting to  " + x.getName());
                    }
                }
            }
        }

        //write to log if cleanlogging.
        if (fd.getConfig().getBoolean(Config.cleanLog)) {
            fd.getLoggingHandler().writeToCleanLog(ei, playerName);
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
            return !fd.getMySQL().blockWasPlaced(loc) && !cantAnnounce.contains(loc);
        } else {
            return !cantAnnounce.contains(loc) && !fd.getBlockPlaceListener().getFlatFilePlacedBlocks().contains(loc);
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
                    fd.getLog().info(Prefix.getDebugPrefix() + " " + ei.getPlayer().getName() + " just mined "
                            + Format.getFormattedName(ei.getMaterial(), 1) + " at light level " + highestLevel
                            + ".  We are disabling ore mining at light level " + formattedLightLevel + " or "
                            + percentage + "%");
                }
                return false;
            }
        }
        sendLightAdminMessage(ei, highestLevel);
        if ((fd.getConfig().getBoolean(Config.logLightLevelViolations))) {
            fd.getLoggingHandler().logLightLevelViolation(ei, highestLevel);
        }
        if (debug) {
            fd.getLog().info(Prefix.getDebugPrefix() + ei.getPlayer().getName() + " was denied mining "
                    + Format.getFormattedName(ei.getMaterial(), 1) + " at light level " + highestLevel
                    + ".  We are disabling ore mining at light level " + formattedLightLevel + " or " + percentage
                    + "%");
        }
        return true;
    }

}