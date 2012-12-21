package org.seed419.founddiamonds.handlers;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.Trap;
import org.seed419.founddiamonds.file.Config;
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
	//private final ArrayList<Trap> trapBlocks = new ArrayList<Trap>();

	public TrapHandler(FoundDiamonds fd) {
		this.fd = fd;
	}

	public void handleTrap(Player player, String[] args) {
		Location playerLoc = player.getLocation();
		Material trap = Material.AIR;
		String item = "";
		int depth = 0;

        //what?
		boolean persistent = false;
		if ((args[args.length - 1].equalsIgnoreCase("true") || args[args.length - 1].equalsIgnoreCase("false"))) {
			persistent = Boolean.parseBoolean(args[args.length - 1]);
			String[] temp = new String[args.length - 1];
			for (int i = 0; i < args.length - 1; i++) {
				temp[i] = args[i];
			}
			args = temp; // continue without the persistent bool in the end, this way I don't have to recode all the command interpreting
		}

        System.out.println("Received: " + args.length);
        System.out.println("Arg 1: " + args[1]);

        if (args.length == 1) {
			trap = Material.DIAMOND_ORE;
			item = "Diamond ore";
		}
		if (args[1].equalsIgnoreCase("list")) {
			int page = 0;
			if (args.length == 3) {
				try {
					page = Integer.parseInt(args[2]);
				} catch (NumberFormatException ex) {
					player.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " Invalid arguments");
					return;
				}
			}
			Trap.Menu(player, page);
			return;

		} else if (args[1].equalsIgnoreCase("remove")) {
			int id;
			if (args.length == 3) {
				try {
					id = Integer.parseInt(args[2]);
				} catch (NumberFormatException ex) {
					player.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " Invalid arguments");
					return;
				}
				Trap.removeTrapCmd(player, id);
			} else {
				player.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " Invalid arguments");
			}
			return;
        } else if (args[1].equalsIgnoreCase("set")) {
            if (args.length == 2) { // either trap block specified, old format, or depth specified, assuming diamond blocks
                item = args[1];
                trap = Material.matchMaterial(item);
                if (trap == null) {
                    try {
                        depth = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ex) {
                        player.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " Invalid arguments");
                        return;
                    }
                    item = "Diamond ore";
                    trap = Material.DIAMOND_ORE;
                }
            } else if (args.length == 3) { // either new block format specification, or depth + old block formatting
                item = args[1] + "_" + args[2];
                trap = Material.matchMaterial(item);
                if (trap == null) {
                    try {
                        depth = Integer.parseInt(args[2]);
                    } catch (NumberFormatException ex) {
                        player.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " Invalid arguments");
                        return;
                    }
                    item = args[1];
                    trap = Material.matchMaterial(item);
                }
            } else if (args.length == 4) { // new block format + depth
                item = args[1] + "_" + args[2];
                trap = Material.matchMaterial(item);
                try {
                    depth = Integer.parseInt(args[3]);
                } catch (NumberFormatException ex) {
                    player.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " Invalid arguments");
                    return;
                }
            }
		} else {
			player.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " Invalid number of arguments");
			player.sendMessage(ChatColor.RED + "Use '/fd trap' to see the menu");
			return;
		}
		if (trap != null && isSensibleTrapBlock(trap)) {
	    	Location trapLoc = playerLoc.add(0, -(depth+1), 0);
			int maxHeight = player.getWorld().getMaxHeight();
			int y = trapLoc.getBlockY();
			if ((y - 2) < 0) {
				player.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " I can't place a trap down there, sorry.");
				return;
			} else if ((y - 1) > maxHeight) {
				player.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " I can't place a trap this high, sorry.");
				return;
			}
			byte type = 0; // determination of the trap type
			if (trap == Material.EMERALD_ORE) {
				type = 3;
			} else {
				type = (byte) Math.ceil(Math.random() + 0.5); // should work.
			}
			Trap temp = new Trap(type, trap, player, trapLoc, persistent,item);
		} else {
			player.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " Unable to set a trap with " + item);
			player.sendMessage(ChatColor.RED + "Is it a valid trap block? Try /fd trap gold ore");
		}
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
			player.sendMessage(ChatColor.AQUA + "Trap block removed");
			trap.removeTrap();
		} else {
			String trapMessage;
			if (trap.isPersistent()) {
				trapMessage = ChatColor.YELLOW + player.getName() + ChatColor.RED + " just triggered a persistent trap";
			} else {
				trapMessage = ChatColor.YELLOW + player.getName() + ChatColor.RED + " just triggered a trap block";
				trap.removeTrap(); // traps are removed once triggered, persistent traps stay armed
				// someone else might accidentally follow the xrayer's path, and get kicked/banned as well
			}
			for (Player x : fd.getServer().getOnlinePlayers()) {
				if ((fd.getPermissions().hasPerm(x, "fd.trap")) || fd
						.getPermissions().hasPerm(x, "fd.admin")) {
					x.sendMessage(trapMessage);
				}
			}
			fd.getServer().getConsoleSender()
					.sendMessage(Prefix.getLoggingPrefix() + trapMessage);
			boolean banned = false;
			boolean kicked = false;
			if (fd.getConfig().getBoolean(Config.kickOnTrapBreak)) {
				String kickMessage = fd.getConfig().getString(
						Config.kickMessage);
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

	public ArrayList<Trap> getTrapBlocks() {
		return Trap.getList();
	}

}
