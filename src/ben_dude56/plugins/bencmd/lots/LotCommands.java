package ben_dude56.plugins.bencmd.lots;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ben_dude56.plugins.bencmd.BenCmd;
import ben_dude56.plugins.bencmd.Commands;
import ben_dude56.plugins.bencmd.User;

public class LotCommands implements Commands {
	BenCmd plugin;
	Logger log = Logger.getLogger("minecraft");

	public LotCommands(BenCmd instance) {
		plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		User user;
		try {
			user = new User(plugin, (Player) sender);
		} catch (ClassCastException e) {
			user = new User(plugin);
		}
		if (commandLabel.equalsIgnoreCase("lot")) {
			Lot(args, user);
			return true;
		}
		if (commandLabel.equalsIgnoreCase("addguest")) {
			if (args.length == 1) {
				plugin.getServer().dispatchCommand(sender,
						"lot guest +" + args[0]);
			} else if (args.length >= 2) {
				String guest = "";
				for (int i = 1; i < args.length; i++) {
					guest += " +" + args[i];
				}
				plugin.getServer().dispatchCommand(sender,
						"lot guest" + args[0] + guest);
			}
			return true;
		}
		if (commandLabel.equalsIgnoreCase("removeguest")) {
			if (args.length == 1) {
				plugin.getServer().dispatchCommand(sender,
						"lot guest -" + args[0]);
			} else if (args.length >= 2) {
				String guest = "";
				for (int i = 1; i < args.length; i++) {
					guest += " -" + args[i];
				}
				plugin.getServer().dispatchCommand(sender,
						"lot guest" + args[0] + guest);
			}
			return true;
		}
		return false;
	}

