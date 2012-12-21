package org.seed419.founddiamonds;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
	private final byte type; // the type, and thus form and size of the trap
	private final Material mat; // the trap blocks placed
	private Material[] oldMat; // the blocks that were replaced
	private final Player placer; // the person that placed the block
	private final Location location; // the 'middle' of the trap
	private final Date time; // the date the trap was added;
	private boolean persistent; // will the trap persist when broken
	private static ArrayList<Trap> list = new ArrayList<Trap>(); // map linking traps to locations, the middle of the trap
	private static Map<Block,Trap> inverseList = new HashMap<Block,Trap>(); // map linking locations to traps

	public Trap(byte type, Material mat, Player placer, Location location, boolean persistent, String item) {
		this.type = type;
		this.mat = mat;
		this.placer = placer;
		this.location = location;
		this.time = new Date(System.currentTimeMillis());
		this.persistent = persistent;
		list.add(this);

		if (!this.createBlocks()) {
			list.remove(this);

		} else {
			placer.sendMessage(ChatColor.WHITE + "Trap placed with " + item);
		}
	}

	public Trap(byte type, Material mat, Material[] oldMat, Player placer, Location loc, long time, boolean persistent) {
		this.type = type;
		this.mat = mat;
		this.oldMat = oldMat;
		this.placer = placer;
		this.location = loc;
		this.time = new Date(time);
		this.persistent = persistent;
		list.add(this);
		placer.sendMessage("trap placed");
		this.refillInverse();
		//This way, the traps are subject to tampering through the savefile. Maybe some sort of hashing?
	}
	
	private void refillInverse() {
		Block[] temp = this.returnLocations();
		for(Block block : temp){
			inverseList.put(block, this);
		}
	}

	private boolean createBlocks() {

		Block[] locations = this.returnLocations();
		oldMat = new Material[locations.length];
		for (int i = 0; i < locations.length; i++) { // need to run this one firstly, this needs to complete before I can start adding the locations
			if (inverseList.containsKey(locations[i])) {
				placer.sendMessage(ChatColor.RED + "Unable to place trap here, there's another one in the way!");
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
		/*
		 * for (int i = 0; i < locations.length; i++) {
		 * oldMat[i] = locations[i].getBlock().getType(); // initialization of old materials
		 * inverseList.put(locations[i], this); // adding the locations to the inverse list
		 * locations[i].getBlock().setType(mat); // replacing the block with the trap block
		 * placer.sendMessage("debug : old material: " + oldMat[i]);
		 * placer.sendMessage("debug : new material: " + mat);
		 * }
		 */
		return true;
	}

	private Block[] returnLocations() {
        World world = placer.getWorld();
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
                return null;
		}
	}

	public void removeTrap() { // not entirely sure about this, but this should remove the trap object(unfamiliar with garbage collecting :-S )
		// it also puts the old blocks back in place
		// I dislike the public modifier on this thing, but it was the only way to get it to work with the onBlockBreak event (in TrapListener)
		Block[] temp = this.returnLocations();
		for (int i = 0; i < temp.length; i++) {
			temp[i].setType(oldMat[i]);
			inverseList.remove(temp[i]);
		}
		list.remove(this);
	}

	public static boolean Menu(CommandSender sender, int page) { // TODO: This part still looks a bit messy
		if (sender.hasPermission("fd.trap.remove.all") || sender.isOp()) { // permissions to see all traps
			ArrayList<Trap> trapList = new ArrayList<Trap>();

			for (Trap trap : list) {
				trapList.add(trap);
			}
			if (page >= 0 && ( (page) *5 < list.size()) ) {		//sane page specified?
				int id1 = (page)*5 ; //begin of the substring
				int id2 = (page +1) *5 -1;  //end of the substring
				if(id2 > list.size()){
					id2 = list.size()-1;
				}
				for(Trap object : trapList){
				sendMenu(sender, object);
				}
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Page number is invalid");
				return false;
			}
		} else if (sender.hasPermission("fd.trap.remove.self")) { // permission to remove, and thus to see own traps
			ArrayList<Trap> trapList = new ArrayList<Trap>();
			for (Trap trap : list) {
				if (trap.placer == sender)
					trapList.add(trap);
			}
			if (page >= 0 && ( (page) *5 < list.size()) ) {		//sane page specified?
				int id1 = (page)*5 ; //begin of the substring
				int id2 = (page +1) *5 -1;  //end of the substring
				if(id2 > list.size()){
					id2 = list.size()-1;
				}
				for(Trap object : trapList){
				sendMenu(sender, object);
				}
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Page number is invalid");
				return false;
			}
		} else {
			sender.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " You don't have permission to do that.");
			return false;
		}

	}

	private static void sendMenu(CommandSender sender, Trap object) { // eclipse formats this weirdly...
			sender.sendMessage(""+ChatColor.WHITE + "[" + list.indexOf(object) + "]" + DateFormat
					.getDateInstance(DateFormat.MEDIUM).format((object.time)) + " - Location: " + object.location
					.getBlockX() + " " + object.location.getBlockY() + " " + object.location
					.getBlockZ() + " By " + object.placer.getDisplayName());
			}

	public static void removeTrapCmd(CommandSender sender, int id) {
		if (id >= 0 && id < list.size()) {
			Trap temp = list.get(id);
			if ((temp.placer == sender && sender
					.hasPermission("fd.trap.remove.self") || sender
						.hasPermission("fd.trap.remove.all"))) {
				temp.removeTrap();
				sender.sendMessage(ChatColor.YELLOW + "Trap removed successfully");
			}
		}
	}

	public boolean isPersistent() {
		return this.persistent;
	}

	public static ArrayList<Trap> getList() {
		return list;
	}

	public static void setList(ArrayList<Trap> list) {
		Trap.list = list;
	}

	public static Map<Block, Trap> getInverseList() {
		return inverseList;
	}

	public String getTrapSummary() { // method to summarize the trap object, for saving purposed
		String oldMatString = "";
		for (Material material : oldMat) {
            oldMatString += material.getId() + ";";
		}
		return this.type + ";" + this.mat.getId() + ";" + oldMatString + this.placer.getDisplayName() + ";" + this.location
				.getBlockX() + ";" + this.location.getBlockY() + ";" + this.location
				.getBlockZ() + ";" + this.location.getWorld().getName() + ";" + this.time
				.getTime() + ";" + this.persistent;
	}
}
