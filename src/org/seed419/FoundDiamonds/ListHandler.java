package org.seed419.FoundDiamonds;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: seed419
 * Date: 5/10/12
 * Time: 7:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class ListHandler {


    private static FoundDiamonds fd;
    private static final List<Node> broadcastedBlocks = new LinkedList<Node>();
    private static final List<Node> adminMessageBlocks = new LinkedList<Node>();
    private static final List<Node> lightLevelBlocks = new LinkedList<Node>();


    public ListHandler(FoundDiamonds instance) {
        this.fd = instance;
    }

    public void loadAllBlocks() {
        loadBlocksFromConfig(broadcastedBlocks, Config.broadcastedBlocks);
        loadBlocksFromConfig(adminMessageBlocks, Config.adminMessageBlocks);
        loadBlocksFromConfig(lightLevelBlocks, Config.lightLevelBlocks);
    }

    public static List<?> getVersatileList(String configLoc) {
        return fd.getConfig().getList(configLoc);
    }

    public static void loadBlocksFromConfig(List<Node> list, String configLoc) {
        if (fd.getConfig().getList(configLoc) == null) {
            if (configLoc.equals(Config.broadcastedBlocks)) {
                loadDefaults();
            } else {
                fd.getConfig().set(configLoc, new LinkedList<String>());
            }
        } else {
            @SuppressWarnings("unchecked")
            List<String> blocks = (List<String>) getVersatileList(configLoc);
            //TODO the logging statement
            fd.getLog().severe(FoundDiamonds.getLoggerPrefix() + " Size of block list = " + list.size());
            for (Node x : list) {
                //System.out.println(x.toString());
                //String x = it.next();
                String[] bi = x.toString().split(":");
                Material matchAttempt= null;
                try {
                    matchAttempt = Material.matchMaterial(bi[0]);
                } catch (Exception ex) {
                    fd.getLog().severe(FoundDiamonds.getLoggerPrefix() + " Unable to match material '" + bi[0] + "'");
                }
                if (matchAttempt != null) {
                    if (matchAttempt.isBlock()) {
                        try {
                            String re = bi[1].replace(" ","_").toUpperCase();
                            ChatColor color = ChatColor.valueOf(re);
                            // TODO look at the color section here...
                            //ChatColor color = BlockColor.getBlockColor(matchAttempt);
                            //System.out.println("Adding " + matchAttempt.name() + " " + color.name());
                            if (Node.containsMat(list, matchAttempt)) {
                                list.add(new Node(matchAttempt,color));
                            }
                        } catch (Exception ex) {
                            fd.getLog().severe(FoundDiamonds.getLoggerPrefix() + " Unable to match color '" + bi[1] + "'");
                        }

                    } else {
                        fd.getLog().warning(FoundDiamonds.getLoggerPrefix() + " Unable to add " + x + " because it is not a block!");
                    }
                } else {
                    fd.getLog().warning(FoundDiamonds.getLoggerPrefix() + " Unable to add " + x
                            + ".  Unrecognized block.  See bukkit material enum for valid block names.");
                }
            }
            writeListToConfig(list, Config.broadcastedBlocks);
        }
    }

    public static void writeListToConfig(List<Node> list, String configLoc) {
        LinkedList<String> tempList = new LinkedList<String>();
        for (Node x : list) {
            tempList.add(x.getMaterial().name().toLowerCase().replace("_", " ") + ":" + x.getColor().name().toLowerCase().replace("_", " "));
        }
        fd.getConfig().set(configLoc, tempList);
        fd.saveConfig();
    }

    public static void loadDefaults() {
        broadcastedBlocks.add(new Node(Material.DIAMOND_ORE, ChatColor.AQUA));
        broadcastedBlocks.add(new Node(Material.GOLD_ORE, ChatColor.GOLD));
        broadcastedBlocks.add(new Node(Material.LAPIS_ORE, ChatColor.BLUE));
        broadcastedBlocks.add(new Node(Material.IRON_ORE, ChatColor.GRAY));
        broadcastedBlocks.add(new Node(Material.COAL_ORE, ChatColor.DARK_GRAY));
        broadcastedBlocks.add(new Node(Material.REDSTONE_ORE, ChatColor.DARK_RED));
        broadcastedBlocks.add(new Node(Material.GLOWING_REDSTONE_ORE, ChatColor.DARK_RED));
        writeListToConfig(broadcastedBlocks, Config.broadcastedBlocks);
    }

    //TODO make this work with all 3 lists?
    public static void handleAddToList(CommandSender sender, String[] args, List<Node> list, String configString) {
        if (args.length == 2) {
            sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " Format is: item:color");
            sender.sendMessage(ChatColor.RED + " Color is an optional argument.");
            sender.sendMessage(ChatColor.RED + " Ex: sugar cane block:dark green");
        } else if (args.length >= 3) {
            StringBuilder sb = new StringBuilder();
            for (int i=2;i<args.length;i++) {
                sb.append(args[i]).append(" ");
            }
            Node block = Node.parseNode(sb.toString().trim());
            if (block != null) {
                if (!Node.containsMat(list, block.getMaterial())) {
                    list.add(block);
                    //TODO just save the list?
                    //addNodeToConfig(block, configString);
                    sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + " Added " + block.getColor()
                            + Format.material(block.getMaterial()));
                    writeListToConfig(list, configString);
                } else {
                    removeMaterialFromList(block.getMaterial(), list, configString);
                    list.add(block);
                    sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + " Updated " + block.getColor()
                            + Format.material(block.getMaterial()));
                    writeListToConfig(list, configString);
                }
            } else {
                sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.DARK_RED + " Unable to add block.  Please check your format.");
            }
        }
    }

    public static void handleRemoveFromList(CommandSender sender, String[] args, List<Node> list, String configString) {
        if (args.length == 2) {
            sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " Simply type the name of the block you want to remove");
            sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " It unfortunely must match bukkit's material enum");
            sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " If this is really buggy, please ask SeeD419 for help!");
        } else if (args.length > 2) {
            StringBuilder sb = new StringBuilder();
            fd.getLog().severe(FoundDiamonds.getLoggerPrefix() + " Size of block list = " + list.size());
            for (int i = 2; i < args.length; i++) {
                sb.append(args[i] + " ");
            }
            Material matToRemove = Material.matchMaterial(sb.toString().trim());
            if (matToRemove == null) {
                sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.DARK_RED + " Unrecognized material");
            } else {
                ChatColor color = getNodeColor(matToRemove,  list);
                if (removeMaterialFromList(matToRemove, list, configString)) {
                    sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.DARK_RED + " Removed " + color + Format.material(matToRemove));
                    fd.getLog().severe(FoundDiamonds.getLoggerPrefix() + " Size of block list = " + list.size());
                } else {
                    sender.sendMessage(FoundDiamonds.getPrefix() + " "  + ChatColor.WHITE + Format.material(matToRemove) + ChatColor.DARK_RED + " isn't listed.");
                }
            }
        }
    }

    public static void handleListingList(CommandSender sender, List<Node> list) {
        for (Node x : list) {
            sender.sendMessage(x.getColor() + Format.capitalize(Format.material(x.getMaterial())));
        }
    }

    public static ChatColor getNodeColor(Material mat, List<Node> list) {
        for (Node x : list) {
            if (x.getMaterial() == mat) {
                return x.getColor();
            }
        }
        return null;
    }

    public static boolean removeMaterialFromList(Material mat, List<Node> list, String configString) {
        for (Node x : list) {
            if (x.getMaterial() == mat) {
                list.remove(x);
                writeListToConfig(list, configString);
                return true;
            }
        }
        return false;
    }

    public static List<Node> getBroadcastedBlocks() {
        return broadcastedBlocks;
    }

    public static List<Node> getAdminMessageBlocks() {
        return adminMessageBlocks;
    }

    public static  List<Node> getLightLevelBlocks() {
        return lightLevelBlocks;
    }
}
