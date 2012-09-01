/**
 * 
 */
package org.seed419.founddiamonds;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.seed419.founddiamonds.util.Prefix;

/**
 * @author snoepje0
 * 
 */
public class Trap {
	private final byte type; // the type, and thus form and size of the trap,
								// byte variable to save memory
	private final Material mat; // the trapblocks placed
	private Material[] oldmat; // the blocks that were replaced
	private final Player placer; // the person that placed the block
	private final Location location; // the 'middle' of the trap
	private final Date time; // the date the trap was added;
	// TODO use Date.toString()
	private boolean persistant; // will the trap persist when broken, or will it
								// dissolve, was a request ticket on the dev
								// page
	private static ArrayList<Trap> list = new ArrayList<Trap>(); // map linking traps to locations, the middle of the trap
	private static Map<Location, Trap> inverselist = new HashMap<Location, Trap>(); // map linking locations to traps

	public Trap(byte type, Material mat, Player placer, Location location,
			boolean persistant) {
		this.type = type;
		this.mat = mat;
		// this.oldid ; oldid is only filled later on, to preserve a certain
		// degree of freedom in trap types & type sizes
		this.placer = placer;
		this.location = location;
		this.time = new Date(System.currentTimeMillis());
		this.persistant = persistant;
		list.add(this);
		if (!createBlocks(this)) {
			list.remove(this);
		}
	}

	private boolean createBlocks(Trap trap) {

		Location[] locations = this.returnLocations();
		oldmat = new Material[locations.length];
		for (int i = 0; i < locations.length; i++) { // need to run this one firstly, this needs to complete before I can start adding the locations
			if (inverselist.containsKey(locations[i])) {
				placer.sendMessage(ChatColor.RED
						+ "Unable to place trap here, there's an other one in the way");
				return false;
			}
		}
		for (int i = 0; i < locations.length; i++) {
			oldmat[i] = locations[i].getBlock().getType(); // initialization of old materials's
			inverselist.put(locations[i], this); // adding the locations to the inverse list
			locations[i].getBlock().setType(mat); // replaceing the block with the trap block
		}
		return false;
	}

	private Location[] returnLocations() { // edit this method to add more ore formations
		// TODO Make this thing read formations from the config file?
		switch (this.type) {
		case 1:
			Location[] temp1 = { this.location.add(0, -1, 0),
					this.location.add(0, -2, 1), this.location.add(-1, -2, 0),
					this.location.add(0, -2, 0) };
			return temp1;
		case 2:
			Location[] temp2 = { this.location.add(0, -1, 0),
					this.location.add(-1, -2, 0), this.location.add(0, -2, 0),
					this.location.add(-1, -1, 0) };
			return temp2; // the 2 basic ones used in the original classes
		case 3:
			Location[] temp3 = { this.location };
			return temp3; // emeralds
		}
		return null;
	}

	private void removeTrap() { // not entirely sure about this, but this should remove the trap object, it also puts the old blocks back in place
		Location[] temp = this.returnLocations();
		for (int i = 0; i < temp.length; i++) {
			temp[i].getBlock().setType(oldmat[i]);
			inverselist.remove(temp[i]);
		}
		list.remove(this);
	}

	public static boolean Menu(CommandSender sender, int page) { // TODO: This part still looks a bit messy
		if (sender instanceof Player) {
			if (sender.hasPermission("fd.trap.remove.self")) { // only permission to remove, and thus to see own traps
				ArrayList<Trap> trapList = new ArrayList<Trap>();
				for (Trap trap : list) {
					if (trap.placer == sender)
						trapList.add(trap);
				}
				if (page >= 1 && (page * 5) >= list.size()) {
					sendMenu(sender, trapList.subList((page - 1) * 5, page * 5));
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Page number is invalid");
					return false;
				}
			} else if (sender.hasPermission("fd.trap.remove.all")) { // permissions to see and remove all traps
				ArrayList<Trap> trapList = new ArrayList<Trap>();
				for (Trap trap : list) {
					trapList.add(trap);
				}
				if (page >= 1 && (page * 5) >= list.size()) {
					sendMenu(sender, trapList.subList((page - 1) * 5, page * 5));
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Page number is invalid");
					return false;
				}
			} else {
				sender.sendMessage(Prefix.getChatPrefix() + ChatColor.RED
						+ " You don't have permission to do that.");
				return false;
			}

		} else {
			sender.sendMessage("No console access");
			return false;
		}

	}

	private static void sendMenu(CommandSender sender, List<Trap> subList) {
		for (Trap object : subList) {
			sender.sendMessage(ChatColor.WHITE
					+ "["
					+ list.indexOf(object)
					+ "]"
					+ DateFormat.getDateInstance(DateFormat.MEDIUM).format(
							(object.time)) + " - Location: "
					+ object.location.getBlockX() + " "
					+ object.location.getBlockY() + " "
					+ object.location.getBlockZ() + " By " + object.placer);
		}
	}

	public static void removeTrap(CommandSender sender, int id) {
		if (id >= 0 && id < list.size()) {
			Trap temp = list.get(id);
			if ( (temp.placer == sender && sender.hasPermission("fd.trap.remove.self") || sender.hasPermission("fd.trap.remove.all"))) {
				Location[] temploc = temp.returnLocations();
				for (Location loc : temploc) {
					inverselist.remove(loc);
				}
				list.remove(id);
			}
		}
	}
}
