package org.seed419.founddiamonds;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.seed419.founddiamonds.util.BlockColor;
import org.seed419.founddiamonds.util.Format;
import org.seed419.founddiamonds.util.Prefix;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

public class Trap {


	private final byte type;
	private final Material mat;
	private Material[] oldMat; // the blocks that were replaced
	private final String placer; // name of the player who set the trap
	private final Location location; // the 'middle' of the trap
	private final Date time; // the date the trap was added;
	private boolean persistent; // will the trap persist when broken
	private static ArrayList<Trap> trapList = new ArrayList<Trap>(); // map linking traps to locations, the middle of the trap
	private static Map<Block,Trap> inverseList = new HashMap<Block,Trap>(); // map linking locations to traps


	public Trap(byte type, Material mat, String player, Location location, boolean persistent) {
		this.type = type;
		this.mat = mat;
		this.placer = player;
		this.location = location;
		this.time = new Date(System.currentTimeMillis());
		this.persistent = persistent;
	}

	public Trap(byte type, Material mat, Material[] oldMat, String player, Location loc, long time, boolean persistent) {
		this.type = type;
		this.mat = mat;
		this.oldMat = oldMat;
		this.placer = player;
		this.location = loc;
		this.time = new Date(time);
		this.persistent = persistent;
		trapList.add(this);
		this.refillInverse();
	}
	
	private void refillInverse() {
		Block[] temp = this.returnLocations(this.location.getWorld());
		for(Block block : temp){
			inverseList.put(block, this);
		}
	}

	public boolean createBlocks() {
		Block[] locations = this.returnLocations(this.location.getWorld());
		oldMat = new Material[locations.length];
        for (Block block : locations) {
            if (inverseList.containsKey(block)) {
                return false;
            }
        }
		if (this.mat == Material.EMERALD_ORE) {
			oldMat[0] = this.location.getBlock().getType();
			inverseList.put(location.getBlock(), this);
			location.getBlock().setType(mat);
		} else {
			oldMat[0] = locations[0].getType();
			oldMat[1] = locations[1].getType();
			oldMat[2] = locations[2].getType();
			oldMat[3] = locations[3].getType();
			inverseList.put(locations[0], this);
			inverseList.put(locations[1], this);
			inverseList.put(locations[2], this);
			inverseList.put(locations[3], this);
			locations[0].setType(mat);
			locations[1].setType(mat);
			locations[2].setType(mat);
			locations[3].setType(mat);
		}
		return true;
	}

	private Block[] returnLocations(World world) {
        Block block1;
        Block block2;
        Block block3;
        Block block4;
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

		switch (this.type) {
            case 1:
                block1 = world.getBlockAt(x, y - 1, z);
                block2 = world.getBlockAt(x, y - 2, z + 1);
                block3 = world.getBlockAt(x - 1, y - 2, z);
                block4 = world.getBlockAt(x, y - 2, z);
                return new Block[]{block1, block2, block3, block4};
            case 2:
                block1 = world.getBlockAt(x, y - 1, z);
                block2 = world.getBlockAt(x - 1, y - 2, z);
                block3 = world.getBlockAt(x , y - 2, z);
                block4 = world.getBlockAt(x -1, y - 1, z);
                return new Block[]{block1, block2, block3, block4};
            case 3:
                return new Block[]{this.location.getBlock()}; // emeralds
            default:
                return null; // aliens
		}
	}

	public void removeTrap() {
		Block[] temp = this.returnLocations(this.location.getWorld());
		for (int i = 0; i < temp.length; i++) {
			temp[i].setType(oldMat[i]);
			inverseList.remove(temp[i]);
		}
		trapList.remove(this);
	}

