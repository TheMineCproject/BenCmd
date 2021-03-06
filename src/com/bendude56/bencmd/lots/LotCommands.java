package com.bendude56.bencmd.lots;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bendude56.bencmd.BenCmd;
import com.bendude56.bencmd.Commands;
import com.bendude56.bencmd.User;
import com.bendude56.bencmd.invtools.BCItem;
import com.bendude56.bencmd.invtools.InventoryBackend;
import com.bendude56.bencmd.listener.BenCmdPlayerListener;
import com.bendude56.bencmd.lots.sparea.DamageArea;
import com.bendude56.bencmd.lots.sparea.DropInfo;
import com.bendude56.bencmd.lots.sparea.DropTable;
import com.bendude56.bencmd.lots.sparea.GroupArea;
import com.bendude56.bencmd.lots.sparea.HealArea;
import com.bendude56.bencmd.lots.sparea.MsgArea;
import com.bendude56.bencmd.lots.sparea.PVPArea;
import com.bendude56.bencmd.lots.sparea.SPArea;
import com.bendude56.bencmd.lots.sparea.TRArea;
import com.bendude56.bencmd.lots.sparea.TimedArea;
import com.bendude56.bencmd.permissions.PermissionGroup;

public class LotCommands implements Commands {
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		User user = User.getUser(sender);
		if (commandLabel.equalsIgnoreCase("lot")) {
			Lot(args, user);
			return true;
		} else if (commandLabel.equalsIgnoreCase("area") && user.hasPerm("bencmd.area.command")) {
			Area(args, user);
			return true;
		}
		if (commandLabel.equalsIgnoreCase("addguest")) {
			if (args.length == 1) {
				Bukkit.dispatchCommand(sender, "lot guest +" + args[0]);
			} else if (args.length >= 2) {
				String guest = "";
				for (int i = 1; i < args.length; i++) {
					guest += " +" + args[i];
				}
				Bukkit.dispatchCommand(sender, "lot guest" + args[0] + guest);
			}
			return true;
		}
		if (commandLabel.equalsIgnoreCase("removeguest")
				|| commandLabel.equalsIgnoreCase("remguest")) {
			if (args.length == 1) {
				Bukkit.dispatchCommand(sender, "lot guest -" + args[0]);
			} else if (args.length >= 2) {
				String guest = "";
				for (int i = 1; i < args.length; i++) {
					guest += " -" + args[i];
				}
				Bukkit.dispatchCommand(sender, "lot guest" + args[0] + guest);
			}
			return true;
		}
		return false;
	}

	// TODO Localize
	public void Lot(String[] args, User user) {

		if (args.length == 0) {
			user.sendMessage(ChatColor.YELLOW + "Available lot commands: { info, set, advset, extend, advextend, delete, guest, owner, group }");
			return;
		}

		/*
		 * LOT INFO
		 * 
		 * This allows all those with the isLandlord, canCheckLots, or *
		 * permissions to get the info about a certain lot.
		 */
		if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("properties") || args[0].equalsIgnoreCase("check")) {
			if (!user.hasPerm("bencmd.lot.info")) {
				BenCmd.getPlugin().logPermFail(user, "lot", args, true);
				return;
			}
			String LotID;
			if (args.length >= 2) {
				LotID = args[1];
				if (LotID.equalsIgnoreCase("current") || LotID.equalsIgnoreCase("here") || LotID.equalsIgnoreCase("this")) {
					if (user.isServer()) {
						BenCmd.getLocale().sendMessage(user, "basic.noServerUse");
						return;
					}
					Player player = ((Player) user.getHandle());
					LotID = String.valueOf(BenCmd.getLots().isInLot(player.getLocation()));
					if (LotID == "-1") {
						user.sendMessage(ChatColor.RED + "You need to specify a lot!");
						return;
					}
				}
				BenCmd.getLots().sortSubs(LotID);
				if (args.length >= 3)
					LotID = LotID + "," + args[2];
				if (!BenCmd.getLots().lotExists(LotID)) {
					user.sendMessage(ChatColor.RED + "Invalid Lot ID." + ChatColor.YELLOW + " Use '/lot info' to get info about a lot you are currently in.");
					return;
				}
			} else {
				if (user.isServer()) {
					BenCmd.getLocale().sendMessage(user, "basic.noServerUse");
					return;
				}
				LotID = String.valueOf(BenCmd.getLots().isInLot(((Player) user.getHandle()).getLocation()));
			}
			if (LotID.equalsIgnoreCase("-1")) {
				user.sendMessage(ChatColor.RED + "You need to specify a lot!");
				return;
			}
			Lot thisLot = BenCmd.getLots().getLot(LotID);
			String lot = thisLot.getLotID();
			String sub = thisLot.getSubID();
			if (thisLot != null) {
				user.sendMessage(ChatColor.GRAY + "Lot ID: " + lot + "  Part: " + sub + "   Total parts: " + (thisLot.getSubs().size()) + "   World: " + thisLot.getWorld().getName());
				user.sendMessage(ChatColor.GRAY + "Owner: " + thisLot.getOwner() + "    Group: " + thisLot.getLotGroup() + "   Guests: " + thisLot.getGuests().size());
				user.sendMessage(ChatColor.GRAY + "Corner 1 - X: " + thisLot.getCorner1().getX() + "  Y: " + thisLot.getCorner1().getBlockY() + "  Z: " + thisLot.getCorner1().getBlockZ());
				user.sendMessage(ChatColor.GRAY + "Corner 2 - X: " + thisLot.getCorner2().getX() + "  Y: " + thisLot.getCorner2().getBlockY() + "  Z: " + thisLot.getCorner2().getBlockZ());
			}
			return;
		}
		/*
		 * LOT GUESTS
		 * 
		 * Allows a user to... 1) Add new guests, 2) Remove guests, or 3) check
		 * the guest list
		 */
		if (args[0].equalsIgnoreCase("guests") || args[0].equalsIgnoreCase("guest")) {
			if (args.length == 1) {
				if (user.isServer()) {
					BenCmd.getLocale().sendMessage(user, "basic.noServerUse");
					return;
				}
				Player player = ((Player) user.getHandle());
				String LotID = String.valueOf(BenCmd.getLots().isInLot(player.getLocation()));
				if (LotID == "-1") {
					user.sendMessage(ChatColor.RED + "You are not standing inside a lot!");
					return;
				}
				Lot lot = BenCmd.getLots().getLot(LotID);
				if (!user.hasPerm("bencmd.lot.info") && !lot.isOwner(((Player) user.getHandle()))) {
					BenCmd.getPlugin().logPermFail(user, "lot", args, true);
					return;
				}
				lot.listGuests(user);
				return;
			}
			if (!args[1].startsWith("+") && !args[1].startsWith("-")) {
				String LotID = args[1];
				if (LotID.equalsIgnoreCase("current") || LotID.equalsIgnoreCase("here") || LotID.equalsIgnoreCase("this") || LotID.equalsIgnoreCase("clear")) {
					if (user.isServer()) {
						BenCmd.getLocale().sendMessage(user, "basic.noServerUse");
						return;
					}
					Player player = ((Player) user.getHandle());
					LotID = BenCmd.getLots().ownsHere(player, player.getLocation());
					if (LotID.equalsIgnoreCase("false")) {
						user.sendMessage(ChatColor.RED + "You do not own this lot!");
						return;
					}
					if (LotID.equalsIgnoreCase("noLot")) {
						user.sendMessage(ChatColor.RED + "You are not standing inside a lot!");
						return;
					}
					if (LotID.equalsIgnoreCase("noUser")) {
						user.sendMessage(ChatColor.RED + "Umm... you appear to not exist...");
						return;
					}
				}
				Lot lot;
				if (BenCmd.getLots().lotExists(LotID)) {
					lot = BenCmd.getLots().getLot(LotID);
				} else {
					user.sendMessage("Invalid lot ID!");
					return;
				}
				if (args[1].equalsIgnoreCase("clear")) {
					int size = lot.getGuests().size();
					if (lot.clearGuests())
						user.sendMessage(ChatColor.GREEN + "" + size + " guests removed.");
					else
						user.sendMessage(ChatColor.RED + "An error has occured!");
					return;
				} else if (args.length >= 3 && args[2].equalsIgnoreCase("clear")) {
					int size = lot.getGuests().size();
					if (lot.clearGuests())
						user.sendMessage(ChatColor.GREEN + "" + size + " guests removed.");
					else
						user.sendMessage(ChatColor.RED + "An error has occured!");
					return;
				}
				if (args.length == 2) {
					if (user.hasPerm("bencmd.lot.info") || lot.isOwner(((Player) user.getHandle()))) {
						BenCmd.getLots().getLot(LotID).listGuests(user);
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
						if (!user.hasPerm("bencmd.lot.guest") && !lot.isOwner(((Player) user.getHandle()))) {
							BenCmd.getLocale().sendMessage(user, "basic.noPermission");
							return;
						}
						str = str.replaceFirst("\\+", "");
						Lot Lot = BenCmd.getLots().getLot(LotID);
						if (Lot.isGuest(str)) {
							user.sendMessage(ChatColor.RED + "'" + str + "' is already a guest of lot " + LotID.split(",")[0]);
						} else {
							BenCmd.getLots().getLot(LotID).addGuest(str);
							user.sendMessage(ChatColor.GREEN + "'" + str + "' is now a guest of lot " + LotID.split(",")[0]);
						}

					} else if (str.startsWith("-")) {
						if (!user.hasPerm("bencmd.lot.guest") && !lot.isOwner(((Player) user.getHandle()))) {
							BenCmd.getLocale().sendMessage(user, "basic.noPermission");
							return;
						}
						str = str.replaceFirst("-", "");
						Lot Lot = BenCmd.getLots().getLot(LotID);
						if (!Lot.isGuest(str)) {
							user.sendMessage(ChatColor.RED + "'" + str + "' is not a guest of lot " + LotID.split(",")[0]);
						} else {
							user.sendMessage(ChatColor.GREEN + "'" + str + "' is no longer a guest of lot " + LotID.split(",")[0]);
							BenCmd.getLots().getLot(LotID).deleteGuest(str);
						}
					}
				}
			} else {
				if (user.isServer()) {
					BenCmd.getLocale().sendMessage(user, "basic.noServerUse");
					return;
				}
				String LotID = "";
				Player player = ((Player) user.getHandle());
				LotID = BenCmd.getLots().ownsHere(player, player.getLocation());
				if (LotID.equalsIgnoreCase("false")) {
					user.sendMessage(ChatColor.RED + "You do not own this lot!");
					return;
				}
				if (LotID.equalsIgnoreCase("noLot")) {
					user.sendMessage(ChatColor.RED + "You are not standing inside a lot!");
					return;
				}
				if (LotID.equalsIgnoreCase("noUser")) {
					user.sendMessage(ChatColor.RED + "Umm... you appear to not exist...");
					return;
				}
				Lot lot = BenCmd.getLots().getLot(LotID);
				boolean commandsReady = false;
				for (String str : args) {
					if (!commandsReady) {
						commandsReady = true;
						continue;
					}
					if (str.startsWith("+")) {
						if (!user.hasPerm("bencmd.lot.guest") && !lot.isOwner(((Player) user.getHandle()))) {
							BenCmd.getLocale().sendMessage(user, "basic.noPermission");
							return;
						}
						str = str.replaceFirst("\\+", "");
						Lot Lot = BenCmd.getLots().getLot(LotID);
						if (Lot.isGuest(str)) {
							user.sendMessage(ChatColor.RED + "'" + str + "' is already a guest of lot " + LotID.split(",")[0]);
						} else {
							BenCmd.getLots().getLot(LotID).addGuest(str);
							user.sendMessage(ChatColor.GREEN + "'" + str + "' is now a guest of lot " + LotID.split(",")[0]);
						}

					} else if (str.startsWith("-")) {
						if (!user.hasPerm("bencmd.lot.guest") && !lot.isOwner(((Player) user.getHandle()))) {
							BenCmd.getLocale().sendMessage(user, "basic.noPermission");
							return;
						}
						str = str.replaceFirst("-", "");
						Lot Lot = BenCmd.getLots().getLot(LotID);
						if (!Lot.isGuest(str)) {
							user.sendMessage(ChatColor.RED + "'" + str + "' is not a guest of lot " + LotID.split(",")[0]);
						} else {
							user.sendMessage(ChatColor.GREEN + "'" + str + "' is no longer a guest of lot " + LotID.split(",")[0]);
							BenCmd.getLots().getLot(LotID).deleteGuest(str);
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
		if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("new")) {
			if (user.isServer()) {
				BenCmd.getLocale().sendMessage(user, "basic.noServerUse");
				return;
			}
			if (!user.hasPerm("bencmd.lot.create")) {
				BenCmd.getPlugin().logPermFail(user, "lot", args, true);
				return;
			}
			Location corner1;
			Location corner2;
			String owner, group;
			BenCmdPlayerListener l;
			try {
				l = BenCmdPlayerListener.getInstance();
			} catch (Exception e) {
				user.sendMessage(ChatColor.RED + "Player listener not loaded!");
				user.sendMessage(ChatColor.RED + "Report this error to a server admin!");
				return;
			}
			if (!l.corner.containsKey(user.getName())) {
				user.sendMessage(ChatColor.RED + "You need to mark off some corners first!");
				user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
				return;
			}
			boolean cor1 = l.corner.get(user.getName()).corner1set;
			boolean cor2 = l.corner.get(user.getName()).corner2set;
			if (cor1 && cor2) {
				String LotID = BenCmd.getLots().getNextID();
				corner1 = l.corner.get(user.getName()).getCorner1();
				corner2 = l.corner.get(user.getName()).getCorner2();

				World c1world = corner1.getWorld();
				World c2world = corner2.getWorld();
				World pworld = ((Player) user.getHandle()).getWorld();
				if (c1world != pworld && c2world != pworld) {
					user.sendMessage(ChatColor.RED + "You need to mark off some corners first!");
					user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
					return;
				}
				if (c1world != pworld && c2world == pworld) {
					user.sendMessage(ChatColor.RED + "You need to mark off CORNER 1! (Left-click)");
					user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
					return;
				}
				if (c1world == pworld && c2world != pworld) {
					user.sendMessage(ChatColor.RED + "You need to mark off CORNER 2! (Right-click)");
					user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
					return;
				}

				if (args.length >= 2) {
					owner = args[1];
				} else {
					owner = user.getName();
				}
				if (!BenCmd.getPermissionManager().getGroupFile().groupExists(BenCmd.getMainProperties().getString("AdminGroup", "admin"))) {
					BenCmd.getPermissionManager().getGroupFile().addGroup(new PermissionGroup(BenCmd.getMainProperties().getString("AdminGroup", "admin"), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), "", ' ', 0));
				}
				group = BenCmd.getMainProperties().getString("AdminGroup", "admin");
				BenCmd.getLots().addLot(LotID, corner1, corner2, owner, group);
				LotID = BenCmd.getLots().getLot(LotID).getLotID();
				user.sendMessage(ChatColor.GREEN + "Lot " + LotID + " was successfully created with owner " + owner);
				return;
			} else {
				if (!cor1 && !cor2)
					user.sendMessage(ChatColor.RED + "You need to mark off some corners first!");
				if (!cor1 && cor2)
					user.sendMessage(ChatColor.RED + "You need to mark off CORNER 1! (Left-click)");
				if (cor1 && !cor2)
					user.sendMessage(ChatColor.RED + "You need to mark off CORNER 2! (Right-click)");
				user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
			}
			return;
		}
		
		/*
		 * GLOBAL LOT SET
		 * 
		 * Allows a user to set a lot over an entire WORLD!
		 * 
		 */
		
		if (args[0].equalsIgnoreCase("setgloballot")) {
			if (user.isServer()) {
				BenCmd.getLocale().sendMessage(user, "basic.noServerUse");
				return;
			}
			if (!user.hasPerm("bencmd.lot.create")) {
				BenCmd.getPlugin().logPermFail(user, "lot", args, true);
				return;
			}
			for (Lot lot : BenCmd.getLots().getLots(false)) {
				if (lot.GLOBALLOT && lot.getWorld() == ((Player) user.getHandle()).getWorld()) {
					user.sendMessage(ChatColor.RED + "A global lot already exists with ID " + lot.getLotID());
					return;
				}
			}
			String LotID = BenCmd.getLots().getNextID();
			Location c1 = ((Player) user.getHandle()).getLocation();
			Location c2 = ((Player) user.getHandle()).getLocation();
			c1.setX(1.1);
			String group = BenCmd.getMainProperties().getString("AdminGroup", "admin");
			BenCmd.getLots().addLot(LotID, c1, c2, user.getName(), group);
			user.sendMessage(ChatColor.GREEN + "A Global lot has been set!");
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
				BenCmd.getLocale().sendMessage(user, "basic.noServerUse");
				return;
			}
			if (!user.hasPerm("bencmd.lot.create")) {
				BenCmd.getPlugin().logPermFail(user, "lot", args, true);
				return;
			}
			Location corner1;
			Location corner2;
			String owner, group;
			BenCmdPlayerListener l;
			try {
				l = BenCmdPlayerListener.getInstance();
			} catch (Exception e) {
				user.sendMessage(ChatColor.RED + "Player listener not loaded!");
				user.sendMessage(ChatColor.RED + "Report this error to a server admin!");
				return;
			}
			if (!l.corner.containsKey(user.getName())) {
				user.sendMessage(ChatColor.RED + "You need to mark off some corners first!");
				user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
				return;
			}
			boolean cor1 = l.corner.get(user.getName()).corner1set;
			boolean cor2 = l.corner.get(user.getName()).corner2set;
			int up, down;
			if (cor1 && cor2) {

				String LotID = BenCmd.getLots().getNextID();
				corner1 = l.corner.get(user.getName()).getCorner1();
				corner2 = l.corner.get(user.getName()).getCorner2();

				World c1world = corner1.getWorld();
				World c2world = corner2.getWorld();
				World pworld = ((Player) user.getHandle()).getWorld();
				if (c1world != pworld && c2world != pworld) {
					user.sendMessage(ChatColor.RED + "You need to mark off some corners first!");
					user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
					return;
				}
				if (c1world != pworld && c2world == pworld) {
					user.sendMessage(ChatColor.RED + "You need to mark off CORNER 1! (Left-click)");
					user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
					return;
				}
				if (c1world == pworld && c2world != pworld) {
					user.sendMessage(ChatColor.RED + "You need to mark off CORNER 2! (Right-click)");
					user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
					return;
				}

				if (args.length >= 4) {
					try {
						up = Integer.parseInt(args[1]);
						down = Integer.parseInt(args[2]);
					} catch (NumberFormatException e) {
						user.sendMessage(ChatColor.RED + "Be sure to use ONLY INTEGERS when specifying up or down!");
						return;
					}
					owner = args[3];
				} else if (args.length >= 3) {
					try {
						up = Integer.parseInt(args[1]);
						down = Integer.parseInt(args[2]);
					} catch (NumberFormatException e) {
						user.sendMessage(ChatColor.RED + "Be sure to use ONLY INTEGERS when specifying up or down!");
						return;
					}
					owner = user.getName();
				} else {
					user.sendMessage(ChatColor.YELLOW + "Proper use is /lot advset <up integer> <down integer> (lot owner)");
					return;
				}
				if (up < -1 || down < -1) {
					user.sendMessage(ChatColor.RED + "Do not use negative integers except for -1!");
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
				if (!BenCmd.getPermissionManager().getGroupFile().groupExists(BenCmd.getMainProperties().getString("AdminGroup", "admin"))) {
					BenCmd.getPermissionManager().getGroupFile().addGroup(new PermissionGroup(BenCmd.getMainProperties().getString("AdminGroup", "admin"), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), "", ' ', 0));
				}
				group = BenCmd.getMainProperties().getString("AdminGroup", "admin");
				BenCmd.getLots().addLot(LotID, corner1, corner2, owner, group);
				LotID = BenCmd.getLots().getLot(LotID).getLotID();
				user.sendMessage(ChatColor.GREEN + "Lot " + LotID + " was successfully created with owner " + owner);
				corner1.setY(oldY1);
				corner2.setY(oldY2);
				return;
			} else {
				if (!cor1 && !cor2)
					user.sendMessage(ChatColor.RED + "You need to mark off some corners first!");
				if (!cor1 && cor2)
					user.sendMessage(ChatColor.RED + "You need to mark off CORNER 1! (Left-click)");
				if (cor1 && !cor2)
					user.sendMessage(ChatColor.RED + "You need to mark off CORNER 2! (Right-click)");
				user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
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
		if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove")) {
			if (!user.hasPerm("bencmd.lot.remove")) {
				BenCmd.getPlugin().logPermFail(user, "lot", args, true);
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

			if (LotID.equalsIgnoreCase("current") || LotID.equalsIgnoreCase("here") || LotID.equalsIgnoreCase("this")) {
				if (user.isServer()) {
					BenCmd.getLocale().sendMessage(user, "basic.noServerUse");
					return;
				}
				Player player = ((Player) user.getHandle());
				LotID = String.valueOf(BenCmd.getLots().isInLot(player.getLocation())).split(",")[0];
				if (LotID == "-1") {
					user.sendMessage(ChatColor.RED + "You're not inside a lot!");
					return;
				}
			}
			if (!SubID.equalsIgnoreCase("-1")) {
				if (BenCmd.getLots().lotExists((LotID + "," + SubID))) {
					if (BenCmd.getLots().deleteLot((LotID + "," + SubID))) {
						user.sendMessage(ChatColor.GREEN + "Lot " + LotID + ", part " + SubID + " was successfully deleted.");
						return;
					} else {
						user.sendMessage(ChatColor.RED + "Something went wrong when deleting the lot.");
						return;
					}
				} else {
					user.sendMessage(ChatColor.RED + "Lot " + LotID + ", part " + SubID + " does not exist!");
				}
				return;
			} else if (BenCmd.getLots().lotExists(LotID)) {
				int size = BenCmd.getLots().getLot(LotID).getSubs().size();
				if (BenCmd.getLots().deleteLot(LotID)) {
					user.sendMessage(ChatColor.GREEN + "Lot " + LotID + " was successfully deleted. (" + size + " parts)");
					return;
				} else {
					user.sendMessage(ChatColor.RED + "Something went wrong when deleting the lot.");
					return;
				}
			} else {
				user.sendMessage(ChatColor.RED + "Lot " + LotID + " does not exist!");
			}
			return;
		}

		/*
		 * LOT SET OWNER
		 * 
		 * Sets the lot owner.
		 */
		if (args[0].equalsIgnoreCase("setowner") || args[0].equalsIgnoreCase("owner") || args[0].equalsIgnoreCase("newowner")) {
			if (!user.hasPerm("bencmd.lot.owner")) {
				BenCmd.getPlugin().logPermFail(user, "lot", args, true);
				return;
			}
			if (args.length == 1) {
				user.sendMessage(ChatColor.YELLOW + "Correct usage is: /lot " + args[0] + " <New Owner> <Lot ID>");
				return;
			}
			if (args.length >= 2) {
				String LotID, Owner;
				Owner = args[1];
				if (args.length >= 3) {
					LotID = args[2];
					if (LotID.equalsIgnoreCase("here") || LotID.equalsIgnoreCase("current") || LotID.equalsIgnoreCase("this")) {
						if (user.isServer()) {
							BenCmd.getLocale().sendMessage(user, "basic.noServerUse");
							return;
						}
						LotID = String.valueOf(BenCmd.getLots().isInLot(((Player) user.getHandle()).getLocation()));
						if (LotID == "-1") {
							user.sendMessage(ChatColor.RED + "You're not inside a lot!");
							return;
						}
					}
				} else {
					if (user.isServer()) {
						BenCmd.getLocale().sendMessage(user, "basic.noServerUse");
						return;
					}
					LotID = String.valueOf(BenCmd.getLots().isInLot(((Player) user.getHandle()).getLocation()));
					if (LotID == "-1") {
						user.sendMessage(ChatColor.RED + "You're not inside a lot!");
						return;
					}
				}
				BenCmd.getLots().getLot(LotID).setOwner(Owner);
				user.sendMessage(ChatColor.GREEN + Owner + " now owns lot " + LotID);
				return;
			}
		}
		/*
		 * LOT LIST
		 * 
		 * Returns all the lots in the lots database
		 */

		if (args[0].equalsIgnoreCase("list")) {
			if (!user.hasPerm("bencmd.lot.info")) {
				BenCmd.getLocale().sendMessage(user, "basic.noPermission");
				return;
			}
			if (args.length == 1) {
				List<Lot> list = BenCmd.getLots().getLots(false);
				user.sendMessage(ChatColor.GRAY + "Registered lots: (" + list.size() + ", " + BenCmd.getLots().getLots(true).size() + " total)");
				int items = 0;
				String message = "";

				for (Lot l : list) {
					if (message != "") {
						message += ", ";
					}
					message += l.getLotID();
					items++;
					if (items == 10) {
						user.sendMessage(ChatColor.GRAY + message);
						items = 0;
						message = "";
					}
				}
				if (message != "") {
					user.sendMessage(ChatColor.GRAY + message);
				}
				return;
			} else if (args.length >= 2) {
				if (args[1].equalsIgnoreCase("here")) {
					List<Lot> list = BenCmd.getLots().getLots(Bukkit.getPlayerExact(user.getName()).getLocation(), false);
					user.sendMessage(ChatColor.GRAY + "Lots at this location: (" + list.size() + ")");
					int items = 0;
					String message = "";

					for (Lot l : list) {
						if (message != "") {
							message += ", ";
						}
						message += l.getLotID();
						items++;
						if (items == 10) {
							user.sendMessage(ChatColor.GRAY + message);
							items = 0;
							message = "";
						}
					}
					if (message != "") {
						user.sendMessage(ChatColor.GRAY + message);
					}
					return;
				} else if (args[1].equalsIgnoreCase("owner")) {
					String owner;
					if (args.length == 2) {
						owner = user.getName();
					} else {
						owner = args[2];
					}
					List<Lot> list = BenCmd.getLots().getLotsByOwner(owner);
					user.sendMessage(ChatColor.GRAY + "Lots owned by " + owner + ": (" + list.size() + ")");
					int items = 0;
					String message = "";

					for (Lot l : list) {
						if (message != "") {
							message += ", ";
						}
						message += l.getLotID();
						items++;
						if (items == 10) {
							user.sendMessage(ChatColor.GRAY + message);
							items = 0;
							message = "";
						}
					}
					if (message != "") {
						user.sendMessage(ChatColor.GRAY + message);
					}
					return;
				} else if (args[1].equalsIgnoreCase("guest")) {
					String guest;
					if (args.length == 2) {
						guest = user.getName();
					} else {
						guest = args[2];
					}
					List<Lot> list = BenCmd.getLots().getLotsByGuest(guest);
					user.sendMessage(ChatColor.GRAY + "Lots with guest " + guest + ": (" + list.size() + ")");
					int items = 0;
					String message = "";

					for (Lot l : list) {
						if (message != "") {
							message += ", ";
						}
						message += l.getLotID();
						items++;
						if (items == 10) {
							user.sendMessage(ChatColor.GRAY + message);
							items = 0;
							message = "";
						}
					}
					if (message != "") {
						user.sendMessage(ChatColor.GRAY + message);
					}
					return;
				} else if (args[1].equalsIgnoreCase("permission") || args[1].equalsIgnoreCase("build") || args[1].equalsIgnoreCase("canbuild")) {
					String player;
					if (args.length == 2) {
						player = user.getName();
					} else {
						player = args[2];
					}
					List<Lot> list = BenCmd.getLots().getLotsByPermission(player);
					user.sendMessage(ChatColor.GRAY + player + " can build in the following lots: (" + list.size() + ")");
					int items = 0;
					String message = "";

					for (Lot l : list) {
						if (message != "") {
							message += ", ";
						}
						message += l.getLotID();
						items++;
						if (items == 10) {
							user.sendMessage(ChatColor.GRAY + message);
							items = 0;
							message = "";
						}
					}
					if (message != "") {
						user.sendMessage(ChatColor.GRAY + message);
					}
					return;
				} else {
					user.sendMessage(ChatColor.RED + "Unknown query. User here, owner, guest, or build.");
				}
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
			Player player = ((Player) user.getHandle());
			if (args.length < 2) {
				user.sendMessage(ChatColor.YELLOW + "Correct usage is: /lot group {group name} [lot id]");
				return;
			}
			if (args.length == 2) {
				group = args[1];
				LotID = BenCmd.getLots().ownsHere(player, player.getLocation());
				if (LotID.equalsIgnoreCase("false") && !user.hasPerm("bencmd.lot.group")) {
					user.sendMessage(ChatColor.RED + "You do not own this lot!");
					return;
				}
				if (LotID.equalsIgnoreCase("noLot")) {
					user.sendMessage(ChatColor.RED + "You are not standing inside a lot!");
					return;
				}
				if (LotID.equalsIgnoreCase("noUser")) {
					user.sendMessage(ChatColor.RED + "Umm... you appear to not exist...");
					return;
				}
				Lot lot = BenCmd.getLots().getLot(LotID);
				lot.setGroup(group);
				user.sendMessage(ChatColor.GREEN + "Lot " + LotID.split(",")[0] + "'s group has been set to '" + group + "'.");
				return;
			} else if (args.length >= 3) {
				group = args[1];
				LotID = args[2];
				if (LotID.equalsIgnoreCase("current") || LotID.equalsIgnoreCase("here") || LotID.equalsIgnoreCase("this")) {
					if (user.isServer()) {
						BenCmd.getLocale().sendMessage(user, "basic.noServerUse");
						return;
					}
					LotID = BenCmd.getLots().ownsHere(player, player.getLocation());
					if (LotID.equalsIgnoreCase("false")) {
						user.sendMessage(ChatColor.RED + "You do not own this lot!");
						return;
					}
					if (LotID.equalsIgnoreCase("noLot")) {
						user.sendMessage(ChatColor.RED + "You are not standing inside a lot!");
						return;
					}
					if (LotID.equalsIgnoreCase("noUser")) {
						user.sendMessage(ChatColor.RED + "Umm... you appear to not exist...");
						return;
					}
				} else {
					if (!BenCmd.getLots().lotExists(LotID)) {
						user.sendMessage(ChatColor.RED + "Lot does not exist!");
						return;
					}
				}
				Lot lot = BenCmd.getLots().getLot(LotID);
				if (!user.hasPerm("bencmd.lot.group") && !lot.isOwner(player)) {
					BenCmd.getLocale().sendMessage(user, "basic.noPermission");
					return;
				}
				lot.setGroup(group);
				LotID = lot.getLotID();
				user.sendMessage(ChatColor.GREEN + "Lot " + LotID + "'s group has been set to '" + group + "'.");
				return;
			} else {
				user.sendMessage(ChatColor.RED + "Some weird error has occured.");
				return;
			}
		}
		/*
		 * EXTEND LOT
		 * 
		 * This command adds a sub-lot to the defined lot.
		 */

		if (args[0].equalsIgnoreCase("extend") || args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("newpart") || args[0].equalsIgnoreCase("ext")) {
			if (user.isServer()) {
				BenCmd.getLocale().sendMessage(user, "basic.noServerUse");
				return;
			}
			if (!user.hasPerm("bencmd.lot.extend")) {
				BenCmd.getPlugin().logPermFail(user, "lot", args, true);
				return;
			}
			Location corner1;
			Location corner2;
			String LotID;
			BenCmdPlayerListener l;
			try {
				l = BenCmdPlayerListener.getInstance();
			} catch (Exception e) {
				user.sendMessage(ChatColor.RED + "Player listener not loaded!");
				user.sendMessage(ChatColor.RED + "Report this error to a server admin!");
				return;
			}
			if (!l.corner.containsKey(user.getName())) {
				user.sendMessage(ChatColor.RED + "You need to mark off some corners first!");
				user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
				return;
			}
			Player player = ((Player) user.getHandle());
			if (args.length >= 2)
				LotID = args[1];
			else
				LotID = "this";
			if (LotID.equalsIgnoreCase("current") || LotID.equalsIgnoreCase("here") || LotID.equalsIgnoreCase("this")) {

				LotID = String.valueOf(BenCmd.getLots().isInLot(player.getLocation()));
				if (LotID == "-1") {
					user.sendMessage(ChatColor.RED + "You need to specify a lot!");
					return;
				}
				if (LotID.split(",").length == 2) {
					LotID = LotID.split(",")[0];
				}
			} else {
				if (!BenCmd.getLots().lotExists(LotID)) {
					user.sendMessage(ChatColor.RED + "Lot does not exist!");
					return;
				}
			}
			boolean cor1 = l.corner.get(user.getName()).corner1set;
			boolean cor2 = l.corner.get(user.getName()).corner2set;
			if (cor1 && cor2) {
				corner1 = l.corner.get(user.getName()).getCorner1();
				corner2 = l.corner.get(user.getName()).getCorner2();

				World c1world = corner1.getWorld();
				World c2world = corner2.getWorld();
				World pworld = ((Player) user.getHandle()).getWorld();
				if (c1world != pworld && c2world != pworld) {
					user.sendMessage(ChatColor.RED + "You need to mark off some corners first!");
					user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
					return;
				}
				if (c1world != pworld && c2world == pworld) {
					user.sendMessage(ChatColor.RED + "You need to mark off CORNER 1! (Left-click)");
					user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
					return;
				}
				if (c1world == pworld && c2world != pworld) {
					user.sendMessage(ChatColor.RED + "You need to mark off CORNER 2! (Right-click)");
					user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
					return;
				}

				String group = BenCmd.getLots().getLot(LotID).getLotGroup();
				String owner = BenCmd.getLots().getLot(LotID).getOwner();
				BenCmd.getLots().sortSubs(LotID);
				String SubID = BenCmd.getLots().getNextSubID(LotID);
				BenCmd.getLots().addLot(LotID + "," + SubID, corner1, corner2, owner, group);
				LotID = BenCmd.getLots().getLot(LotID).getLotID();
				int SubSize = BenCmd.getLots().getLot(LotID).getSubs().size();
				user.sendMessage(ChatColor.GREEN + "Lot " + LotID + " was successfully extended to part " + SubID + ".  (" + SubSize + " total parts)");
				return;
			} else {
				if (!cor1 && !cor2)
					user.sendMessage(ChatColor.RED + "You need to mark off some corners first!");
				if (!cor1 && cor2)
					user.sendMessage(ChatColor.RED + "You need to mark off CORNER 1! (Left-click)");
				if (cor1 && !cor2)
					user.sendMessage(ChatColor.RED + "You need to mark off CORNER 2! (Right-click)");
				user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
			}
			return;
		}
		/*
		 * This allows the user to change the y-coordinates of the extension
		 * being created
		 */
		if (args[0].equalsIgnoreCase("advextend") || args[0].equalsIgnoreCase("advadd") || args[0].equalsIgnoreCase("advnewpart") || args[0].equalsIgnoreCase("advext")) {
			if (user.isServer()) {
				BenCmd.getLocale().sendMessage(user, "basic.noServerUse");
				return;
			}
			if (!user.hasPerm("bencmd.lot.extend")) {
				BenCmd.getPlugin().logPermFail(user, "lot", args, true);
				return;
			}
			Player player = ((Player) user.getHandle());
			Location corner1;
			Location corner2;
			String LotID;
			BenCmdPlayerListener l;
			try {
				l = BenCmdPlayerListener.getInstance();
			} catch (Exception e) {
				user.sendMessage(ChatColor.RED + "Player listener not loaded!");
				user.sendMessage(ChatColor.RED + "Report this error to a server admin!");
				return;
			}
			if (!l.corner.containsKey(user.getName())) {
				user.sendMessage(ChatColor.RED + "You need to mark off some corners first!");
				user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
				return;
			}
			boolean cor1 = l.corner.get(user.getName()).corner1set;
			boolean cor2 = l.corner.get(user.getName()).corner2set;
			int up, down;
			if (cor1 && cor2) {
				corner1 = l.corner.get(user.getName()).getCorner1();
				corner2 = l.corner.get(user.getName()).getCorner2();

				World c1world = corner1.getWorld();
				World c2world = corner2.getWorld();
				World pworld = ((Player) user.getHandle()).getWorld();
				if (c1world != pworld && c2world != pworld) {
					user.sendMessage(ChatColor.RED + "You need to mark off some corners first!");
					user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
					return;
				}
				if (c1world != pworld && c2world == pworld) {
					user.sendMessage(ChatColor.RED + "You need to mark off CORNER 1! (Left-click)");
					user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
					return;
				}
				if (c1world == pworld && c2world != pworld) {
					user.sendMessage(ChatColor.RED + "You need to mark off CORNER 2! (Right-click)");
					user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
					return;
				}

				if (args.length >= 4) {
					try {
						up = Integer.parseInt(args[1]);
						down = Integer.parseInt(args[2]);
					} catch (NumberFormatException e) {
						user.sendMessage(ChatColor.RED + "Be sure to use ONLY INTEGERS when specifying up or down!");
						return;
					}
					LotID = args[3];
				} else if (args.length == 3) {
					try {
						up = Integer.parseInt(args[1]);
						down = Integer.parseInt(args[2]);
					} catch (NumberFormatException e) {
						user.sendMessage(ChatColor.RED + "Be sure to use ONLY INTEGERS when specifying up or down!");
						return;
					}
					LotID = "this";
				} else {
					user.sendMessage(ChatColor.YELLOW + "Proper use is /lot advset <up integer> <down integer> (lot ID)");
					return;
				}
				if (up < -1 || down < -1) {
					user.sendMessage(ChatColor.RED + "Do not use negative integers except for -1!");
				}
				LotID = LotID.split(",")[0];
				if (LotID.equalsIgnoreCase("current") || LotID.equalsIgnoreCase("here") || LotID.equalsIgnoreCase("this")) {

					LotID = String.valueOf(BenCmd.getLots().isInLot(player.getLocation()));
					if (LotID == "-1") {
						user.sendMessage(ChatColor.RED + "You need to specify a lot!");
						return;
					}
					if (LotID.split(",").length == 2) {
						LotID = LotID.split(",")[0];
					}
				} else {
					if (!BenCmd.getLots().lotExists(LotID)) {
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
				String owner = BenCmd.getLots().getLot(LotID).getOwner();
				String group = BenCmd.getLots().getLot(LotID).getLotGroup();
				BenCmd.getLots().sortSubs(LotID);
				String SubID = BenCmd.getLots().getNextSubID(LotID);
				BenCmd.getLots().addLot(LotID + "," + SubID, corner1, corner2, owner, group);
				int SubSize = BenCmd.getLots().getLot(LotID).getSubs().size();
				user.sendMessage(ChatColor.GREEN + "Lot " + LotID + " was successfully extended to part " + SubID + ".  (" + SubSize + " total parts)");
				corner1.setY(oldY1);
				corner2.setY(oldY2);
				return;
			} else {
				if (!cor1 && !cor2)
					user.sendMessage(ChatColor.RED + "You need to mark off some corners first!");
				if (!cor1 && cor2)
					user.sendMessage(ChatColor.RED + "You need to mark off CORNER 1! (Left-click)");
				if (cor1 && !cor2)
					user.sendMessage(ChatColor.RED + "You need to mark off CORNER 2! (Right-click)");
				user.sendMessage(ChatColor.RED + "Be sure to use a wooden shovel!");
			}
			return;
		}

		/*
		 * Alerts the user to an invalid command.
		 */

		user.sendMessage(ChatColor.RED + "Unknown lot command!" + ChatColor.YELLOW + " Available Commands:");
		user.sendMessage(ChatColor.YELLOW + "set, advset, delete, guest, info, setowner, group");
		return;
	}

	public void Area(String[] args, User user) {
		if (args.length == 0) {
			BenCmd.showUse(user, "area");
			return;
		}
		BenCmdPlayerListener l;
		l = BenCmdPlayerListener.getInstance();
		if (args[0].equalsIgnoreCase("info")) {
			if (!user.hasPerm("bencmd.area.info")) {
				BenCmd.getPlugin().logPermFail(user, "area", args, true);
				return;
			}
			SPArea a = null;
			if (args.length == 1) {
				List<SPArea> e = new ArrayList<SPArea>();
				for (SPArea a2 : BenCmd.getAreas().listAreas()) {
					if (a2.insideArea(((Player) user.getHandle()).getLocation())) {
						e.add(a2);
					}
				}
				if (e.isEmpty()) {
					BenCmd.getLocale().sendMessage(user, "command.area.notInArea");
					return;
				}
				if (e.size() > 1) {
					String list = "";
					for (SPArea a2 : e) {
						if (list.isEmpty()) {
							list += a2.getAreaID();
						} else {
							list += ", " + a2.getAreaID();
						}
					}
					user.sendMessage(ChatColor.GRAY + "You are standing in more than one area. (Be more specific)");
					user.sendMessage(ChatColor.GRAY + "Area IDs: " + list);
					return;
				}
				a = e.get(0);
				e = null;

			} else if (args.length == 2) {
				int id;
				try {
					id = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					BenCmd.showUse(user, "area", "info");
					return;
				}
				a = BenCmd.getAreas().byId(id);
				if (a == null) {
					BenCmd.getLocale().sendMessage(user, "command.area.areaNotFound");
					return;
				}
			} else {
				BenCmd.showUse(user, "area", "info");
				return;
			}
			user.sendMessage(ChatColor.GRAY + "Area ID: " + a.getAreaID());
			if (a instanceof PVPArea) {
				user.sendMessage(ChatColor.RED + "Area Type: PvP");
			} else if (a instanceof MsgArea) {
				user.sendMessage(ChatColor.GRAY + "Area Type: Message");
			} else if (a instanceof DamageArea) {
				user.sendMessage(ChatColor.RED + "Area Type: Timed Damage");
			} else if (a instanceof HealArea) {
				user.sendMessage(ChatColor.GREEN + "Area Type: Timed Healing");
			} else if (a instanceof TRArea) {
				user.sendMessage(ChatColor.GRAY + "Area Type: Timed Restrictive");
			}
			Location c = a.getCorner1();
			user.sendMessage(ChatColor.GRAY + "Corner 1: " + c.getBlockX() + ", " + c.getBlockY() + ", " + c.getBlockZ() + ", in world \"" + c.getWorld().getName() + "\"");
			c = a.getCorner2();
			user.sendMessage(ChatColor.GRAY + "Corner 2: " + c.getBlockX() + ", " + c.getBlockY() + ", " + c.getBlockZ() + ", in world \"" + c.getWorld().getName() + "\"");
			c = null;
			if (a instanceof MsgArea) {
				String msg = ((MsgArea) a).getEnterMessage();
				if (!(msg.startsWith("§") && msg.length() == 2)) {
					user.sendMessage(ChatColor.GRAY + "Enter message: " + msg);
				}
				msg = ((MsgArea) a).getLeaveMessage();
				if (!(msg.startsWith("§") && msg.length() == 2)) {
					user.sendMessage(ChatColor.GRAY + "Leave message: " + msg);
				}
			} else if (a instanceof TimedArea) {
				user.sendMessage(ChatColor.GRAY + "Minimum Time: " + ((TimedArea) a).getMinTime() + " seconds");
			}
			if (a instanceof PVPArea) {
				String dm = "";
				switch (((PVPArea) a).getNDrop()) {
					case DROP:
						dm += "d";
						break;
					case LOSE:
						dm += "l";
						break;
					case KEEP:
						dm += "k";
						break;
				}
				switch (((PVPArea) a).getCDrop()) {
					case DROP:
						dm += "d";
						break;
					case LOSE:
						dm += "l";
						break;
					case KEEP:
						dm += "k";
						break;
				}
				user.sendMessage(ChatColor.GRAY + "Drop mode: " + dm);
				user.sendMessage(ChatColor.GRAY + "For info on the drop table, use /area table");
			}
			a = null;
		} else if (args[0].equalsIgnoreCase("table")) {
			if (!user.hasPerm("bencmd.area.info")) {
				BenCmd.getPlugin().logPermFail(user, "area", args, true);
				return;
			}
			PVPArea a = null;
			if (args.length == 1) {
				List<PVPArea> e = new ArrayList<PVPArea>();
				for (SPArea a2 : BenCmd.getAreas().listAreas()) {
					if (a2 instanceof PVPArea && a2.insideArea(((Player) user.getHandle()).getLocation())) {
						e.add((PVPArea) a2);
					}
				}
				if (e.isEmpty()) {
					user.sendMessage(ChatColor.RED + "You aren't standing inside any PvP areas...");
					return;
				}
				if (e.size() > 1) {
					String list = "";
					for (SPArea a2 : e) {
						if (list.isEmpty()) {
							list += a2.getAreaID();
						} else {
							list += ", " + a2.getAreaID();
						}
					}
					user.sendMessage(ChatColor.GRAY + "You are standing in more than one PvP area. (Be more specific)");
					user.sendMessage(ChatColor.GRAY + "Area IDs: " + list);
					return;
				}
				a = e.get(0);
				e = null;
			} else if (args.length == 2) {
				int id;
				try {
					id = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					user.sendMessage(ChatColor.YELLOW + "Proper usage: /area table [id]");
					return;
				}
				SPArea a2 = BenCmd.getAreas().byId(id);
				if (a2 == null) {
					user.sendMessage(ChatColor.RED + "There is no area with that ID!");
					return;
				} else if (!(a2 instanceof PVPArea)) {
					user.sendMessage(ChatColor.RED + "The area with that ID is not a PvP area!");
					return;
				}
				a = (PVPArea) a2;
			} else {
				user.sendMessage(ChatColor.YELLOW + "Proper usage: /area table [id]");
				return;
			}
			DropTable t = a.getDropTable();
			if (t.getAllDrops().isEmpty()) {
				user.sendMessage(ChatColor.RED + "That PvP area's drop table is empty...");
				return;
			}
			user.sendMessage(ChatColor.GRAY + "PvP Drop Table for area " + a.getAreaID() + ":");
			for (int i = 0; i < t.getAllDrops().size(); i++) {
				BCItem item = (BCItem) t.getAllDrops().keySet().toArray()[i];
				DropInfo info = t.getAllDrops().get(item);
				String msg = "Item: " + item.getMaterial().getId();
				if (item.getDamage() != 0) {
					msg += ":" + item.getDamage();
				}
				msg += ", Chance: 1/" + info.getChance() + ", Amount: ";
				if (info.getMin() == info.getMax()) {
					msg += info.getMin();
				} else {
					msg += info.getMin() + "-" + info.getMax();
				}
				user.sendMessage(ChatColor.GRAY + msg);
			}
			a = null;
		} else if (args[0].equalsIgnoreCase("new")) {
			l.checkPlayer(user.getName());
			if (!l.corner.get(user.getName()).corner1set || !l.corner.get(user.getName()).corner2set) {
				user.sendMessage(ChatColor.RED + "Your corners aren't set! Make sure you use a wooden shovel!");
				return;
			}
			if (args.length == 1) {
				user.sendMessage(ChatColor.YELLOW + "Proper usage: /area new {pvp|msg|heal|dmg} [options]");
				return;
			} else if (args[1].equalsIgnoreCase("msg")) {
				if (!user.hasPerm("bencmd.area.create.msg")) {
					BenCmd.getPlugin().logPermFail(user, "area", args, true);
					return;
				}
				int up, down;
				Location c1 = l.corner.get(user.getName()).corner1, c2 = l.corner.get(user.getName()).corner2;
				if (args.length == 2) {
					up = 0;
					down = 0;
				} else if (args.length == 4) {
					try {
						up = Integer.parseInt(args[2]);
						down = Integer.parseInt(args[3]);
					} catch (NumberFormatException e) {
						user.sendMessage(ChatColor.YELLOW + "Proper usage: /area new msg [<ext up> <ext down>]");
						return;
					}
				} else {
					user.sendMessage(ChatColor.YELLOW + "Proper usage: /area new msg [<ext up> <ext down>]");
					return;
				}
				if (up != 0) {
					if (c1.getBlockY() > c2.getBlockY()) {
						if (up == -1) {
							c1.setY(128);
						} else if (c1.getBlockX() + up > 128) {
							c1.setY(128);
						} else {
							c1.setY(c1.getBlockX() + up);
						}
					} else {
						if (up == -1) {
							c2.setY(128);
						} else if (c2.getBlockX() + up > 128) {
							c2.setY(128);
						} else {
							c2.setY(c2.getBlockX() + up);
						}
					}
				}
				if (down != 0) {
					if (c1.getBlockY() > c2.getBlockY()) {
						if (down == -1) {
							c2.setY(0);
						} else if (c2.getBlockX() - down < 0) {
							c2.setY(0);
						} else {
							c2.setY(c1.getBlockX() - down);
						}
					} else {
						if (down == -1) {
							c1.setY(0);
						} else if (c1.getBlockX() - down < 0) {
							c1.setY(0);
						} else {
							c1.setY(c1.getBlockX() - down);
						}
					}
				}
				BenCmd.getAreas().addArea(new MsgArea(BenCmd.getAreas().nextId(), c1, c2, "Use /area emsg to change...", ""));
				user.sendMessage(ChatColor.GREEN + "That area is now dedicated as a Message Area!");
			} else if (args[1].equalsIgnoreCase("heal")) {
				if (!user.hasPerm("bencmd.area.create.heal")) {
					BenCmd.getPlugin().logPermFail(user, "area", args, true);
					return;
				}
				int up, down;
				Location c1 = l.corner.get(user.getName()).corner1, c2 = l.corner.get(user.getName()).corner2;
				if (args.length == 2) {
					up = 0;
					down = 0;
				} else if (args.length == 4) {
					try {
						up = Integer.parseInt(args[2]);
						down = Integer.parseInt(args[3]);
					} catch (NumberFormatException e) {
						user.sendMessage(ChatColor.YELLOW + "Proper usage: /area new heal [<ext up> <ext down>]");
						return;
					}
				} else {
					user.sendMessage(ChatColor.YELLOW + "Proper usage: /area new heal [<ext up> <ext down>]");
					return;
				}
				if (up != 0) {
					if (c1.getBlockY() > c2.getBlockY()) {
						if (up == -1) {
							c1.setY(128);
						} else if (c1.getBlockX() + up > 128) {
							c1.setY(128);
						} else {
							c1.setY(c1.getBlockX() + up);
						}
					} else {
						if (up == -1) {
							c2.setY(128);
						} else if (c2.getBlockX() + up > 128) {
							c2.setY(128);
						} else {
							c2.setY(c2.getBlockX() + up);
						}
					}
				}
				if (down != 0) {
					if (c1.getBlockY() > c2.getBlockY()) {
						if (down == -1) {
							c2.setY(0);
						} else if (c2.getBlockX() - down < 0) {
							c2.setY(0);
						} else {
							c2.setY(c1.getBlockX() - down);
						}
					} else {
						if (down == -1) {
							c1.setY(0);
						} else if (c1.getBlockX() - down < 0) {
							c1.setY(0);
						} else {
							c1.setY(c1.getBlockX() - down);
						}
					}
				}
				BenCmd.getAreas().addArea(new HealArea(BenCmd.getAreas().nextId(), c1, c2, 0));
				user.sendMessage(ChatColor.GREEN + "That area is now dedicated as a Healing Area!");
			} else if (args[1].equalsIgnoreCase("dmg")) {
				if (!user.hasPerm("bencmd.area.create.dmg")) {
					BenCmd.getPlugin().logPermFail(user, "area", args, true);
					return;
				}
				int up, down;
				Location c1 = l.corner.get(user.getName()).corner1, c2 = l.corner.get(user.getName()).corner2;
				if (args.length == 2) {
					up = 0;
					down = 0;
				} else if (args.length == 4) {
					try {
						up = Integer.parseInt(args[2]);
						down = Integer.parseInt(args[3]);
					} catch (NumberFormatException e) {
						user.sendMessage(ChatColor.YELLOW + "Proper usage: /area new dmg [<ext up> <ext down>]");
						return;
					}
				} else {
					user.sendMessage(ChatColor.YELLOW + "Proper usage: /area new dmg [<ext up> <ext down>]");
					return;
				}
				if (up != 0) {
					if (c1.getBlockY() > c2.getBlockY()) {
						if (up == -1) {
							c1.setY(128);
						} else if (c1.getBlockX() + up > 128) {
							c1.setY(128);
						} else {
							c1.setY(c1.getBlockX() + up);
						}
					} else {
						if (up == -1) {
							c2.setY(128);
						} else if (c2.getBlockX() + up > 128) {
							c2.setY(128);
						} else {
							c2.setY(c2.getBlockX() + up);
						}
					}
				}
				if (down != 0) {
					if (c1.getBlockY() > c2.getBlockY()) {
						if (down == -1) {
							c2.setY(0);
						} else if (c2.getBlockX() - down < 0) {
							c2.setY(0);
						} else {
							c2.setY(c1.getBlockX() - down);
						}
					} else {
						if (down == -1) {
							c1.setY(0);
						} else if (c1.getBlockX() - down < 0) {
							c1.setY(0);
						} else {
							c1.setY(c1.getBlockX() - down);
						}
					}
				}
				BenCmd.getAreas().addArea(new DamageArea(BenCmd.getAreas().nextId(), c1, c2, 0));
				user.sendMessage(ChatColor.GREEN + "That area is now dedicated as a Damage Area!");
			} else if (args[1].equalsIgnoreCase("pvp")) {
				if (!user.hasPerm("bencmd.area.create.pvp")) {
					BenCmd.getPlugin().logPermFail(user, "area", args, true);
					return;
				}
				int reqval, up, down;
				Location c1 = l.corner.get(user.getName()).corner1, c2 = l.corner.get(user.getName()).corner2;
				if (args.length == 3) {
					try {
						reqval = Integer.parseInt(args[2]);
					} catch (NumberFormatException e) {
						user.sendMessage(ChatColor.YELLOW + "Proper usage: /area new pvp <value needed> [<ext up> <ext down>] ");
						return;
					}
					up = 0;
					down = 0;
				} else if (args.length == 5) {
					try {
						reqval = Integer.parseInt(args[2]);
						up = Integer.parseInt(args[3]);
						down = Integer.parseInt(args[4]);
					} catch (NumberFormatException e) {
						user.sendMessage(ChatColor.YELLOW + "Proper usage: /area new pvp <value needed> [<ext up> <ext down>] ");
						return;
					}
				} else {
					user.sendMessage(ChatColor.YELLOW + "Proper usage: /area new pvp <value needed> [<ext up> <ext down>] ");
					return;
				}
				if (up != 0) {
					if (c1.getBlockY() > c2.getBlockY()) {
						if (up == -1) {
							c1.setY(128);
						} else if (c1.getBlockX() + up > 128) {
							c1.setY(128);
						} else {
							c1.setY(c1.getBlockX() + up);
						}
					} else {
						if (up == -1) {
							c2.setY(128);
						} else if (c2.getBlockX() + up > 128) {
							c2.setY(128);
						} else {
							c2.setY(c2.getBlockX() + up);
						}
					}
				}
				if (down != 0) {
					if (c1.getBlockY() > c2.getBlockY()) {
						if (down == -1) {
							c2.setY(0);
						} else if (c2.getBlockX() - down < 0) {
							c2.setY(0);
						} else {
							c2.setY(c1.getBlockX() - down);
						}
					} else {
						if (down == -1) {
							c1.setY(0);
						} else if (c1.getBlockX() - down < 0) {
							c1.setY(0);
						} else {
							c1.setY(c1.getBlockX() - down);
						}
					}
				}
				BenCmd.getAreas().addArea(new PVPArea(BenCmd.getAreas().nextId(), c1, c2, reqval));
				user.sendMessage(ChatColor.GREEN + "That area is now dedicated as a PVP Area!");
			} else if (args[1].equalsIgnoreCase("time")) {
				if (!user.hasPerm("bencmd.area.create.time")) {
					BenCmd.getPlugin().logPermFail(user, "area", args, true);
					return;
				}
				int time, up, down;
				Location c1 = l.corner.get(user.getName()).corner1, c2 = l.corner.get(user.getName()).corner2;
				if (args.length == 3) {
					try {
						time = Integer.parseInt(args[2]);
					} catch (NumberFormatException e) {
						user.sendMessage(ChatColor.YELLOW + "Proper usage: /area new time <seconds> [<ext up> <ext down>] ");
						return;
					}
					up = 0;
					down = 0;
				} else if (args.length == 5) {
					try {
						time = Integer.parseInt(args[2]);
						up = Integer.parseInt(args[3]);
						down = Integer.parseInt(args[4]);
					} catch (NumberFormatException e) {
						user.sendMessage(ChatColor.YELLOW + "Proper usage: /area new time <seconds> [<ext up> <ext down>] ");
						return;
					}
				} else {
					user.sendMessage(ChatColor.YELLOW + "Proper usage: /area new time <seconds> [<ext up> <ext down>] ");
					return;
				}
				if (up != 0) {
					if (c1.getBlockY() > c2.getBlockY()) {
						if (up == -1) {
							c1.setY(128);
						} else if (c1.getBlockX() + up > 128) {
							c1.setY(128);
						} else {
							c1.setY(c1.getBlockX() + up);
						}
					} else {
						if (up == -1) {
							c2.setY(128);
						} else if (c2.getBlockX() + up > 128) {
							c2.setY(128);
						} else {
							c2.setY(c2.getBlockX() + up);
						}
					}
				}
				if (down != 0) {
					if (c1.getBlockY() > c2.getBlockY()) {
						if (down == -1) {
							c2.setY(0);
						} else if (c2.getBlockX() - down < 0) {
							c2.setY(0);
						} else {
							c2.setY(c1.getBlockX() - down);
						}
					} else {
						if (down == -1) {
							c1.setY(0);
						} else if (c1.getBlockX() - down < 0) {
							c1.setY(0);
						} else {
							c1.setY(c1.getBlockX() - down);
						}
					}
				}
				BenCmd.getAreas().addArea(new TRArea(BenCmd.getAreas().nextId(), c1, c2, time));
				user.sendMessage(ChatColor.GREEN + "That area is now dedicated as a time-lock Area!");
			} else if (args[1].equalsIgnoreCase("group")) {
				if (!user.hasPerm("bencmd.area.create.group")) {
					BenCmd.getPlugin().logPermFail(user, "area", args, true);
					return;
				}
				int up, down;
				String group;
				Location c1 = l.corner.get(user.getName()).corner1, c2 = l.corner.get(user.getName()).corner2;
				if (args.length == 3) {
					group = args[2];
					up = 0;
					down = 0;
				} else if (args.length == 5) {
					try {
						group = args[2];
						up = Integer.parseInt(args[3]);
						down = Integer.parseInt(args[4]);
					} catch (NumberFormatException e) {
						user.sendMessage(ChatColor.YELLOW + "Proper usage: /area new group <group> [<ext up> <ext down>] ");
						return;
					}
				} else {
					user.sendMessage(ChatColor.YELLOW + "Proper usage: /area new group <group> [<ext up> <ext down>] ");
					return;
				}
				if (up != 0) {
					if (c1.getBlockY() > c2.getBlockY()) {
						if (up == -1) {
							c1.setY(128);
						} else if (c1.getBlockX() + up > 128) {
							c1.setY(128);
						} else {
							c1.setY(c1.getBlockX() + up);
						}
					} else {
						if (up == -1) {
							c2.setY(128);
						} else if (c2.getBlockX() + up > 128) {
							c2.setY(128);
						} else {
							c2.setY(c2.getBlockX() + up);
						}
					}
				}
				if (down != 0) {
					if (c1.getBlockY() > c2.getBlockY()) {
						if (down == -1) {
							c2.setY(0);
						} else if (c2.getBlockX() - down < 0) {
							c2.setY(0);
						} else {
							c2.setY(c1.getBlockX() - down);
						}
					} else {
						if (down == -1) {
							c1.setY(0);
						} else if (c1.getBlockX() - down < 0) {
							c1.setY(0);
						} else {
							c1.setY(c1.getBlockX() - down);
						}
					}
				}
				BenCmd.getAreas().addArea(new GroupArea(BenCmd.getAreas().nextId(), c1, c2, group));
				user.sendMessage(ChatColor.GREEN + "That area is now dedicated as a group-locked area!");
			} else {
				user.sendMessage(ChatColor.YELLOW + "Proper usage: /area new {pvp|msg|heal|dmg|time|group} [options]");
				return;
			}
		} else if (args[0].equalsIgnoreCase("delete")) {
			if (!user.hasPerm("bencmd.area.remove")) {
				BenCmd.getPlugin().logPermFail(user, "area", args, true);
				return;
			}
			if (args.length != 1 && args.length != 2) {
				user.sendMessage(ChatColor.YELLOW + "Proper usage: /area delete [id]");
				return;
			}
			SPArea d = null;
			if (args.length == 1) {
				for (SPArea a : BenCmd.getAreas().listAreas()) {
					if (a.insideArea(((Player) user.getHandle()).getLocation())) {
						d = a;
						break;
					}
				}
				if (d == null) {
					user.sendMessage(ChatColor.RED + "You aren't standing inside an area...");
					return;
				}
			} else {
				int id;
				try {
					id = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					user.sendMessage(ChatColor.YELLOW + "Proper usage: /area delete [id]");
					return;
				}
				d = BenCmd.getAreas().byId(id);
				if (d == null) {
					user.sendMessage(ChatColor.RED + "No area with that ID exists!");
					return;
				}
			}
			BenCmd.getAreas().removeArea(d);
			user.sendMessage(ChatColor.GREEN + "That area has been successfully deleted...");
		} else if (args[0].equalsIgnoreCase("emsg")) {
			if (!user.hasPerm("bencmd.area.create.msg")) {
				BenCmd.getPlugin().logPermFail(user, "area", args, true);
				return;
			}
			String msg = "";
			for (int i = 1; i < args.length; i++) {
				if (msg.isEmpty()) {
					msg += args[i];
				} else {
					msg += " " + args[i];
				}
			}
			MsgArea e = null;
			for (SPArea a : BenCmd.getAreas().listAreas()) {
				if (a instanceof MsgArea && a.insideArea(((Player) user.getHandle()).getLocation())) {
					e = (MsgArea) a;
					break;
				}
			}
			if (e == null) {
				user.sendMessage(ChatColor.RED + "You aren't standing inside an area that can have enter/exit messages...");
				return;
			}
			e.setEnterMessage(args.length > 1 ? msg : "");
		} else if (args[0].equalsIgnoreCase("lmsg")) {
			if (!user.hasPerm("bencmd.area.create.msg")) {
				BenCmd.getPlugin().logPermFail(user, "area", args, true);
				return;
			}
			String msg = "";
			for (int i = 1; i < args.length; i++) {
				if (msg.isEmpty()) {
					msg += args[i];
				} else {
					msg += " " + args[i];
				}
			}
			MsgArea e = null;
			for (SPArea a : BenCmd.getAreas().listAreas()) {
				if (a instanceof MsgArea && a.insideArea(((Player) user.getHandle()).getLocation())) {
					e = (MsgArea) a;
					break;
				}
			}
			if (e == null) {
				user.sendMessage(ChatColor.RED + "You aren't standing inside an area that can have enter/exit messages...");
				return;
			}
			e.setLeaveMessage(args.length > 1 ? msg : "");
		} else if (args[0].equalsIgnoreCase("addi")) {
			if (!user.hasPerm("bencmd.area.table")) {
				BenCmd.getPlugin().logPermFail(user, "area", args, true);
				return;
			}
			BCItem item;
			int chance, max, min;
			if (args.length == 3) {
				item = InventoryBackend.getInstance().checkAlias(args[1]);
				if (item == null) {
					user.sendMessage(ChatColor.YELLOW + "Proper usage: /area addi <item> <chance> [<min> <max>]");
					return;
				}
				try {
					chance = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					user.sendMessage(ChatColor.YELLOW + "Proper usage: /area addi <item> <chance> [<min> <max>]");
					return;
				}
				max = 1;
				min = 1;
			} else if (args.length == 5) {
				item = InventoryBackend.getInstance().checkAlias(args[1]);
				if (item == null) {
					user.sendMessage(ChatColor.YELLOW + "Proper usage: /area addi <item> <chance> [<min> <max>]");
					return;
				}
				try {
					chance = Integer.parseInt(args[2]);
					min = Integer.parseInt(args[3]);
					max = Integer.parseInt(args[4]);
				} catch (NumberFormatException e) {
					user.sendMessage(ChatColor.YELLOW + "Proper usage: /area addi <item> <chance> [<min> <max>]");
					return;
				}
			} else {
				user.sendMessage(ChatColor.YELLOW + "Proper usage: /area addi <item> <chance> [<min> <max>]");
				return;
			}
			PVPArea e = null;
			for (SPArea a : BenCmd.getAreas().listAreas()) {
				if (a instanceof PVPArea && a.insideArea(((Player) user.getHandle()).getLocation())) {
					e = (PVPArea) a;
					break;
				}
			}
			if (e == null) {
				user.sendMessage(ChatColor.RED + "You aren't standing inside a pvp area...");
				return;
			}
			e.addDrop(item, new DropInfo(chance, min, max));
		} else if (args[0].equalsIgnoreCase("remi")) {
			if (!user.hasPerm("bencmd.area.table")) {
				BenCmd.getPlugin().logPermFail(user, "area", args, true);
				return;
			}
			if (args.length == 2) {
				BCItem item = null;
				item = InventoryBackend.getInstance().checkAlias(args[1]);
				if (item == null) {
					user.sendMessage(ChatColor.YELLOW + "Proper usage: /area remi <item>");
					return;
				}
				PVPArea e = null;
				for (SPArea a : BenCmd.getAreas().listAreas()) {
					if (a instanceof PVPArea && a.insideArea(((Player) user.getHandle()).getLocation())) {
						e = (PVPArea) a;
						break;
					}
				}
				if (e == null) {
					user.sendMessage(ChatColor.RED + "You aren't standing inside a pvp area...");
					return;
				}
				e.remDrop(item);
			} else {
				user.sendMessage(ChatColor.YELLOW + "Proper usage: /area remi <item>");
				return;
			}
		} else if (args[0].equalsIgnoreCase("die")) {
			if (!user.hasPerm("bencmd.area.table")) {
				BenCmd.getPlugin().logPermFail(user, "area", args, true);
				return;
			}
			if (args.length == 2) {
				if (args[1].length() == 2) {
					char nc = args[1].charAt(0);
					char cc = args[1].charAt(1);
					PVPArea.DropMode n;
					PVPArea.DropMode c;
					switch (nc) {
						case 'd':
							n = PVPArea.DropMode.DROP;
							break;
						case 'l':
							n = PVPArea.DropMode.LOSE;
							break;
						case 'k':
							n = PVPArea.DropMode.KEEP;
							break;
						default:
							n = null;
							break;
					}
					switch (cc) {
						case 'd':
							c = PVPArea.DropMode.DROP;
							break;
						case 'l':
							c = PVPArea.DropMode.LOSE;
							break;
						case 'k':
							c = PVPArea.DropMode.KEEP;
							break;
						default:
							c = null;
							break;
					}
					PVPArea e = null;
					for (SPArea a : BenCmd.getAreas().listAreas()) {
						if (a instanceof PVPArea && a.insideArea(((Player) user.getHandle()).getLocation())) {
							e = (PVPArea) a;
							break;
						}
					}
					if (e == null) {
						user.sendMessage(ChatColor.RED + "You aren't standing inside a pvp area...");
						return;
					}
					if (n != null) {
						e.setNDrop(n);
					}
					if (e != null) {
						e.setCDrop(c);
					}
				} else {
					user.sendMessage(ChatColor.YELLOW + "Proper usage: /area die {d|l|k|-}{d|l|k|-}");
					return;
				}
			} else {
				user.sendMessage(ChatColor.YELLOW + "Proper usage: /area die {d|l|k|-}{d|l|k|-}");
				return;
			}
		} else if (args[0].equalsIgnoreCase("mtime")) {
			if (!user.hasPerm("bencmd.area.create.time")) {
				BenCmd.getPlugin().logPermFail(user, "area", args, true);
				return;
			}
			if (args.length == 2) {
				int time;
				try {
					time = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					user.sendMessage(ChatColor.YELLOW + "Proper usage: /area mtime <seconds>");
					return;
				}
				TimedArea e = null;
				for (SPArea a : BenCmd.getAreas().listAreas()) {
					if (a instanceof TimedArea && a.insideArea(((Player) user.getHandle()).getLocation())) {
						e = (TimedArea) a;
						break;
					}
				}
				if (e == null) {
					user.sendMessage(ChatColor.RED + "You aren't standing inside a timeable area...");
					return;
				}
				e.setMinTime(time);
			} else {
				user.sendMessage(ChatColor.YELLOW + "Proper usage: /area mtime <seconds>");
				return;
			}
		} else if (args[0].equalsIgnoreCase("group")) {
			if (!user.hasPerm("bencmd.area.create.group")) {
				BenCmd.getPlugin().logPermFail(user, "area", args, true);
				return;
			}
			if (args.length == 2) {
				GroupArea e = null;
				for (SPArea a : BenCmd.getAreas().listAreas()) {
					if (a instanceof GroupArea && a.insideArea(((Player) user.getHandle()).getLocation())) {
						e = (GroupArea) a;
						break;
					}
				}
				if (e == null) {
					user.sendMessage(ChatColor.RED + "You aren't standing inside a group-locked area...");
					return;
				}
				e.setGroup(args[1]);
			} else {
				user.sendMessage(ChatColor.YELLOW + "Proper usage: /area group <group>");
				return;
			}
		} else {
			user.sendMessage(ChatColor.YELLOW + "Proper usage: /area {info [id]|table [id]|new {pvp|msg|heal|dmg|time} [options]|delete|emsg <message>|lmsg <message>|addi <item> <chance> [<min> <max>]|remi <item>|die {d|l|k|-}{d|l|k|-}|mtime <seconds>}");
			return;
		}
	}
}
