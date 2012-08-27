package org.seed419.founddiamonds.handlers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.Node;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.util.BlockColor;
import org.seed419.founddiamonds.util.Format;
import org.seed419.founddiamonds.util.Prefix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ListHandler {


    private FoundDiamonds fd;
    private List<Node> broadcastedBlocks = new ArrayList<Node>();
    private List<Node> adminMessageBlocks = new ArrayList<Node>();
    private List<Node> lightLevelBlocks = new ArrayList<Node>();


    public ListHandler(FoundDiamonds fd) {
        this.fd = fd;
    }

    public void loadAllBlocks() {
        loadBlocksFromConfig(broadcastedBlocks, Config.broadcastedBlocks);
        loadBlocksFromConfig(adminMessageBlocks, Config.adminMessageBlocks);
        loadBlocksFromConfig(lightLevelBlocks, Config.lightLevelBlocks);
    }

    public void createList(List<Node> list, String configLoc) {
        if (configLoc.equals(Config.broadcastedBlocks)) {
            loadDefaults();
        } else {
            fd.getConfig().set(configLoc, new HashSet<String>());
        }
        writeListToConfig(list, configLoc);
    }

    public void loadBlocksFromConfig(List<Node> list, String configLoc) {
        if (fd.getConfig().getList(configLoc) == null) {
            createList(list, configLoc);
        } else {
            List<String> thelist = fd.getConfig().getStringList(configLoc);
            for (String x : thelist) {
                String[] bi = x.split(":");
                Material matchAttempt= null;
                try {
                    matchAttempt = Material.matchMaterial(bi[0]);
                    if (matchAttempt != null && matchAttempt.isBlock()) {
                        try {
                            String parsedColor = bi[1].replace(" ","_").toUpperCase();
                            ChatColor color = ChatColor.valueOf(parsedColor);
                            if (color == null) {
                                color = BlockColor.getBlockColor(matchAttempt);
                            }
                            if (!Node.containsMat(list, matchAttempt)) {
                                list.add(new Node(matchAttempt,color));
                            }
                        } catch (Exception ex) {
                            fd.getLog().severe(" Unable to match color '" + bi[1] + "'");
                        }
                    } else {
                        fd.getLog().warning(" Unable to add " + bi[0]);
                        fd.getLog().warning(" Check the FD wiki for valid names.");
                    }
                } catch (Exception ex) {
                    fd.getLog().severe(" Unable to match material '" + bi[0] + "'");
                }
            }
        }
    }


    public void loadDefaults() {
        fd.getLog().info("Adding broadcast defaults...");
        broadcastedBlocks.add(new Node(Material.DIAMOND_ORE, ChatColor.AQUA));
        fd.getLog().info("Diamond Ore added");
        broadcastedBlocks.add(new Node(Material.GOLD_ORE, ChatColor.GOLD));
        fd.getLog().info("Gold Ore added");
        broadcastedBlocks.add(new Node(Material.LAPIS_ORE, ChatColor.BLUE));
        fd.getLog().info("Lapis Ore added");
        broadcastedBlocks.add(new Node(Material.IRON_ORE, ChatColor.GRAY));
        fd.getLog().info("Iron Ore added");
        broadcastedBlocks.add(new Node(Material.COAL_ORE, ChatColor.DARK_GRAY));
        fd.getLog().info("Coal Ore added");
        broadcastedBlocks.add(new Node(Material.REDSTONE_ORE, ChatColor.DARK_RED));
        broadcastedBlocks.add(new Node(Material.GLOWING_REDSTONE_ORE, ChatColor.DARK_RED));
        fd.getLog().info("Redstone Ore added");
        broadcastedBlocks.add(new Node(Material.EMERALD_ORE, ChatColor.GREEN));
        fd.getLog().info("Emerald Ore added");
    }

    public void writeListToConfig(Collection<Node> list, String configLoc) {
        List<String> temp = new ArrayList<String>();
        for (Node x : list) {
            temp.add(x.toString());
        }
        fd.getConfig().set(configLoc, temp);
        fd.saveConfig();
    }

    public void handleAddToList(CommandSender sender, String[] args, List<Node> list, String configString) {
        if (args.length == 2) {
            sender.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " Format is: item:data,color");
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
                    sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " Added " + block.getColor()
                            + Format.material(block.getMaterial()));
                } else {
                    removeMaterialFromList(block.getMaterial(), list);
                    list.add(block);
                    sender.sendMessage(Prefix.getChatPrefix() + ChatColor.AQUA + " Updated " + block.getColor()
                            + Format.material(block.getMaterial()));
                }
                writeListToConfig(list, configString);
            } else {
                sender.sendMessage(Prefix.getChatPrefix() + ChatColor.DARK_RED + " Unable to add block.  Please check your format.");
            }
        }
    }

    public void handleRemoveFromList(CommandSender sender, String[] args, List<Node> list, String configString) {
        if (args.length == 2) {
            sender.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " Simply type the name of the block you want to remove");
            sender.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " It unfortunely must match bukkit's material enum");
            sender.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " If this is really buggy, please ask SeeD419 for help!");
        } else if (args.length > 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }
            Material matToRemove = Material.matchMaterial(sb.toString().trim());
            if (matToRemove == null) {
                sender.sendMessage(Prefix.getChatPrefix() + ChatColor.DARK_RED + " Unrecognized material");
            } else {
                ChatColor color = getNodeColor(matToRemove,  list);
                if (removeMaterialFromList(matToRemove, list)) {
                    sender.sendMessage(Prefix.getChatPrefix() + ChatColor.DARK_RED + " Removed " + color + Format.material(matToRemove));
                    writeListToConfig(list, configString);
                } else {
                    sender.sendMessage(Prefix.getChatPrefix() + " "  + ChatColor.WHITE + Format.material(matToRemove) + ChatColor.DARK_RED + " isn't listed.");
                }
            }
        }
    }

    public void handleListingList(CommandSender sender, List<Node> list) {
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

    public static boolean removeMaterialFromList(Material mat, List<Node> list) {
        for (Node x : list) {
            if (x.getMaterial() == mat) {
                list.remove(x);
                return true;
            }
        }
        return false;
    }

    public List<Node> getBroadcastedBlocks() {
        return broadcastedBlocks;
    }

    public List<Node> getAdminMessageBlocks() {
        return adminMessageBlocks;
    }

    public  List<Node> getLightLevelBlocks() {
        return lightLevelBlocks;
    }
}