	public static void listTraps(CommandSender sender, int page) { // TODO: Page numbers
        sender.sendMessage(Prefix.getMenuPrefix() + Format.formatMenuHeader("Active Traps"));
        if (trapList.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "  None");
            return;
        }
		if (sender.hasPermission("fd.trap.remove.all") || sender.isOp()) {
			if (page >= 0 && ( (page) *5 < trapList.size()) ) { //sane page specified?
				int id1 = (page)*5 ; //begin of the substring
				int id2 = (page +1) *5 -1;  //end of the substring
				if (id2 > Trap.trapList.size()) {
					id2 = Trap.trapList.size()-1;
				}
				for (Trap object : trapList) {
                    sendTrapListing(sender, object);
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Page number is invalid");
			}
		} else if (sender.hasPermission("fd.trap.remove.self")) { // permission to remove, and thus to see own traps
			ArrayList<Trap> showList = new ArrayList<Trap>();
			for (Trap trap : trapList) {
				if (trap.placer.equals(sender.getName())) {
                    showList.add(trap);
                }
			}
			if (page >= 0 && ( (page) *5 < showList.size()) ) {		//sane page specified?
				int id1 = (page)*5 ; //begin of the substring
				int id2 = (page +1) *5 -1;  //end of the substring
				if (id2 > showList.size()) {
					id2 = showList.size()-1;
				}
				for (Trap object : showList) {
                    sendTrapListing(sender, object);
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Page number is invalid");
			}
		} else {
			sender.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + "You don't have permission to view any traps");
		}

	}

	private static void sendTrapListing(CommandSender sender, Trap object) {
		sender.sendMessage(ChatColor.WHITE + "[" + ChatColor.YELLOW + trapList.indexOf(object) + ChatColor.WHITE + "] "
                + ChatColor.AQUA + DateFormat.getDateInstance(DateFormat.MEDIUM).format((object.time)) + ChatColor.WHITE
                + " @ x" + Format.leftGreenParen + object.location.getBlockX() + Format.rightGreenParen
                + " y" + Format.leftGreenParen + object.location.getBlockY() + Format.rightGreenParen
                + " z" + Format.leftGreenParen + object.location.getBlockZ() + Format.rightGreenParen + " "
                + ChatColor.RED + (object.persistent ? "{Persistent}" : ChatColor.GREEN + "{Breakable}" ));
        sender.sendMessage("        " + BlockColor.getBlockColor(object.mat)
                + Format.capitalize(Format.getFormattedName(object.mat, 1)) + ChatColor.WHITE
                + " placed by " + ChatColor.YELLOW + object.placer + ChatColor.WHITE + " in world "
                + ChatColor.YELLOW + object.location.getWorld().getName());
			}

	public static void removeTrapCmd(CommandSender sender, int id) {
		if (id >= 0 && id < trapList.size()) {
			Trap temp = trapList.get(id);
			if ((temp.placer.equals(temp.placer)) &&
                    (sender.hasPermission("fd.trap.remove.self") || sender.hasPermission("fd.trap.remove.all"))) {
				temp.removeTrap();
                // TODO this method already exists in TrapHandler
				sender.sendMessage(Prefix.getMenuPrefix() + ChatColor.WHITE + "Trap ID " + ChatColor.WHITE + "["
                        + ChatColor.YELLOW + id + ChatColor.WHITE + "]" + ChatColor.GREEN +" removed successfully");
			}
		}
	}

	public boolean isPersistent() {
		return this.persistent;
	}

	public static ArrayList<Trap> getTrapList() {
		return trapList;
	}

	public static Map<Block, Trap> getInverseList() {
		return inverseList;
	}

	public String getTrapSummary() { // method to summarize the trap object, for saving
		String oldMatString = "";
		for (Material material : oldMat) {
            oldMatString += material.getId() + ";";
		}
		return this.type + ";" + this.mat.getId() + ";" + oldMatString + this.placer + ";" + this.location
				.getBlockX() + ";" + this.location.getBlockY() + ";" + this.location
				.getBlockZ() + ";" + this.location.getWorld().getName() + ";" + this.time
				.getTime()   + ";" + this.persistent;
	}
}
