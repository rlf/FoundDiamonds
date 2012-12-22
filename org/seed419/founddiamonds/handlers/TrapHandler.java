package org.seed419.founddiamonds.handlers;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.Trap;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.util.BlockColor;
import org.seed419.founddiamonds.util.Format;
import org.seed419.founddiamonds.util.Prefix;

import java.util.ArrayList;

/*
Copyright 2011-2012 Blake Bartenbach, snoepje0

This file is part of FoundDiamonds.

FoundDiamonds is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

FoundDiamonds is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FoundDiamonds.  If not, see <http://www.gnu.org/licenses/>.
*/

public class TrapHandler {


	private FoundDiamonds fd;


	public TrapHandler(FoundDiamonds fd) {
		this.fd = fd;
	}

	public void handleSetTrap(CommandSender sender, String[] args) {
		Material trapMaterial = Material.DIAMOND_ORE;
        boolean persistent = false;
		int depth = 0;

        Player player = (Player) sender; // MenuHandler verifies this is a player.
        if (args[args.length - 1].equalsIgnoreCase("true")) {
            persistent = true;
            System.out.println("persisting!");
        }
        if (args.length == 3) {
            // either trap block specified with no depth, or depth specified with diamond blocks
            // note that the depth will almost *always* match a material ID, so this is NOT recommended.
            trapMaterial = Material.matchMaterial(args[2]);
            if (trapMaterial == null) {
                try {
                    depth = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                    sendTrapError(player);
                    return;
                }
            }
        } else if (args.length == 4) { // either new block format specification, or depth + old block formatting
            trapMaterial = Material.matchMaterial(args[2] + "_" + args[3]);
            if (trapMaterial == null) {
                try {
                    trapMaterial = Material.matchMaterial(args[2]);
                    depth = Integer.parseInt(args[3]);
                } catch (NumberFormatException ex) {
                    sendTrapError(player);
                    return;
                }
            }
        } else if (args.length == 5) { // new block format + depth
            trapMaterial = Material.matchMaterial(args[2] + "_" + args[3]);
            try {
                depth = Integer.parseInt(args[4]);
            } catch (NumberFormatException ex) {
                sendTrapError(player);
            }
        }
        if (trapMaterial != null) {
            createTrap(player, trapMaterial, depth, persistent);
        } else {
            sendTrapError(player);
        }
	}

