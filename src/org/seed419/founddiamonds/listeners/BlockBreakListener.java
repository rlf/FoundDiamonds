package org.seed419.founddiamonds.listeners;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.seed419.founddiamonds.*;
import org.seed419.founddiamonds.sql.MySQL;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class BlockBreakListener implements Listener  {


    private FoundDiamonds fd;
    private MySQL mysql;
    private Trap trap;
    private Logging logging;
    private BlockPlaceListener bpl;
    private boolean mysqlEnabled;
    private static final Logger log = Logger.getLogger("FoundDiamonds");
    private HashSet<Location> cantAnnounce = new HashSet<Location>();
    private List<Player> recievedAdminMessage = new LinkedList<Player>();
    private boolean consoleReceived;
    private boolean debug;


    public BlockBreakListener(FoundDiamonds instance, MySQL mysql, Trap trap, Logging logging, BlockPlaceListener bpl) {
        this.mysql = mysql;
        this.fd = instance;
        this.trap = trap;
        this.logging = logging;
        this.bpl = bpl;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        Material mat = event.getBlock().getType();
        //Check to see if the block's lightlevel is being monitored.  Comes before others to prevent loopholes.
        Node lightNode = Node.getNodeByMaterial(ListHandler.getLightLevelBlocks(), mat);
        if (lightNode != null) {
            EventInformation lightEvent = new EventInformation(this, event, lightNode, false);
            if(!isValidLightLevel(lightEvent, event)) {
                return;
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

        //Check if the world is a world we're listening to
        if (!isEnabledWorld(event.getPlayer())) {
            if (debug) {log.info(FoundDiamonds.getDebugPrefix() + " Cancelling: User is not in a FD enabled world.");}
            return;
        }

        //Prevent mcMMO's superbreaker from re-announcing.
        if (event.getEventName().equalsIgnoreCase("FakeBlockBreakEvent")) { return; }

        //Make sure the player is in a valid gamemode
        if (!isValidGameMode(event.getPlayer())) {
            if (debug) { log.info(FoundDiamonds.getDebugPrefix() + " Cancelling: User is in creative mode."); }
            return;
        }

        //Check to see if the block is a trap block
        if (trap.checkForTrapBlock(event)) {
            return;
        }

        //Broadcast
        if (fd.hasPerms(event.getPlayer(), "fd.broadcast")) {
            if (!isAnnounceable(event.getBlock().getLocation())) {
                removeAnnouncedOrPlacedBlock(event.getBlock().getLocation());
                if (debug) {log.info(FoundDiamonds.getDebugPrefix() + " Cancelling: Block already announced or placed.  Removing broken block from memory.");}
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
        if (bpl.getPlacedBlocks().contains(loc)) {
            bpl.getPlacedBlocks().remove(loc);
        } else if (cantAnnounce.contains(loc)) {
            cantAnnounce.remove(loc);
        }
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
        int amount = getRandomItemAmount();
        giveItems(randomItem, amount);
    }

    @SuppressWarnings("deprecation")
    private void giveItems(int item, int amount) {
        for(Player p: fd.getServer().getOnlinePlayers()) {
            if (isEnabledWorld(p)) {
                p.sendMessage(FoundDiamonds.getPrefix() + ChatColor.GRAY + " Everyone else got " + amount +
                    " " + Format.getFormattedName(Material.getMaterial(item), amount));
                p.getInventory().addItem(new ItemStack(item, amount));
                p.updateInventory();
            }
        }
    }

    private int getRandomItemAmount(){
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
        if (!fd.hasPerms(ei.getPlayer(), "fd.broadcast")) {
            return;
        }
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
    private boolean isEnabledWorld(Player player) {
        return fd.getConfig().getList(Config.enabledWorlds).contains(player.getWorld().getName());
    }

    private boolean isValidGameMode(Player player) {
        return !((player.getGameMode() == GameMode.CREATIVE) && (fd.getConfig().getBoolean(Config.disableInCreative)));
    }

    public boolean isAnnounceable(Location loc) {
        return !cantAnnounce.contains(loc) && !bpl.getPlacedBlocks().contains(loc);
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

    private boolean isValidLightLevel(EventInformation ei, BlockDamageEvent event) {
        if (fd.hasPerms(ei.getPlayer(), "fd.monitor")) {
            if (blockSeesNoLight(ei) && ei.getPlayer().getWorld().getEnvironment() != World.Environment.NETHER) {
                event.setCancelled(true);
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