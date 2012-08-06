package org.seed419.founddiamonds.listeners;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.seed419.founddiamonds.*;
import org.seed419.founddiamonds.sql.MySQL;

public class BlockListener implements Listener  {


    private FoundDiamonds fd;
    private MySQL mysql;
    private boolean mysqlEnabled;
    private static final Logger log = Logger.getLogger("FoundDiamonds");
    private HashSet<Location> cantAnnounce = new HashSet<Location>();
    private List<Player> recievedAdminMessage = new LinkedList<Player>();
    private boolean consoleReceived;
    private boolean debug;


    public BlockListener(FoundDiamonds instance, MySQL mysql) {
        this.mysql = mysql;
        this.fd = instance;
    }

    /*Placed block listener*/
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        for (Node x : ListHandler.getBroadcastedBlocks()) {
            if (x.getMaterial() == event.getBlockPlaced().getType()) {
                cantAnnounce.add(event.getBlock().getLocation());
            }
        }
        for (Node x : ListHandler.getAdminMessageBlocks()) {
            if (x.getMaterial() == event.getBlockPlaced().getType()) {
                cantAnnounce.add(event.getBlock().getLocation());
            }
        }
    }

    /*Block break event*/
    /*
    * Method structure:
    * Valid world - returnable
    * Is trap block - cancellable/returnable
    * Valid gamemode - returnable
    * Already broadcasted - returnable
    * Has permissions.
    * Valid lightlevel- cancellable
    * Admin message block
    * Broadcastable block.
    * Logging
    */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        //Check for debug mode
        debug = fd.getConfig().getBoolean(Config.debug);
        Material mat = event.getBlock().getType();

        //Prevent mcMMO's superbreaker from re-announcing.
        if (event.getEventName().equalsIgnoreCase("FakeBlockBreakEvent")) { return; }

        //Check if the world is a world we're listening to
        if (!isEnabledWorld(event.getPlayer())) {
            if (debug) {log.info(FoundDiamonds.getDebugPrefix() + " Cancelling: User is not in a FD enabled world.");}
            return;
        }

        //Check to see if the block is a trap block
        if (isTrapBlock(event.getBlock())) {
            handleTrapBlock(event.getPlayer(), event.getBlock(), event);
            return;
        }

        //Make sure the player is in a valid gamemode
        if (!isValidGameMode(event.getPlayer())) {
            if (debug) { log.info(FoundDiamonds.getDebugPrefix() + " Cancelling: User is in creative mode."); }
            return;
        }

        //Check to see if the block's lightlevel is being monitored.  Comes before others to prevent loopholes.
        Node lightNode = Node.getNodeByMaterial(ListHandler.getLightLevelBlocks(), mat);
        if (lightNode != null) {
            EventInformation lightEvent = new EventInformation(this, event, lightNode, false);
            if(!isValidLightLevel(lightEvent)) {
                event.setCancelled(true);
                return;
            }
        }

        //Check if the block was already announced
        if (!isAnnounceable(event.getBlock().getLocation())) {
            cantAnnounce.remove(event.getBlock().getLocation());
            if (debug) {log.info(FoundDiamonds.getDebugPrefix() + " Cancelling: Block already announced or placed.  Removing broken block from memory.");}
            return;
        }

        //Check if block is broadcast
        Node broadcastNode = Node.getNodeByMaterial(ListHandler.getBroadcastedBlocks(), mat);
        EventInformation broadcastEvent;
        if (broadcastNode != null) {
                broadcastEvent = new EventInformation(this, event, broadcastNode, true);
                handleBroadcast(broadcastEvent);
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
                handleLogging(event.getPlayer(), event.getBlock(), false, false, false);
            }
        }

        //reset message checks after successful event
        recievedAdminMessage.clear();
        consoleReceived = false;
    }




    /*Admin messages*/
    private void sendAdminMessage(EventInformation adminEvent) {
        String adminMessage = FoundDiamonds.getAdminPrefix() + " " + ChatColor.YELLOW + adminEvent.getPlayer().getName() +
                ChatColor.DARK_RED + " just found " + adminEvent.getColor() +
                (adminEvent.getTotal() == 500 ? "a lot of " :String.valueOf(adminEvent.getTotal())) + " " +
                Format.getFormattedName(adminEvent.getMaterial(), adminEvent.getTotal());
        fd.getServer().getConsoleSender().sendMessage(adminMessage);
        consoleReceived = true;
        for (Player y : fd.getServer().getOnlinePlayers()) {
            if (fd.hasPerms(y, "fd.admin") && y != adminEvent.getPlayer()) {
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
            if (fd.hasPerms(y, "fd.admin")) {
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





    /*
     * Trap block handlers
     */
    private boolean isTrapBlock(Block block) {
        if (fd.getTrapBlocks().contains(block.getLocation())) {
            return true;
        }
        return false;
    }

    private void removeTrapBlock(Block block) {
        fd.getTrapBlocks().remove(block.getLocation());
    }

    private void handleTrapBlock(Player player, Block block, BlockBreakEvent event) {
        if(fd.getConfig().getBoolean(Config.adminAlertsOnAllTrapBreaks)) {
            for (Player x: fd.getServer().getOnlinePlayers()) {
                if(fd.hasPerms(x, "fd.admin") && (x != player)) {
                    x.sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " " + player.getName()
                            + " just broke a trap block");
                }
            }
        }
        if (fd.hasPerms(player, "fd.trap")) {
            player.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + " Trap block removed");
            event.setCancelled(true);
            block.setType(Material.AIR);
        } else {
            fd.getServer().broadcastMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " " +  player.getName()
                    + " just broke a trap block");
            event.setCancelled(true);
        }
        boolean banned = false;
        boolean kicked = false;
        if (fd.getConfig().getBoolean(Config.kickOnTrapBreak)  && !fd.hasPerms(player, "FD.trap")) {
            player.kickPlayer(fd.getConfig().getString(Config.kickMessage));
            kicked = true;
        }
        if (fd.getConfig().getBoolean(Config.banOnTrapBreak) && !fd.hasPerms(player, "FD.trap")) {
            player.setBanned(true);
            banned = true;
        }
        if((fd.getConfig().getBoolean(Config.logTrapBreaks)) && (!fd.hasPerms(player, "fd.trap"))) {
            handleLogging(player, block, true, kicked, banned);
        }
        removeTrapBlock(block);
    }




    /*
     * Logging Handlers
     */
    private void handleLogging(Player player, Block block, boolean trapBlock, boolean kicked, boolean banned) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(FileHandler.getLogFile(), true)));
            pw.print("[" + getFormattedDate() + "]");
            if (trapBlock) {
                pw.print(" [TRAP BLOCK]");
            }
            pw.println(" " + block.getType().name().toLowerCase().replace("_", " ") + " broken by "
                    + player.getName() + " at (x: " + block.getX() + ", y: " + block.getY() + ", z: " + block.getZ()
                    + ") in " + player.getWorld().getName());
            if (trapBlock) {
                pw.print("[" + getFormattedDate() + "]" + " [ACTION TAKEN] ");
                if (kicked && !banned) {
                    pw.println(player.getName() + " was kicked from the sever per the configuration.");
                } else if (banned && !kicked) {
                    pw.println(player.getName() + " was banned from the sever per the configuration.");
                } else if (banned && kicked) {
                    pw.println(player.getName() + " was kicked and banned from the sever per the configuration.");
                } else if (!banned && !kicked) {
                    pw.println(player.getName() + " was neither kicked nor banned per the configuration.");
                }
            }
            pw.flush();
            FileHandler.close(pw);
        } catch (IOException ex) {
            log.severe(MessageFormat.format("[{0}] Unable to write to log file {1}", FoundDiamonds.getPrefix(), ex));
        }
    }

    private void logLightLevelViolation(EventInformation ei,  int lightLevel) {
        String lightLogMsg = "[" + getFormattedDate() + "]" + " " + ei.getPlayer().getName() + " was denied mining "
                + Format.getFormattedName(ei.getMaterial(), 1) + " at" + " light level " +  lightLevel;
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(FileHandler.getLogFile(), true)));
            pw.println(lightLogMsg);
            pw.flush();
            FileHandler.close(pw);
        } catch (IOException ex) {
            log.severe(MessageFormat.format("[{0}] Unable to write to log file {1}", FoundDiamonds.getPrefix(), ex));
        }
    }

    private void writeToCleanLog(EventInformation ei, String playerName) {
        String formattedDate = getFormattedDate();
        String message;
        if (ei.getMaterial() == Material.GLOWING_REDSTONE_ORE || ei.getMaterial() == Material.REDSTONE_ORE) {
            if (ei.getTotal() > 1) {
                message = fd.getConfig().getString(Config.bcMessage).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(ei.getTotal())).replace("@BlockName@", "redstone ores");
            } else {
                message = fd.getConfig().getString(Config.bcMessage).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(ei.getTotal())).replace("@BlockName@", "redstone ore");
            }
        } else if (ei.getMaterial() == Material.OBSIDIAN) {
                message = fd.getConfig().getString(Config.bcMessage).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(ei.getTotal())).replace("@BlockName@", "obsidian");
        } else {
            String blockName = Format.getFormattedName(ei.getMaterial(), ei.getTotal());
            if (ei.getTotal() > 1) {
                message = fd.getConfig().getString(Config.bcMessage).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(ei.getTotal())).replace("@BlockName@", blockName +
                        (ei.getMaterial() == Material.DIAMOND_ORE ? "s!" : "s"));
            } else {
                message = fd.getConfig().getString(Config.bcMessage).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(ei.getTotal())).replace("@BlockName@", blockName +
                        (ei.getMaterial() == Material.DIAMOND_ORE ? "!" : ""));
            }
        }
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(FileHandler.getCleanLog(), true)));
            pw.println("[" + formattedDate + "] " + message);
            pw.flush();
            FileHandler.close(pw);
        } catch (IOException ex) {
            Logger.getLogger(BlockListener.class.getName()).log(Level.SEVERE, "Couldn't write to clean log!", ex);
        }
    }

    private String getFormattedDate() {
        Date todaysDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss");
        return formatter.format(todaysDate);
    }




    /*
     * Random Item methods
     */
    private void handleRandomItems(int randomNumber) {
        int randomItem;
        if (randomNumber < 50) {
            randomItem = fd.getConfig().getInt(Config.randomItem1);
        } else if (randomNumber >= 50 && randomNumber < 100) {
            randomItem = fd.getConfig().getInt(Config.randomItem2);
        } else {
            randomItem = fd.getConfig().getInt(Config.randomItem3);
        }
        int amount = getRandomAmount();
        giveItems(randomItem, amount);
    }

    private void broadcastRandomItem(int item, int amount) {
        fd.getServer().broadcastMessage(FoundDiamonds.getPrefix() + ChatColor.GRAY + " Everyone else got " + amount +
        " " + Format.getFormattedName(Material.getMaterial(item), amount));
    }

    @SuppressWarnings("deprecation")
    private void giveItems(int item, int amount) {
        for(Player p: fd.getServer().getOnlinePlayers()) {
            if (isEnabledWorld(p)) {
                broadcastRandomItem(item, amount);
                p.getInventory().addItem(new ItemStack(item, amount));
                p.updateInventory();
            }
        }
    }

    private int getRandomAmount(){
        Random rand = new Random();
        int amount = rand.nextInt(fd.getConfig().getInt(Config.maxItems)) + 1;
        return amount;
    }



    /*Spells*/
    private void handleRandomPotions(int randomNumber) {
        PotionEffect potion;
        String potionMessage;
        int strength = fd.getConfig().getInt(Config.potionStrength);
        if (randomNumber < 25) {
            potion = new PotionEffect(PotionEffectType.SPEED, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.speed);
        } else if (randomNumber >= 25 && randomNumber < 50) {
            potion = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.strength);
        } else if (randomNumber >=50 && randomNumber < 100) {
            potion = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.resist);
        } else if (randomNumber >=100 && randomNumber < 125) {
            potion = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.fireresist);
        } else if (randomNumber >=125 && randomNumber < 150) {
            potion = new PotionEffect(PotionEffectType.FAST_DIGGING, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.fastdig);
        } else if (randomNumber >=150 && randomNumber < 175) {
            potion = new PotionEffect(PotionEffectType.WATER_BREATHING, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.waterbreathe);
        } else if (randomNumber >=175 && randomNumber < 200) {
            potion = new PotionEffect(PotionEffectType.REGENERATION, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.regeneration);
        } else {
            potion = new PotionEffect(PotionEffectType.JUMP, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.jump);
        }
        givePotions(potion, potionMessage);
    }

    private void givePotions(PotionEffect potion, String potionMsg) {
        for (Player p : fd.getServer().getOnlinePlayers()) {
            if (!p.hasPotionEffect(potion.getType()) && isEnabledWorld(p)) {
                p.addPotionEffect(potion);
                if (potion.getType() == PotionEffectType.JUMP) {
                    fd.getJumpPotion().put(p, Boolean.TRUE);
                }
                p.sendMessage(FoundDiamonds.getPrefix() + ChatColor.DARK_RED + " " + potionMsg);
            }
        }
        sendPotionMessageToConsole(potion);
    }

    private void sendPotionMessageToConsole(PotionEffect potion) {
       if (potion.getType() == PotionEffectType.SPEED) {
           log.info(FoundDiamonds.getLoggerPrefix() + " A speed potion has been awarded to the players");
       } else if (potion.getType() == PotionEffectType.FIRE_RESISTANCE) {
           log.info(FoundDiamonds.getLoggerPrefix() + " A fire resistance potion has been awarded to the players");
       } else if (potion.getType() == PotionEffectType.INCREASE_DAMAGE) {
           log.info(FoundDiamonds.getLoggerPrefix() + " An attack buff potion has been awarded to the players");
       } else if (potion.getType() == PotionEffectType.JUMP) {
           log.info(FoundDiamonds.getLoggerPrefix() + " A jump potion has been awarded to the players");
       } else if (potion.getType() == PotionEffectType.DAMAGE_RESISTANCE) {
           log.info(FoundDiamonds.getLoggerPrefix() + " A damage resistance potion has been awarded to the players");
       } else if (potion.getType() == PotionEffectType.FAST_DIGGING) {
           log.info(FoundDiamonds.getLoggerPrefix() + " A fast digging potion has been awarded to the players");
       } else if (potion.getType() == PotionEffectType.REGENERATION) {
           log.info(FoundDiamonds.getLoggerPrefix() + " A regeneration potion has been awarded to the players");
       } else if (potion.getType() == PotionEffectType.WATER_BREATHING) {
           log.info(FoundDiamonds.getLoggerPrefix() + " A water breathing potion has been awarded to the players");
       }

    }

    /*Broadcasts*/
    // This looks sloppy but it needs to be in this order for the output.  Whatever.
    private void handleBroadcast(EventInformation ei) {
        /*Handle mysql*/
        if (isOre(ei.getMaterial()) && mysqlEnabled) {
            mysql.updateUser(ei);
        }
        if (ei.getMaterial() == Material.DIAMOND_ORE) {
            if (fd.getConfig().getBoolean(Config.potionsForFindingDiamonds)) {
                int randomInt = (int) (Math.random()*100);
                if (randomInt <= fd.getConfig().getInt(Config.chanceToGetPotion)) {
                    int randomNumber = (int)(Math.random()*225);
                    if (randomNumber >= 0 && randomNumber <= 225) {
                        handleRandomPotions(randomNumber);
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
                        handleRandomItems(randomNumber);
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
                (ei.getTotal() == 500 ? "a lot of" :String.valueOf(ei.getTotal()))).replace("@BlockName@", matName);
        String formatted = customTranslateAlternateColorCodes('&', message);

        //Prevent redunant output to the console if an admin message was already sent.
        if (!consoleReceived) {
            fd.getServer().getConsoleSender().sendMessage(formatted);
        }

        for (Player x : fd.getServer().getOnlinePlayers()) {
            if (fd.hasPerms(x,"fd.broadcast") && isEnabledWorld(x)) {
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
                    if (!isEnabledWorld(x)) {
                        log.info(FoundDiamonds.getDebugPrefix() + x.getName() + " is not in an enabled world, so not broadcasting to  " + x.getName());
                    }
                }
            }
        }

        //write to log if cleanlogging.
        if (fd.getConfig().getBoolean(Config.cleanLog)) {
            writeToCleanLog(ei, playerName);
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
    private boolean isEnabledWorld(Player player) {
        return fd.getConfig().getList(Config.enabledWorlds).contains(player.getWorld().getName());
    }

    private boolean isValidGameMode(Player player) {
        return !((player.getGameMode() == GameMode.CREATIVE) && (fd.getConfig().getBoolean(Config.disableInCreative)));
    }

    public boolean isAnnounceable(Location loc) {
        return !cantAnnounce.contains(loc);
    }

    public static String customTranslateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] charArray = textToTranslate.toCharArray();
        for (int i = 0; i < charArray.length - 1; i++) {
            if (charArray[i] == altColorChar && "0123456789AaBbCcDdEeFfKkNnRrLlMmOo".indexOf(charArray[i+1]) > -1) {
                charArray[i] = ChatColor.COLOR_CHAR;
                charArray[i+1] = Character.toLowerCase(charArray[i+1]);
            }
        }
        return new String(charArray);
    }

    public HashSet<Location> getCantAnnounce() {
        return cantAnnounce;
    }




    /*
     * Light Methods
     */
    private boolean blockSeesNoLight(EventInformation ei) {
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
            logLightLevelViolation(ei, highestLevel);
        }
        if (debug) {
            log.info(FoundDiamonds.getDebugPrefix() + ei.getPlayer().getName() + " was denied mining "
                    + Format.getFormattedName(ei.getMaterial(), 1) + " at light level " + highestLevel
                    + ".  We are disabling ore mining at light level "  + formattedLightLevel + " or " + percentage
                    + "%");
        }
        return true;
    }

    private boolean isValidLightLevel(EventInformation ei) {
        if (fd.hasPerms(ei.getPlayer(), "fd.monitor")) {
            if (blockSeesNoLight(ei) && ei.getPlayer().getWorld().getEnvironment() != World.Environment.NETHER) {
                ei.getEvent().setCancelled(true);
                ei.getPlayer().sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " Mining in the dark is dangerous, place a torch!");
                return false;
            }
        }
        return true;
    }

    public void setMySQLEnabled(boolean b) {
        mysqlEnabled = b;
    }

    public boolean isOre(Material mat) {
        return mat == Material.IRON_ORE || mat == Material.GOLD_ORE || mat == Material.COAL_ORE
                || mat == Material.LAPIS_ORE || mat == Material.DIAMOND_ORE || mat == Material.REDSTONE_ORE
                || mat == Material.GLOWING_REDSTONE_ORE;
    }

}