    public void handleRemoveTrap(CommandSender sender, String[] args) {
        int id;
        if (args.length == 3) {
            try {
                id = Integer.parseInt(args[2]);
                Trap.removeTrapCmd(sender, id);
            } catch (NumberFormatException ex) {
                sender.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " Trap ID number must be an integer");
            }
        } else {
            sender.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " You must specify a trap ID number to remove");
            sender.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " Get the ID number from /fd trap list");
        }
    }

    public void handleListTraps(CommandSender sender, String[] args) {
        int page = 0;
        if (args.length == 3) {
            try {
                page = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                sender.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " Page number must be an integer");
                return;
            }
        }
        Trap.listTraps(sender, page);
    }

    private void sendTrapError(Player player) {
        player.sendMessage(Prefix.getChatPrefix() + ChatColor.RED +" Unable to understand the entered trap format.");
        player.sendMessage(Prefix.getChatPrefix() + ChatColor.RED +" Please ensure your formatting makes sense.");
        player.sendMessage(Prefix.getChatPrefix() + ChatColor.RED +" If it does - please report this bug.");
    }

    private void createTrap(Player player, Material trapMaterial, int depth, boolean persistent) {
        if (trapMaterial != null && isSensibleTrapBlock(trapMaterial)) {
            Location trapLoc = player.getLocation().add(0, -(depth), 0);
            if (!isValidHeight(player, trapLoc)) { return; }
            Trap newTrap = new Trap(getTrapType(trapMaterial), trapMaterial, player.getName(), trapLoc, persistent);
            if (newTrap.createBlocks()) {
                player.sendMessage(Prefix.getMenuPrefix() + ChatColor.WHITE + "Trap ID [" + ChatColor.YELLOW
                        + Trap.getTrapList().size() + ChatColor.WHITE + "] set with " + BlockColor.getBlockColor(trapMaterial)
                        + Format.capitalize(Format.getFormattedName(trapMaterial, 1)) + ChatColor.WHITE + getFormattedDepthString(depth));
                Trap.getTrapList().add(newTrap);
            } else {
                player.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " Unable to place a trap here");
                player.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " There's another one in the way!");
            }
        } else {
            player.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " Unable to set a trap with " + BlockColor.getBlockColor(trapMaterial)
                    + Format.getFormattedName(trapMaterial, 1));
            player.sendMessage(ChatColor.RED + "Use a valid block, for example, /fd trap gold ore");
        }
    }

    private String getFormattedDepthString(int depth) {
        if (depth > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(" ").append(ChatColor.WHITE).append(depth).append(" ")
                    .append((depth == 1 ? "block" : "blocks")).append(" below you");
            return sb.toString();
        }
        return "";
    }

    private byte getTrapType(Material mat) {
        if (mat == Material.EMERALD_ORE) {
            return 3;
        } else {
            return (byte) Math.ceil(Math.random() + 0.5);
        }
    }

    private boolean isValidHeight(Player player, Location trapLocation) {
        int maxHeight = player.getWorld().getMaxHeight();
        int y = trapLocation.getBlockY();
        if ((y - 2) < 0) {
            player.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " I can't place a trap down there, sorry.");
            return false;
        } else if ((y - 1) > maxHeight) {
            player.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " I can't place a trap this high, sorry.");
            return false;
        }
        return true;
    }

	private boolean isSensibleTrapBlock(Material trap) {
		switch (trap) {
            case TORCH:
            case GRAVEL:
            case SAND:
            case DIRT:
            case GRASS:
            case VINE:
            case LEAVES:
            case DEAD_BUSH:
            case REDSTONE_TORCH_ON:
            case REDSTONE_TORCH_OFF:
            case WATER:
            case LAVA:
                return false;
            default:
                return trap.isBlock();
		}
	}

	public boolean isTrapBlock(Location loc) {
		return Trap.getInverseList().containsKey(loc.getBlock());
	}

	public void handleTrapBlockBreak(BlockBreakEvent event) {
		event.setCancelled(true);
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Trap trap = Trap.getInverseList().get(block);
		if (fd.getPermissions().hasPerm(player, "fd.trap")) {
			//player.sendMessage(ChatColor.AQUA + "Trap block removed");
            sendTrapRemovedMessage(player, trap);
            trap.removeTrap();
		} else {
			String trapMessage;
			if (trap.isPersistent()) {
				trapMessage = ChatColor.YELLOW + player.getName() + ChatColor.RED + " just triggered a persistent trap block";
			} else {
				trapMessage = ChatColor.YELLOW + player.getName() + ChatColor.RED + " just triggered a trap block";
				trap.removeTrap(); // traps are removed once triggered, persistent traps stay armed
			}
			for (Player x : fd.getServer().getOnlinePlayers()) {
				if ((fd.getPermissions().hasPerm(x, "fd.trap")) || fd.getPermissions().hasPerm(x, "fd.admin")) {
					x.sendMessage(trapMessage);
				}
			}
			fd.getServer().getConsoleSender().sendMessage(Prefix.getLoggingPrefix() + trapMessage);
			boolean banned = false;
			boolean kicked = false;
			if (fd.getConfig().getBoolean(Config.kickOnTrapBreak)) {
				String kickMessage = fd.getConfig().getString(Config.kickMessage);
				player.kickPlayer(kickMessage);
				kicked = true;
			}
			if (fd.getConfig().getBoolean(Config.banOnTrapBreak)) {
				player.setBanned(true);
				banned = true;
			}
			if (fd.getConfig().getBoolean(Config.logTrapBreaks)) {
				fd.getLoggingHandler().handleLogging(player, block, true, kicked, banned);
			}
		}
	}

    public void sendTrapRemovedMessage(CommandSender sender, Trap trap) {
        sender.sendMessage(Prefix.getMenuPrefix() + ChatColor.WHITE + "Trap ID " + ChatColor.WHITE + "["
                + ChatColor.YELLOW + Trap.getTrapList().indexOf(trap) + ChatColor.WHITE + "]" + ChatColor.GREEN +" removed successfully");    }

	public ArrayList<Trap> getTrapBlocks() {
		return Trap.getTrapList();
	}

}