	public void Lot(String[] args, User user) {

		if (args.length == 0) {
			user.sendMessage(ChatColor.YELLOW
					+ "Available lot commands: { info, set, advset, extend, advextend, delete, guest, owner, group }");
			return;
		}

		/*
		 * LOT INFO
		 * 
		 * This allows all those with the isLandlord, canCheckLots, or *
		 * permissions to get the info about a certain lot.
		 */
		if (args[0].equalsIgnoreCase("info")
				|| args[0].equalsIgnoreCase("properties")
				|| args[0].equalsIgnoreCase("check")) {
			if (!user.hasPerm("canCheckLots") && !user.hasPerm("isLandlord")) {
				user.sendMessage(ChatColor.RED
						+ "You do not have permission to check lots!");
				return;
			}
			String LotID;
			if (args.length >= 2) {
				LotID = args[1];
				if (LotID.equalsIgnoreCase("current")
						|| LotID.equalsIgnoreCase("here")
						|| LotID.equalsIgnoreCase("this")) {
					if (user.isServer()) {
						user.sendMessage(ChatColor.RED
								+ "The server cannot do that!");
						return;
					}
					Player player = user.getHandle();
					LotID = String.valueOf(plugin.lots.isInLot(player
							.getLocation()));
					if (LotID == "-1") {
						user.sendMessage(ChatColor.RED
								+ "You need to specify a lot!");
						return;
					}
				}
				plugin.lots.sortSubs(LotID);
				if (args.length >= 3)
					LotID = LotID + "," + args[2];
				if (!plugin.lots.lotExists(LotID)) {
					user.sendMessage(ChatColor.RED
							+ "Invalid Lot ID."
							+ ChatColor.YELLOW
							+ " Use '/lot info' to get info about a lot you are currently in.");
					return;
				}
			} else {
				if (user.isServer()) {
					user.sendMessage(ChatColor.RED
							+ "The server cannot do that!");
					return;
				}
				LotID = String.valueOf(plugin.lots.isInLot(user.getHandle()
						.getLocation()));
			}
			if (LotID.equalsIgnoreCase("-1")) {
				user.sendMessage(ChatColor.RED + "You need to specify a lot!");
				return;
			}
			Lot thisLot = plugin.lots.getLot(LotID);
			String lot = thisLot.getLotID();
			String sub = thisLot.getSubID();
			if (thisLot != null) {
				user.sendMessage(ChatColor.GRAY + "Lot ID: " + lot + "  Part: "
						+ sub + "   Total parts: " + (thisLot.getSubs().size()));
				user.sendMessage(ChatColor.GRAY + "Owner: "
						+ thisLot.getOwner() + "    Group: "
						+ thisLot.getLotGroup() + "   Guests: "
						+ thisLot.getGuests().size());
				user.sendMessage(ChatColor.GRAY + "Corner 1 - X: "
						+ thisLot.getCorner1().getX() + "  Y: "
						+ thisLot.getCorner1().getBlockY() + "  Z: "
						+ thisLot.getCorner1().getBlockZ());
				user.sendMessage(ChatColor.GRAY + "Corner 2 - X: "
						+ thisLot.getCorner2().getX() + "  Y: "
						+ thisLot.getCorner2().getBlockY() + "  Z: "
						+ thisLot.getCorner2().getBlockZ());
			}
			return;
		}
		/*
		 * LOT GUESTS
		 * 
		 * Allows a user to... 1) Add new guests, 2) Remove guests, or 3) check
		 * the guest list
		 */
		if (args[0].equalsIgnoreCase("guests")
				|| args[0].equalsIgnoreCase("guest")) {
			if (args.length == 1) {
				if (user.isServer()) {
					user.sendMessage(ChatColor.RED
							+ "The server cannot do that!");
					return;
				}
				Player player = user.getHandle();
				String LotID = String.valueOf(plugin.lots.isInLot(player
						.getLocation()));
				if (LotID == "-1") {
					user.sendMessage(ChatColor.RED
							+ "You are not standing inside a lot!");
					return;
				}
				Lot lot = plugin.lots.getLot(LotID);
				if (!user.hasPerm("canCheckLots")
						&& !user.hasPerm("isLandlord")
						&& !lot.isOwner(user.getHandle())) {
					user.sendMessage(ChatColor.RED
							+ "You do not have permission to check lots!");
					return;
				}
				lot.listGuests(user);
				return;
			}
			if (!args[1].startsWith("+") && !args[1].startsWith("-")) {
				String LotID = args[1];
				if (LotID.equalsIgnoreCase("current")
						|| LotID.equalsIgnoreCase("here")
						|| LotID.equalsIgnoreCase("this")
						|| LotID.equalsIgnoreCase("clear")) {
					if (user.isServer()) {
						user.sendMessage(ChatColor.RED
								+ "The server cannot do that!");
						return;
					}
					Player player = user.getHandle();
					LotID = plugin.lots.ownsHere(player, player.getLocation());
					if (LotID.equalsIgnoreCase("false")) {
						user.sendMessage(ChatColor.RED
								+ "You do not own this lot!");
						return;
					}
					if (LotID.equalsIgnoreCase("noLot")) {
						user.sendMessage(ChatColor.RED
								+ "You are not standing inside a lot!");
						return;
					}
					if (LotID.equalsIgnoreCase("noUser")) {
						user.sendMessage(ChatColor.RED
								+ "Umm... you appear to not exist...");
						return;
					}
				}
				Lot lot;
				if (plugin.lots.lotExists(LotID)) {
					lot = plugin.lots.getLot(LotID);
				} else {
					user.sendMessage("Invalid lot ID!");
					return;
				}
				if (args[1].equalsIgnoreCase("clear")) {
					int size = lot.getGuests().size();
					if (lot.clearGuests())
						user.sendMessage(ChatColor.GREEN + "" + size
								+ " guests removed.");
					else
						user.sendMessage(ChatColor.RED
								+ "An error has occured!");
					return;
				} else if (args.length >= 3
						&& args[2].equalsIgnoreCase("clear")) {
					int size = lot.getGuests().size();
					if (lot.clearGuests())
						user.sendMessage(ChatColor.GREEN + "" + size
								+ " guests removed.");
					else
						user.sendMessage(ChatColor.RED
								+ "An error has occured!");
					return;
				}
				if (args.length == 2) {
					if (user.hasPerm("isLandlord")
							|| user.hasPerm("canCheckLots")
							|| lot.isOwner(user.getHandle())) {
						plugin.lots.getLot(LotID).listGuests(user);
						return;
					}
				}

				boolean commandsReady = false;
				for (String str : args) {
					if (!commandsReady) {
						commandsReady = true;
						continue;
					}
					if (str.startsWith("+")) {
						if (!user.hasPerm("isLandlord")
								&& !lot.isOwner(user.getHandle())) {
							user.sendMessage(ChatColor.RED
									+ "You don't have permission to edit this lot!");
							return;
						}
						str = str.replaceFirst("\\+", "");
						Lot Lot = plugin.lots.getLot(LotID);
						if (Lot.isGuest(str)) {
							user.sendMessage(ChatColor.RED + "'" + str
									+ "' is already a guest of lot "
									+ LotID.split(",")[0]);
						} else {
							plugin.lots.getLot(LotID).addGuest(str);
							user.sendMessage(ChatColor.GREEN + "'" + str
									+ "' is now a guest of lot "
									+ LotID.split(",")[0]);
						}

					} else if (str.startsWith("-")) {
						if (!user.hasPerm("isLandlord")
								&& !lot.isOwner(user.getHandle())) {
							user.sendMessage(ChatColor.RED
									+ "You don't have permission to this lot!");
							return;
						}
						str = str.replaceFirst("-", "");
						Lot Lot = plugin.lots.getLot(LotID);
						if (!Lot.isGuest(str)) {
							user.sendMessage(ChatColor.RED + "'" + str
									+ "' is not a guest of lot "
									+ LotID.split(",")[0]);
						} else {
							user.sendMessage(ChatColor.GREEN + "'" + str
									+ "' is no longer a guest of lot "
									+ LotID.split(",")[0]);
							plugin.lots.getLot(LotID).deleteGuest(str);
						}
					}
				}
			} else {
				if (user.isServer()) {
					user.sendMessage(ChatColor.RED
							+ "The server needs to specify a lot!");
					return;
				}
				String LotID = "";
				Player player = user.getHandle();
				LotID = plugin.lots.ownsHere(player, player.getLocation());
				if (LotID.equalsIgnoreCase("false")) {
					user.sendMessage(ChatColor.RED + "You do not own this lot!");
					return;
				}
				if (LotID.equalsIgnoreCase("noLot")) {
					user.sendMessage(ChatColor.RED
							+ "You are not standing inside a lot!");
					return;
				}
				if (LotID.equalsIgnoreCase("noUser")) {
					user.sendMessage(ChatColor.RED
							+ "Umm... you appear to not exist...");
					return;
				}
				Lot lot = plugin.lots.getLot(LotID);
				boolean commandsReady = false;
				for (String str : args) {
					if (!commandsReady) {
						commandsReady = true;
						continue;
					}
					if (str.startsWith("+")) {
						if (!user.hasPerm("isLandlord")
								&& !lot.isOwner(user.getHandle())) {
							user.sendMessage(ChatColor.RED
									+ "You don't have permission to edit this lot!");
							return;
						}
						str = str.replaceFirst("\\+", "");
						Lot Lot = plugin.lots.getLot(LotID);
						if (Lot.isGuest(str)) {
							user.sendMessage(ChatColor.RED + "'" + str
									+ "' is already a guest of lot "
									+ LotID.split(",")[0]);
						} else {
							plugin.lots.getLot(LotID).addGuest(str);
							user.sendMessage(ChatColor.GREEN + "'" + str
									+ "' is now a guest of lot "
									+ LotID.split(",")[0]);
						}

					} else if (str.startsWith("-")) {
						if (!user.hasPerm("isLandlord")
								&& !lot.isOwner(user.getHandle())) {
							user.sendMessage(ChatColor.RED
									+ "You don't have permission to this lot!");
							return;
						}
						str = str.replaceFirst("-", "");
						Lot Lot = plugin.lots.getLot(LotID);
						if (!Lot.isGuest(str)) {
							user.sendMessage(ChatColor.RED + "'" + str
									+ "' is not a guest of lot "
									+ LotID.split(",")[0]);
						} else {
							user.sendMessage(ChatColor.GREEN + "'" + str
									+ "' is no longer a guest of lot "
									+ LotID.split(",")[0]);
							plugin.lots.getLot(LotID).deleteGuest(str);
						}
					}
				}
			}
			return;
		}

		/*
		 * LOT SET
		 * 
		 * This creates a new lot using the corners marked off with the wooden
		 * shovel.
		 */
		if (args[0].equalsIgnoreCase("set")
				|| args[0].equalsIgnoreCase("create")
				|| args[0].equalsIgnoreCase("new")) {
			if (user.isServer()) {
				user.sendMessage(ChatColor.RED + "The server cannot do that!");
				return;
			}
			if (!user.hasPerm("isLandlord")) {
				user.sendMessage(ChatColor.RED
						+ "You don't have permission to modify lots!");
				return;
			}
			Location corner1;
			Location corner2;
			String owner, group;
			if (!plugin.lotListener.corner.containsKey(user.getName())) {
				user.sendMessage(ChatColor.RED
						+ "You need to mark off some corners first!");
				user.sendMessage(ChatColor.RED
						+ "Be sure to use a wooden shovel!");
				return;
			}
			boolean cor1 = plugin.lotListener.corner.get(user.getName()).corner1set;
			boolean cor2 = plugin.lotListener.corner.get(user.getName()).corner2set;
			if (cor1 && cor2) {
				String LotID = plugin.lots.getNextID();
				corner1 = plugin.lotListener.corner.get(user.getName())
						.getCorner1();
				corner2 = plugin.lotListener.corner.get(user.getName())
						.getCorner2();
				if (args.length >= 2) {
					owner = args[1];
				} else {
					owner = user.getName();
				}
				if (!plugin.perm.groupFile.groupExists(plugin.mainProperties
						.getString("AdminGroup", "admin"))) {
					plugin.perm.groupFile.addGroup(plugin.mainProperties
							.getString("AdminGroup", "admin"));
				}
				group = plugin.mainProperties.getString("AdminGroup", "admin");
				plugin.lots.addLot(LotID, corner1, corner2, owner, group);
				LotID = plugin.lots.getLot(LotID).getLotID();
				user.sendMessage(ChatColor.GREEN + "Lot " + LotID
						+ " was successfully created with owner " + owner);
				return;
			} else {
				if (!cor1 && !cor2)
					user.sendMessage(ChatColor.RED
							+ "You need to mark off some corners first!");
				if (!cor1 && cor2)
					user.sendMessage(ChatColor.RED
							+ "You need to mark off CORNER 1! (Left-click)");
				if (cor1 && !cor2)
					user.sendMessage(ChatColor.RED
							+ "You need to mark off CORNER 2! (Right-click)");
				user.sendMessage(ChatColor.RED
						+ "Be sure to use a wooden shovel!");
			}
			return;
		}

		/*
		 * LOT ADVANCED SET
		 * 
		 * This allows landlords to lot off vertically more blocks then they had
		 * originally marked off.
		 */
		if (args[0].equalsIgnoreCase("advset")) {
			if (user.isServer()) {
				user.sendMessage(ChatColor.RED + "The server cannot do that!");
				return;
			}
			if (!user.hasPerm("isLandlord")) {
				user.sendMessage(ChatColor.RED
						+ "You don't have permission to modify lots!");
				return;
			}
			Location corner1;
			Location corner2;
			String owner, group;
			if (!plugin.lotListener.corner.containsKey(user.getName())) {
				user.sendMessage(ChatColor.RED
						+ "You need to mark off some corners first!");
				user.sendMessage(ChatColor.RED
						+ "Be sure to use a wooden shovel!");
				return;
			}
			boolean cor1 = plugin.lotListener.corner.get(user.getName()).corner1set;
			boolean cor2 = plugin.lotListener.corner.get(user.getName()).corner2set;
			int up, down;
			if (cor1 && cor2) {

				String LotID = plugin.lots.getNextID();
				corner1 = plugin.lotListener.corner.get(user.getName())
						.getCorner1();
				corner2 = plugin.lotListener.corner.get(user.getName())
						.getCorner2();
				if (args.length >= 4) {
					try {
						up = Integer.parseInt(args[1]);
						down = Integer.parseInt(args[2]);
					} catch (NumberFormatException e) {
						user.sendMessage(ChatColor.RED
								+ "Be sure to use ONLY INTEGERS when specifying up or down!");
						return;
					}
					owner = args[3];
				} else if (args.length >= 3) {
					try {
						up = Integer.parseInt(args[1]);
						down = Integer.parseInt(args[2]);
					} catch (NumberFormatException e) {
						user.sendMessage(ChatColor.RED
								+ "Be sure to use ONLY INTEGERS when specifying up or down!");
						return;
					}
					owner = user.getName();
				} else {
					user.sendMessage(ChatColor.YELLOW
							+ "Proper use is /lot advset <up integer> <down integer> (lot owner)");
					return;
				}
				if (up < -1 || down < -1) {
					user.sendMessage(ChatColor.RED
							+ "Do not use negative integers except for -1!");
				}

				int oldY1 = corner1.getBlockY();
				int oldY2 = corner2.getBlockY();
				if (up > 0) {
					if (corner1.getBlockY() >= corner2.getBlockY()) {
						corner1.setY(corner1.getY() + up);
					} else {
						corner2.setY(corner2.getY() + up);
					}
				} else if (up == -1) {
					if (corner1.getBlockY() >= corner2.getBlockY()) {
						corner1.setY(128);
					} else {
						corner2.setY(128);
					}
				}

				if (down > 0) {
					if (corner1.getBlockY() <= corner2.getBlockY()) {
						corner1.setY(corner1.getY() - down);
					} else {
						corner2.setY(corner2.getY() - down);
					}
				} else if (down == -1) {
					if (corner1.getBlockY() <= corner2.getBlockY()) {
						corner1.setY(0);
					} else {
						corner2.setY(0);
					}
				}
				if (!plugin.perm.groupFile.groupExists(plugin.mainProperties
						.getString("AdminGroup", "admin"))) {
					plugin.perm.groupFile.addGroup(plugin.mainProperties
							.getString("AdminGroup", "admin"));
				}
				group = plugin.mainProperties.getString("AdminGroup", "admin");
				plugin.lots.addLot(LotID, corner1, corner2, owner, group);
				LotID = plugin.lots.getLot(LotID).getLotID();
				user.sendMessage(ChatColor.GREEN + "Lot " + LotID
						+ " was successfully created with owner " + owner);
				corner1.setY(oldY1);
				corner2.setY(oldY2);
				return;
			} else {
				if (!cor1 && !cor2)
					user.sendMessage(ChatColor.RED
							+ "You need to mark off some corners first!");
				if (!cor1 && cor2)
					user.sendMessage(ChatColor.RED
							+ "You need to mark off CORNER 1! (Left-click)");
				if (cor1 && !cor2)
					user.sendMessage(ChatColor.RED
							+ "You need to mark off CORNER 2! (Right-click)");
				user.sendMessage(ChatColor.RED
						+ "Be sure to use a wooden shovel!");
			}
			return;
		}
		/*
		 * LOT DELETE
		 * 
		 * Deletes the specified lot
		 * 
		 * If no lot is specified, then the lot the player is currently standing
		 * in is deleted.
		 */
		if (args[0].equalsIgnoreCase("delete")
				|| args[0].equalsIgnoreCase("remove")) {
			if (!user.hasPerm("isLandlord")) {
				user.sendMessage(ChatColor.RED
						+ "You don't have permission to edit lots!");
				return;
			}
			String LotID;
			String SubID = "-1";
			if (args.length == 1) {
				LotID = "this";
			} else {
				LotID = args[1];
			}

			if (args.length >= 3) {
				SubID = args[2];
			}

			if (LotID.equalsIgnoreCase("current")
					|| LotID.equalsIgnoreCase("here")
					|| LotID.equalsIgnoreCase("this")) {
				if (user.isServer()) {
					user.sendMessage(ChatColor.RED
							+ "The server cannot do that!");
					return;
				}
				Player player = user.getHandle();
				LotID = String.valueOf(
						plugin.lots.isInLot(player.getLocation())).split(",")[0];
				if (LotID == "-1") {
					user.sendMessage(ChatColor.RED + "You're not inside a lot!");
					return;
				}
			}
			if (!SubID.equalsIgnoreCase("-1")) {
				if (plugin.lots.lotExists((LotID + "," + SubID))) {
					if (plugin.lots.deleteLot((LotID + "," + SubID))) {
						user.sendMessage(ChatColor.GREEN + "Lot " + LotID
								+ ", part " + SubID
								+ " was successfully deleted.");
						return;
					} else {
						user.sendMessage(ChatColor.RED
								+ "Something went wrong when deleting the lot.");
						return;
					}
				} else {
					user.sendMessage(ChatColor.RED + "Lot " + LotID + ", part "
							+ SubID + " does not exist!");
				}
				return;
			} else if (plugin.lots.lotExists(LotID)) {
				int size = plugin.lots.getLot(LotID).getSubs().size();
				if (plugin.lots.deleteLot(LotID)) {
					user.sendMessage(ChatColor.GREEN + "Lot " + LotID
							+ " was successfully deleted. (" + size + " parts)");
					return;
				} else {
					user.sendMessage(ChatColor.RED
							+ "Something went wrong when deleting the lot.");
					return;
				}
			} else {
				user.sendMessage(ChatColor.RED + "Lot " + LotID
						+ " does not exist!");
			}
			return;
		}

		/*
		 * LOT SET OWNER
		 * 
		 * Sets the lot owner.
		 */
		if (args[0].equalsIgnoreCase("setowner")
				|| args[0].equalsIgnoreCase("owner")
				|| args[0].equalsIgnoreCase("newowner")) {
			if (!user.hasPerm("isLandlord")) {
				user.sendMessage(ChatColor.RED
						+ "You don't have permission to edit lots!");
				return;
			}
			if (args.length == 1) {
				user.sendMessage(ChatColor.YELLOW + "Correct usage is: /lot "
						+ args[0] + " <New Owner> <Lot ID>");
				return;
			}
			if (args.length >= 2) {
				String LotID, Owner;
				Owner = args[1];
				if (args.length >= 3) {
					LotID = args[2];
					if (LotID.equalsIgnoreCase("here")
							|| LotID.equalsIgnoreCase("current")
							|| LotID.equalsIgnoreCase("this")) {
						if (user.isServer()) {
							user.sendMessage(ChatColor.RED
									+ "The server needs to specify a lot!");
							return;
						}
						LotID = String.valueOf(plugin.lots.isInLot(user
								.getHandle().getLocation()));
						if (LotID == "-1") {
							user.sendMessage(ChatColor.RED
									+ "You're not inside a lot!");
							return;
						}
					}
				} else {
					if (user.isServer()) {
						user.sendMessage(ChatColor.RED
								+ "The server needs to specify a lot!");
						return;
					}
					LotID = String.valueOf(plugin.lots.isInLot(user.getHandle()
							.getLocation()));
					if (LotID == "-1") {
						user.sendMessage(ChatColor.RED
								+ "You're not inside a lot!");
						return;
					}
				}
				plugin.lots.getLot(LotID).setOwner(Owner);
				user.sendMessage(ChatColor.GREEN + Owner + " now owns lot "
						+ LotID);
				return;
			}
		}
		/*
		 * LOT LIST
		 * 
		 * Returns all the lots in the lots database
		 */
		if (args[0].equalsIgnoreCase("list")) {

			if (!user.hasPerm("isLandlord")) {
				user.sendMessage(ChatColor.RED + "You cannot do that!");
				return;
			}

			if (args.length == 1) {
				int size = 0;
				for (String key : plugin.lots.lot.keySet()) {
					plugin.lots.sortSubs(key.split(",")[0]);
					if (key.split(",")[1].equalsIgnoreCase("0"))
						size++;
				}
				int[] Lot = new int[size];
				int total = plugin.lots.lot.size();
				int i = 0;
				for (String key : plugin.lots.lot.keySet()) {
					if (key.split(",")[1].equalsIgnoreCase("0")) {
						Lot[i] = Integer.parseInt((String) key.split(",")[0]);
						i++;
					}
				}
				plugin.lots.selectionSort(Lot);
				String list = "";
				i = 0;
				int r = 0;
				user.sendMessage(ChatColor.GRAY + "Registered lots:  " + size
						+ "   Total Parts:  " + total);
				for (int key : Lot) {
					String LotIDString = String.valueOf(key);
					list += LotIDString;
					i++;
					r++;
					if (r < Lot.length)
						list += ",  ";
					else
						list += ".";
					if (i >= 12) {
						user.sendMessage(ChatColor.GRAY + list);
						i = 0;
						list = "";
					}
				}
				if (!list.equalsIgnoreCase("")) {
					user.sendMessage(ChatColor.GRAY + list);
				}
				return;
			}

			// Only passes if args.length >= 2

			// Lists lots by USER'S LOCATION

			if (args[1].equalsIgnoreCase("location")
					|| args[1].equalsIgnoreCase("here")) {

				Player player = user.getHandle();
				Location location = player.getLocation();
				int total = 0;

				if (plugin.lots.isInLot(location).equalsIgnoreCase("-1")) {
					user.sendMessage(ChatColor.RED
							+ "You are not standing inside a lot");
					return;
				} else {

					for (String LotID : plugin.lots.lot.keySet()) {
						if (plugin.lots.getLot(LotID).withinLot(location)) {
							total++;
						}
					}

					user.sendMessage(ChatColor.YELLOW
							+ "You are standing in the following lots: ("
							+ total + ")");
				}

				String list = "";
				int i = 0, r = 0;
				List<String> usedIDs = new ArrayList<String>();
				for (String LotID : plugin.lots.lot.keySet()) {
					if (!plugin.lots.getLot(LotID).withinLot(location)) {
						continue;
					}
					LotID = LotID.split(",")[0];
					if (!usedIDs.contains(LotID)) {
						usedIDs.add(LotID);
						list += LotID;
						i++;
						r++;
						if (r < total)
							list += ",  ";
						else
							list += ".";
						if (i >= 3) {
							user.sendMessage(ChatColor.GRAY + list);
							i = 0;
							list = "";
						}
					}
				}
				if (!list.equalsIgnoreCase("")) {
					user.sendMessage(ChatColor.YELLOW + list);
				}
				return;
			}

			return;
		}

		/*
		 * LOT GROUP
		 * 
		 * Sets the group that is allowed to edit the lot.
		 */

		if (args[0].equalsIgnoreCase("group")) {
			String LotID, group;
			Player player = user.getHandle();
			if (args.length < 2) {
				user.sendMessage(ChatColor.YELLOW
						+ "Correct usage is: /lot group {group name} [lot id]");
				return;
			}
			if (args.length == 2) {
				group = args[1];
				LotID = plugin.lots.ownsHere(player, player.getLocation());
				if (LotID.equalsIgnoreCase("false")) {
					user.sendMessage(ChatColor.RED + "You do not own this lot!");
					return;
				}
				if (LotID.equalsIgnoreCase("noLot")) {
					user.sendMessage(ChatColor.RED
							+ "You are not standing inside a lot!");
					return;
				}
				if (LotID.equalsIgnoreCase("noUser")) {
					user.sendMessage(ChatColor.RED
							+ "Umm... you appear to not exist...");
					return;
				}
				Lot lot = plugin.lots.getLot(LotID);
				lot.setGroup(group);
				user.sendMessage(ChatColor.GREEN + "Lot " + LotID.split(",")[0]
						+ "'s group has been seet to '" + group + "'.");
				return;
			} else if (args.length >= 3) {
				group = args[1];
				LotID = args[2];
				if (LotID.equalsIgnoreCase("current")
						|| LotID.equalsIgnoreCase("here")
						|| LotID.equalsIgnoreCase("this")) {
					if (user.isServer()) {
						user.sendMessage(ChatColor.RED
								+ "The server cannot do that!");
						return;
					}
					LotID = plugin.lots.ownsHere(player, player.getLocation());
					if (LotID.equalsIgnoreCase("false")) {
						user.sendMessage(ChatColor.RED
								+ "You do not own this lot!");
						return;
					}
					if (LotID.equalsIgnoreCase("noLot")) {
						user.sendMessage(ChatColor.RED
								+ "You are not standing inside a lot!");
						return;
					}
					if (LotID.equalsIgnoreCase("noUser")) {
						user.sendMessage(ChatColor.RED
								+ "Umm... you appear to not exist...");
						return;
					}
				} else {
					if (!plugin.lots.lotExists(LotID)) {
						user.sendMessage(ChatColor.RED + "Lot does not exist!");
						return;
					}
				}
				Lot lot = plugin.lots.getLot(LotID);
				if (!user.hasPerm("isLandlord") && !lot.isOwner(player)) {
					user.sendMessage(ChatColor.RED
							+ "You do not have permission to do that!");
					return;
				}
				lot.setGroup(group);
				LotID = lot.getLotID();
				user.sendMessage(ChatColor.GREEN + "Lot " + LotID
						+ "'s group has been seet to '" + group + "'.");
				return;
			} else {
				user.sendMessage(ChatColor.RED
						+ "Some weird error has occured.");
				return;
			}
		}
		/*
		 * EXTEND LOT
		 * 
		 * This command adds a sub-lot to the defined lot.
		 */

		if (args[0].equalsIgnoreCase("extend")
				|| args[0].equalsIgnoreCase("add")
				|| args[0].equalsIgnoreCase("newpart")
				|| args[0].equalsIgnoreCase("ext")) {
			if (user.isServer()) {
				user.sendMessage(ChatColor.RED + "The server cannot do that!");
				return;
			}
			if (!user.hasPerm("isLandlord")) {
				user.sendMessage(ChatColor.RED
						+ "You don't have permission to modify lots!");
				return;
			}
			Location corner1;
			Location corner2;
			String LotID;
			if (!plugin.lotListener.corner.containsKey(user.getName())) {
				user.sendMessage(ChatColor.RED
						+ "You need to mark off some corners first!");
				user.sendMessage(ChatColor.RED
						+ "Be sure to use a wooden shovel!");
				return;
			}
			Player player = user.getHandle();
			if (args.length >= 2)
				LotID = args[1];
			else
				LotID = "this";
			if (LotID.equalsIgnoreCase("current")
					|| LotID.equalsIgnoreCase("here")
					|| LotID.equalsIgnoreCase("this")) {

				LotID = String
						.valueOf(plugin.lots.isInLot(player.getLocation()));
				if (LotID == "-1") {
					user.sendMessage(ChatColor.RED
							+ "You need to specify a lot!");
					return;
				}
				if (LotID.split(",").length == 2) {
					LotID = LotID.split(",")[0];
				}
			} else {
				if (!plugin.lots.lotExists(LotID)) {
					user.sendMessage(ChatColor.RED + "Lot does not exist!");
					return;
				}
			}
			boolean cor1 = plugin.lotListener.corner.get(user.getName()).corner1set;
			boolean cor2 = plugin.lotListener.corner.get(user.getName()).corner2set;
			if (cor1 && cor2) {
				corner1 = plugin.lotListener.corner.get(user.getName())
						.getCorner1();
				corner2 = plugin.lotListener.corner.get(user.getName())
						.getCorner2();
				String group = plugin.lots.getLot(LotID).getLotGroup();
				String owner = plugin.lots.getLot(LotID).getOwner();
				plugin.lots.sortSubs(LotID);
				String SubID = plugin.lots.getNextSubID(LotID);
				plugin.lots.addLot(LotID + "," + SubID, corner1, corner2,
						owner, group);
				LotID = plugin.lots.getLot(LotID).getLotID();
				int SubSize = plugin.lots.getLot(LotID).getSubs().size();
				user.sendMessage(ChatColor.GREEN + "Lot " + LotID
						+ " was successfully extended to part " + SubID
						+ ".  (" + SubSize + " total parts)");
				return;
			} else {
				if (!cor1 && !cor2)
					user.sendMessage(ChatColor.RED
							+ "You need to mark off some corners first!");
				if (!cor1 && cor2)
					user.sendMessage(ChatColor.RED
							+ "You need to mark off CORNER 1! (Left-click)");
				if (cor1 && !cor2)
					user.sendMessage(ChatColor.RED
							+ "You need to mark off CORNER 2! (Right-click)");
				user.sendMessage(ChatColor.RED
						+ "Be sure to use a wooden shovel!");
			}
			return;
		}
		/*
		 * This allows the user to change the y-coordinates of the extension
		 * being created
		 */
		if (args[0].equalsIgnoreCase("advextend")
				|| args[0].equalsIgnoreCase("advadd")
				|| args[0].equalsIgnoreCase("advnewpart")
				|| args[0].equalsIgnoreCase("advext")) {
			if (user.isServer()) {
				user.sendMessage(ChatColor.RED + "The server cannot do that!");
				return;
			}
			if (!user.hasPerm("isLandlord")) {
				user.sendMessage(ChatColor.RED
						+ "You don't have permission to modify lots!");
				return;
			}
			Player player = user.getHandle();
			Location corner1;
			Location corner2;
			String LotID;
			if (!plugin.lotListener.corner.containsKey(user.getName())) {
				user.sendMessage(ChatColor.RED
						+ "You need to mark off some corners first!");
				user.sendMessage(ChatColor.RED
						+ "Be sure to use a wooden shovel!");
				return;
			}
			boolean cor1 = plugin.lotListener.corner.get(user.getName()).corner1set;
			boolean cor2 = plugin.lotListener.corner.get(user.getName()).corner2set;
			int up, down;
			if (cor1 && cor2) {
				corner1 = plugin.lotListener.corner.get(user.getName())
						.getCorner1();
				corner2 = plugin.lotListener.corner.get(user.getName())
						.getCorner2();
				if (args.length >= 4) {
					try {
						up = Integer.parseInt(args[1]);
						down = Integer.parseInt(args[2]);
					} catch (NumberFormatException e) {
						user.sendMessage(ChatColor.RED
								+ "Be sure to use ONLY INTEGERS when specifying up or down!");
						return;
					}
					LotID = args[3];
				} else if (args.length == 3) {
					try {
						up = Integer.parseInt(args[1]);
						down = Integer.parseInt(args[2]);
					} catch (NumberFormatException e) {
						user.sendMessage(ChatColor.RED
								+ "Be sure to use ONLY INTEGERS when specifying up or down!");
						return;
					}
					LotID = "this";
				} else {
					user.sendMessage(ChatColor.YELLOW
							+ "Proper use is /lot advset <up integer> <down integer> (lot ID)");
					return;
				}
				if (up < -1 || down < -1) {
					user.sendMessage(ChatColor.RED
							+ "Do not use negative integers except for -1!");
				}
				LotID = LotID.split(",")[0];
				if (LotID.equalsIgnoreCase("current")
						|| LotID.equalsIgnoreCase("here")
						|| LotID.equalsIgnoreCase("this")) {

					LotID = String.valueOf(plugin.lots.isInLot(player
							.getLocation()));
					if (LotID == "-1") {
						user.sendMessage(ChatColor.RED
								+ "You need to specify a lot!");
						return;
					}
					if (LotID.split(",").length == 2) {
						LotID = LotID.split(",")[0];
					}
				} else {
					if (!plugin.lots.lotExists(LotID)) {
						user.sendMessage(ChatColor.RED + "Lot does not exist!");
						return;
					}
				}
				int oldY1 = corner1.getBlockY();
				int oldY2 = corner2.getBlockY();
				if (up > 0) {
					if (corner1.getBlockY() >= corner2.getBlockY()) {
						corner1.setY(corner1.getY() + up);
					} else {
						corner2.setY(corner2.getY() + up);
					}
				} else if (up == -1) {
					if (corner1.getBlockY() >= corner2.getBlockY()) {
						corner1.setY(128);
					} else {
						corner2.setY(128);
					}
				}

				if (down > 0) {
					if (corner1.getBlockY() <= corner2.getBlockY()) {
						corner1.setY(corner1.getY() - down);
					} else {
						corner2.setY(corner2.getY() - down);
					}
				} else if (down == -1) {
					if (corner1.getBlockY() <= corner2.getBlockY()) {
						corner1.setY(0);
					} else {
						corner2.setY(0);
					}
				}
				String owner = plugin.lots.getLot(LotID).getOwner();
				String group = plugin.lots.getLot(LotID).getLotGroup();
				plugin.lots.sortSubs(LotID);
				String SubID = plugin.lots.getNextSubID(LotID);
				plugin.lots.addLot(LotID + "," + SubID, corner1, corner2,
						owner, group);
				int SubSize = plugin.lots.getLot(LotID).getSubs().size();
				user.sendMessage(ChatColor.GREEN + "Lot " + LotID
						+ " was successfully extended to part " + SubID
						+ ".  (" + SubSize + " total parts)");
				corner1.setY(oldY1);
				corner2.setY(oldY2);
				return;
			} else {
				if (!cor1 && !cor2)
					user.sendMessage(ChatColor.RED
							+ "You need to mark off some corners first!");
				if (!cor1 && cor2)
					user.sendMessage(ChatColor.RED
							+ "You need to mark off CORNER 1! (Left-click)");
				if (cor1 && !cor2)
					user.sendMessage(ChatColor.RED
							+ "You need to mark off CORNER 2! (Right-click)");
				user.sendMessage(ChatColor.RED
						+ "Be sure to use a wooden shovel!");
			}
			return;
		}

		/*
		 * Alerts the user to an invalid command.
		 */

		user.sendMessage(ChatColor.RED + "Unknown lot command!"
				+ ChatColor.YELLOW + " Available Commands:");
		user.sendMessage(ChatColor.YELLOW
				+ "set, advset, delete, guest, info, setowner, group");
		return;
	}
}
