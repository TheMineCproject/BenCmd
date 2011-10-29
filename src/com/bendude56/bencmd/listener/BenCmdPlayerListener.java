package com.bendude56.bencmd.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftChest;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import com.bendude56.bencmd.*;
import com.bendude56.bencmd.advanced.Grave;
import com.bendude56.bencmd.advanced.Shelf;
import com.bendude56.bencmd.advanced.ViewableInventory;
import com.bendude56.bencmd.advanced.npc.BankManagerNPC;
import com.bendude56.bencmd.advanced.npc.BankerNPC;
import com.bendude56.bencmd.advanced.npc.BlacksmithNPC;
import com.bendude56.bencmd.advanced.npc.Clickable;
import com.bendude56.bencmd.advanced.npc.EntityNPC;
import com.bendude56.bencmd.advanced.npc.NPC;
import com.bendude56.bencmd.advanced.npc.StaticNPC;
import com.bendude56.bencmd.chat.ChatChecker;
import com.bendude56.bencmd.chat.SlowMode;
import com.bendude56.bencmd.invtools.InventoryBackend;
import com.bendude56.bencmd.lots.Corner;
import com.bendude56.bencmd.lots.sparea.MsgArea;
import com.bendude56.bencmd.lots.sparea.PVPArea;
import com.bendude56.bencmd.lots.sparea.SPArea;
import com.bendude56.bencmd.lots.sparea.TRArea;
import com.bendude56.bencmd.money.Currency;
import com.bendude56.bencmd.multiworld.HomePortal;
import com.bendude56.bencmd.multiworld.Portal;
import com.bendude56.bencmd.permissions.PermissionUser;
import com.bendude56.bencmd.protect.ProtectedBlock;
import com.bendude56.bencmd.warps.Warp;

public class BenCmdPlayerListener extends PlayerListener {

	// Singleton instancing

	private static BenCmdPlayerListener	instance	= null;

	public static BenCmdPlayerListener getInstance() {
		if (instance == null) {
			return instance = new BenCmdPlayerListener();
		} else {
			return instance;
		}
	}

	public static void destroyInstance() {
		instance = null;
	}

	public HashMap<String, Corner>			corner	= new HashMap<String, Corner>();
	private HashMap<Player, List<SPArea>>	areas	= new HashMap<Player, List<SPArea>>();
	private List<Player>					ignore	= new ArrayList<Player>();
	private HashMap<Player, List<NPC>>		sent	= new HashMap<Player, List<NPC>>();

	public void checkPlayer(String player) {
		if (!corner.containsKey(player)) {
			corner.put(player, new Corner());
		}
	}

	private void sendSkins(Player p, Location l) {
		if (BenCmd.isSpoutConnected()) {
			if (!sent.containsKey(p)) {
				sent.put(p, new ArrayList<NPC>());
			}
			for (NPC n : BenCmd.getNPCFile().allNPCs()) {
				if (n.isSpawned() && l.getWorld().equals(n.getCurrentLocation().getWorld()) && l.distance(n.getCurrentLocation()) < 50) {
					if (!sent.get(p).contains(n)) {
						sent.get(p).add(n);
						BenCmd.getSpoutConnector().sendSkin(p, n.getEntityId(), n.getSkinURL());
					}
				} else {
					if (sent.get(p).contains(n)) {
						sent.get(p).remove(n);
						BenCmd.getSpoutConnector().sendSkin(p, n.getEntityId(), "http://s3.amazonaws.com/MinecraftSkins/" + n.getName() + ".png");
					}
				}
			}
		}
	}

	private BenCmdPlayerListener() {
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_CHAT, this, Event.Priority.Lowest, BenCmd.getPlugin());
		pm.registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Monitor, BenCmd.getPlugin());
		pm.registerEvent(Event.Type.PLAYER_QUIT, this, Event.Priority.Monitor, BenCmd.getPlugin());
		pm.registerEvent(Event.Type.PLAYER_KICK, this, Event.Priority.Monitor, BenCmd.getPlugin());
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this, Event.Priority.Lowest, BenCmd.getPlugin());
		pm.registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, this, Event.Priority.Lowest, BenCmd.getPlugin());
		pm.registerEvent(Event.Type.PLAYER_LOGIN, this, Event.Priority.Normal, BenCmd.getPlugin());
		pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, this, Event.Priority.Highest, BenCmd.getPlugin());
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, this, Event.Priority.Highest, BenCmd.getPlugin());
		pm.registerEvent(Event.Type.PLAYER_BUCKET_EMPTY, this, Event.Priority.Highest, BenCmd.getPlugin());
		pm.registerEvent(Event.Type.PLAYER_BUCKET_FILL, this, Event.Priority.Highest, BenCmd.getPlugin());
		pm.registerEvent(Event.Type.PLAYER_RESPAWN, this, Event.Priority.Monitor, BenCmd.getPlugin());
		pm.registerEvent(Event.Type.PLAYER_TELEPORT, this, Event.Priority.Monitor, BenCmd.getPlugin());
		pm.registerEvent(Event.Type.PLAYER_PORTAL, this, Event.Priority.Highest, BenCmd.getPlugin());
		pm.registerEvent(Event.Type.PLAYER_MOVE, this, Event.Priority.Highest, BenCmd.getPlugin());
	}

	public void ToggleSlow(User user) {
		SlowMode slow = SlowMode.getInstance();
		if (slow.isEnabled()) {
			slow.DisableSlow();
			BenCmd.log(user.getDisplayName() + " has disabled slow mode.");
			Bukkit.broadcastMessage(ChatColor.GRAY + "Slow mode has been disabled.");
		} else {
			slow.EnableSlow();
			BenCmd.log(user.getDisplayName() + " has enabled slow mode.");
			Bukkit.broadcastMessage(ChatColor.GRAY + "Slow mode has been enabled. You must wait " + (slow.getDefTime() / 1000) + " seconds between each chat message.");
		}
	}

	private void chat(PlayerChatEvent event) {
		// If external chat is enabled, skip this event
		if (BenCmd.getMainProperties().getBoolean("externalChat", false)) {
			return;
		}
		SlowMode slow = SlowMode.getInstance();
		String message = event.getMessage();
		User user = User.getUser(event.getPlayer());

		// Check if player is muted
		if (user.isMuted() != null) {
			event.setCancelled(true);
			user.sendMessage(ChatColor.GRAY + BenCmd.getMainProperties().getString("muteMessage", "You are muted..."));
			return;
		}

		// Check for channels and use them if applicable
		if (BenCmd.getMainProperties().getBoolean("channelsEnabled", false)) {
			if (user.inChannel()) {
				user.getActiveChannel().sendChat(user, message);
			} else {
				user.sendMessage(ChatColor.RED + "You must be in a chat channel to talk!");
			}
			event.setCancelled(true);
			return;
		}

		// Check for blocked words
		boolean blocked = ChatChecker.checkBlocked(message);
		if (blocked) {
			event.setCancelled(true);
			user.sendMessage(ChatColor.GRAY + BenCmd.getMainProperties().getString("blockMessage", "You used a blocked word..."));
			return;
		}

		// Check for slow mode
		long slowTimeLeft = slow.playerBlocked(user.getName());
		if ((!user.hasPerm("bencmd.chat.noslow")) && slow.isEnabled()) {
			if (slowTimeLeft > 0) {
				user.sendMessage(ChatColor.GRAY + "Slow mode is enabled! You must wait " + (int) Math.ceil(slowTimeLeft / 1000) + " more second(s) before you can talk again.");
				event.setCancelled(true);
				return;
			} else {
				slow.playerAdd(user.getName());
			}
		}

		// Format + display message
		String prefix;
		BenCmd.log(user.getDisplayName() + ": " + message);
		if (!(prefix = user.getPrefix()).isEmpty()) {
			message = user.getColor() + "[" + prefix + "] " + user.getDisplayName() + ": " + ChatColor.WHITE + message;
			Bukkit.broadcastMessage(message);
			event.setCancelled(true);
		} else {
			message = user.getColor() + user.getDisplayName() + ": " + ChatColor.WHITE + message;
			Bukkit.broadcastMessage(message);
			event.setCancelled(true);
		}
	}

	private void userInit(PlayerJoinEvent event) {
		SlowMode slow = SlowMode.getInstance();

		// Prepare the user instance
		ViewableInventory.replInv((CraftPlayer) event.getPlayer());
		User user = User.getUser(event.getPlayer());

		// Disable cheats + flymod if applicable
		String cheatstring = "";
		if (!user.hasPerm("bencmd.allowfly")) {
			// No Zombe-mod flying
			cheatstring += "&f &f &1 &0 &2 &4 ";

			// No CJB flying
			cheatstring += "&3 &9 &2 &0 &0 &1 ";
		}
		if (!user.hasPerm("bencmd.allowcheat")) {
			// No Zombe-mod cheats
			cheatstring += "&f &f &2 &0 &4 &8 ";

			// No CJB cheats
			cheatstring += "&3 &9 &2 &0 &0 &2 ";

			// No entities on minimap
			cheatstring += "&3 &9 &2 &0 &0 &3 ";
		}
		if (!cheatstring.isEmpty()) {
			cheatstring = cheatstring.replaceAll("(&([a-f0-9]))", "\u00A7$2");
			user.sendMessage(cheatstring);
		}

		// Announce updates to admins
		if (BenCmd.updateAvailable && user.hasPerm("bencmd.update")) {
			user.sendMessage(ChatColor.RED + "A new BenCmd update was detected! Use \"/bencmd update\" to update your server...");
		}

		// List players
		Player[] playerList = Bukkit.getOnlinePlayers();
		if (user.hasPerm("bencmd.chat.list")) {
			if (playerList.length == 1) {
				user.sendMessage(ChatColor.GREEN + "You are the only one online. :(");
			} else {
				String playerString = "";
				for (Player player2 : playerList) {
					if (User.getUser(player2).isOffline()) {
						continue;
					}
					playerString += User.getUser(player2).getColor() + player2.getDisplayName() + ChatColor.WHITE + ", ";
				}
				user.sendMessage("The following players are online: " + playerString);
			}
		}

		// Send chat status notifications
		if (user.isMuted() != null) {
			user.sendMessage(ChatColor.RED + "Please note that you are currently muted and cannot speak.");
		} else if (slow.isEnabled()) {
			user.sendMessage(ChatColor.RED + "Please note that slow mode is currently enabled. You must wait " + (slow.getDefTime() / 1000) + " seconds between each chat message.");
		}
		if (user.hasPerm("bencmd.ticket.readall") && BenCmd.getReports().unreadTickets()) {
			user.sendMessage(ChatColor.RED + "There are unread reports! Use /ticket list to see them!");
		}

		// Join general channel
		Bukkit.dispatchCommand(user.getHandle(), "channel join general");
		event.setJoinMessage(user.getColor() + user.getDisplayName() + ChatColor.WHITE + " has joined the game...");

		// Check for jailing/unjailing
		if (BenCmd.getPermissionManager().getActionFile().isUnjailed(user) != null) {
			user.spawn();
			BenCmd.getPermissionManager().getActionFile().removeAction(BenCmd.getPermissionManager().getActionFile().isUnjailed(user));
		}
		if (user.isJailed() != null) {
			user.warpTo(BenCmd.getPermissionManager().getJailWarp());
		}

		// Special effects for devs
		if (user.isDev()) {
			((Player) user.getHandle()).getWorld().strikeLightningEffect(((Player) user.getHandle()).getLocation());
			Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "A BenCmd developer has joined the game!");
		}
	}

	private void quitFinalize(PlayerQuitEvent event) {

		// Remove any of their graves, if applicable
		for (int i = 0; i < BenCmd.getPlugin().graves.size(); i++) {
			Grave g = BenCmd.getPlugin().graves.get(i);
			if (g.getPlayer().equals(event.getPlayer())) {
				g.delete();
				BenCmd.getPlugin().graves.remove(i);
			}
		}
		if (BenCmd.getPlugin().returns.containsKey(event.getPlayer())) {
			BenCmd.getPlugin().returns.remove(event.getPlayer());
		}
		User user = User.getUser(event.getPlayer());

		// Format quit message
		if (user.isOffline()) {
			user.goOnlineNoMsg();
			event.setQuitMessage(null);
		} else {
			event.setQuitMessage(user.getColor() + user.getDisplayName() + ChatColor.WHITE + " has left the game...");
		}

		// Remove them from the maximum players lists
		BenCmd.getPermissionManager().getMaxPlayerHandler().leave(user);

		// Remove all special stauses from them, if applicable
		if (user.isPoofed()) {
			user.unPoof();
		}
		if (user.isNoPoofed()) {
			user.unNoPoof();
		}
		if (user.isAllPoofed()) {
			user.unAllPoof();
		}
		if (user.inChannel()) {
			user.leaveChannel();
		}
		if (user.isGod()) {
			user.makeNonGod();
		}
		user.unspyAll();

		// Destroy their user instance
		User.finalizeUser(user);
	}

	private void kickFinalize(PlayerKickEvent event) {

		// Remove any of their graves, if applicable
		for (int i = 0; i < BenCmd.getPlugin().graves.size(); i++) {
			Grave g = BenCmd.getPlugin().graves.get(i);
			if (g.getPlayer().equals(event.getPlayer())) {
				g.delete();
				BenCmd.getPlugin().graves.remove(i);
			}
		}
		if (BenCmd.getPlugin().returns.containsKey(event.getPlayer())) {
			BenCmd.getPlugin().returns.remove(event.getPlayer());
		}
		User user = User.getUser(event.getPlayer());

		// Format quit message
		if (user.isOffline()) {
			user.goOnlineNoMsg();
			event.setLeaveMessage(null);
		} else {
			event.setLeaveMessage(user.getColor() + user.getDisplayName() + ChatColor.WHITE + " has left the game...");
		}

		// Remove them from the maximum players lists
		BenCmd.getPermissionManager().getMaxPlayerHandler().leave(user);

		// Remove all special stauses from them, if applicable
		if (user.isPoofed()) {
			user.unPoof();
		}
		if (user.isNoPoofed()) {
			user.unNoPoof();
		}
		if (user.isAllPoofed()) {
			user.unAllPoof();
		}
		if (user.inChannel()) {
			user.leaveChannel();
		}
		if (user.isGod()) {
			user.makeNonGod();
		}
		user.unspyAll();

		// Destroy their user instance
		User.finalizeUser(user);

		// Announce kick to console (McMyAdmin compatibility)
		BenCmd.log(user.getName() + " lost connection: User was kicked");
	}

	private void bookshelfInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.BOOKSHELF) {
			Shelf shelf;
			if ((shelf = BenCmd.getShelfFile().getShelf(event.getClickedBlock().getLocation())) != null) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.YELLOW + "The books on this shelf read:");
				event.getPlayer().sendMessage(ChatColor.YELLOW + shelf.getText());
			}
		}
	}

	private void disposalChestInteract(PlayerInteractEvent event) {
		InventoryBackend back = InventoryBackend.getInstance();
		if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		Block block = event.getClickedBlock();
		Player player = event.getPlayer();
		if (back.TryDispense(block)) {
			event.setCancelled(true);
		}
		if (block.getType() == Material.CHEST && BenCmd.getDisposals().isDisposalChest(block.getLocation())) {
			CraftChest chest = new CraftChest(block);
			chest.getInventory().clear();
			player.sendMessage(ChatColor.RED + "ALERT: The chest you have opened is a disposal chest! Anything you put inside will disappear FOREVER!");
		}
	}

	private void strikeBind(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
			return;
		}
		if (BenCmd.getStrikeBindings().bindEquipped(event.getPlayer())) {
			Bukkit.dispatchCommand(event.getPlayer(), "strike");
		}
	}

	private void lockInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.BED_BLOCK) {
			if (event.getPlayer().getWorld().getEnvironment() == Environment.NETHER) {
				event.getPlayer().sendMessage(ChatColor.RED + "Did you really think that would work!?");
				BenCmd.log(event.getPlayer().getDisplayName() + " attempted to use a bed in the Nether.");
				event.setCancelled(true);
			}
		}
		if ((event.getAction() != Action.RIGHT_CLICK_BLOCK && !(event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.WOODEN_DOOR)) || event.isCancelled()) {
			return;
		}
		int id;
		ProtectedBlock block;
		if ((id = BenCmd.getProtections().getProtection(event.getClickedBlock().getLocation())) != -1) {
			block = BenCmd.getProtections().getProtection(id);
			User user = User.getUser(event.getPlayer());
			if (!block.canUse(user) && !user.hasPerm("bencmd.lock.peek")) {
				event.setCancelled(true);
				user.sendMessage(ChatColor.RED + "That block is locked! Use /protect info for more information...");
			} else {
				if (!user.getName().equalsIgnoreCase(block.getOwner().getName())) {
					BenCmd.log(user.getDisplayName() + " has accessed " + block.getOwner().getName() + "'s protected block. (" + block.GetId() + ")");
				}
			}
		}
	}

	private void lotSelectInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Player player = event.getPlayer();
			if (User.getUser(player).hasPerm("bencmd.lot.select") && player.getItemInHand().getType() == Material.WOOD_SPADE) {
				checkPlayer(player.getName());
				if (this.corner.get(player.getName()).corner2set) {
					if (this.corner.get(player.getName()).getCorner2().equals(event.getClickedBlock().getLocation())) {
						return;
					}
				}
				this.corner.get(player.getName()).setCorner2(event.getClickedBlock().getLocation());
				Location corner2 = this.corner.get(player.getName()).getCorner2();
				player.sendMessage(ChatColor.LIGHT_PURPLE + "Corner 2 set at [X: " + corner2.getX() + ", Y: " + corner2.getY() + ", Z: " + corner2.getZ() + ", W: " + corner2.getWorld().getName() + "]");
			}
			return;
		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			Player player = event.getPlayer();
			if (User.getUser(player).hasPerm("bencmd.lot.select") && player.getItemInHand().getType() == Material.WOOD_SPADE) {
				checkPlayer(player.getName());
				if (this.corner.get(player.getName()).corner1set) {
					if (this.corner.get(player.getName()).getCorner1().equals(event.getClickedBlock().getLocation())) {
						return;
					}
				}
				this.corner.get(player.getName()).setCorner1(event.getClickedBlock().getLocation());
				Location corner1 = this.corner.get(player.getName()).getCorner1();
				player.sendMessage(ChatColor.LIGHT_PURPLE + "Corner 1 set at [X: " + corner1.getX() + ", Y: " + corner1.getY() + ", Z: " + corner1.getZ() + ", W: " + corner1.getWorld().getName() + "]");
			}
			return;
		}
	}

	private void lotBucketEmpty(PlayerBucketEmptyEvent event) {
		Player player = event.getPlayer();

		if (!BenCmd.getLots().canBuildHere(player, event.getBlockClicked().getLocation())) {
			event.setCancelled(true);
			player.sendMessage("You cannot build here.");
		}
	}

	private void lotBucketFill(PlayerBucketFillEvent event) {
		Player player = event.getPlayer();

		if (!BenCmd.getLots().canBuildHere(player, event.getBlockClicked().getLocation())) {
			event.setCancelled(true);
			player.sendMessage("You cannot build here.");
		}
	}

	private void pvpRespawn(PlayerRespawnEvent event) {
		if (BenCmd.getPlugin().returns.containsKey(event.getPlayer())) {
			for (ItemStack i : BenCmd.getPlugin().returns.get(event.getPlayer())) {
				event.getPlayer().getInventory().addItem(i);
			}
			BenCmd.getPlugin().returns.remove(event.getPlayer());
		}
	}

	private void areaMoveCheck(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		if (ignore.contains(p)) {
			event.setCancelled(true);
			return;
		}
		if (!areas.containsKey(p)) {
			areas.put(p, new ArrayList<SPArea>());
		}
		for (SPArea a : BenCmd.getAreas().listAreas()) {
			if (a instanceof PVPArea && a.insideArea(p.getLocation())) {
				int money = 0;
				for (Currency c : BenCmd.getMarketController().getCurrencies()) {
					for (ItemStack i : p.getInventory().all(c.getMaterial()).values()) {
						if (i.getDurability() == c.getDurability()) {
							money += Math.floor(i.getAmount() * c.getPrice());
						}
					}
				}
				if (money < ((PVPArea) a).getMinimumCurrency()) {
					ignore.add(p);
					p.sendMessage(ChatColor.RED + "You must have " + ((PVPArea) a).getMinimumCurrency() + " worth of currency to PVP in this area...");
					int c = 1;
					while (true) {
						Location f = p.getLocation();
						f.setX(f.getX() + c);
						if (!a.insideArea(f) && f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setX(f.getX() - (c * 2));
						if (!a.insideArea(f) && f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setX(f.getX() + c);
						f.setZ(f.getZ() + c);
						if (!a.insideArea(f) && f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setZ(f.getZ() - (c * 2));
						if (!a.insideArea(f) && f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setZ(f.getZ() + c);
						f.setY(f.getY() + c);
						if (!a.insideArea(f) && f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setY(f.getY() - (c * 2));
						if (!a.insideArea(f) && f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						c += 1;
					}
				}
			}
			if (a instanceof TRArea) {
				if (a.insideArea(p.getLocation()) && ((TRArea) a).isLocked(p)) {
					ignore.add(p);
					p.sendMessage(ChatColor.RED + "You cannot enter this area at this time!");
					int c = 1;
					while (true) {
						Location f = p.getLocation();
						f.setX(f.getX() + c);
						if (!a.insideArea(f) && f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setX(f.getX() - (c * 2));
						if (!a.insideArea(f) && f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setX(f.getX() + c);
						f.setZ(f.getZ() + c);
						if (!a.insideArea(f) && f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setZ(f.getZ() - (c * 2));
						if (!a.insideArea(f) && f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setZ(f.getZ() + c);
						f.setY(f.getY() + c);
						if (!a.insideArea(f) && f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setY(f.getY() - (c * 2));
						if (!a.insideArea(f) && f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						c += 1;
					}
				}
			}
			if (a instanceof MsgArea) {
				if (a.insideArea(p.getLocation())) {
					if (!areas.get(p).contains(a)) {
						if (((MsgArea) a).getEnterMessage().startsWith("§") && ((MsgArea) a).getEnterMessage().length() == 2) {
							return;
						}
						p.sendMessage(((MsgArea) a).getEnterMessage());
						areas.get(p).add(a);
					}
				} else {
					if (areas.get(p).contains(a)) {
						if (((MsgArea) a).getLeaveMessage().startsWith("§") && ((MsgArea) a).getLeaveMessage().length() == 2) {
							return;
						}
						p.sendMessage(((MsgArea) a).getLeaveMessage());
						areas.get(p).remove(a);
					}
				}
			}
		}
	}

	private void checkPortal(PlayerPortalEvent event) {
		if (!BenCmd.getMainProperties().getBoolean("BenCmdPortals", true)) {
			return;
		}
		Portal portal;
		Location loc = event.getPlayer().getLocation();
		if ((portal = BenCmd.getPortalFile().getPortalAt(loc)) == null) {
			event.getPlayer().sendMessage(ChatColor.RED + "That portal doesn't lead anywhere!");
			event.setCancelled(true);
			return;
		}
		if (portal.getGroup() != null && !User.getUser(event.getPlayer()).inGroup(portal.getGroup())) {
			event.getPlayer().sendMessage(ChatColor.RED + "You're not allowed to use that portal!");
			event.setCancelled(true);
			return;
		}
		event.useTravelAgent(false);
		if (portal instanceof HomePortal) {
			Warp warp;
			if ((warp = ((HomePortal) portal).getWarp(User.getUser(event.getPlayer()))) != null) {
				event.setTo(warp.loc);
			} else {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "You haven't set a home #" + ((HomePortal) portal).getHomeNumber() + " yet!");
			}
		} else {
			event.setTo(portal.getWarp().loc);
		}
		BenCmd.getWarpCheckpoints().SetPreWarp(event.getPlayer());
	}

	private void flyTeleport(PlayerTeleportEvent event) {
		if (event.isCancelled()) {
			return;
		}
		BenCmd.getFlymodDetector().lastL.put(event.getPlayer(), event.getTo());
		if (BenCmd.isSpoutConnected()) {
			sendSkins(event.getPlayer(), event.getTo());
		}
	}

	private void flyPortal(PlayerPortalEvent event) {
		if (event.isCancelled()) {
			return;
		}
		BenCmd.getFlymodDetector().lastL.put(event.getPlayer(), event.getTo());
		if (BenCmd.isSpoutConnected()) {
			sendSkins(event.getPlayer(), event.getTo());
		}
	}

	private void loginCheck(PlayerLoginEvent event) {
		PermissionUser user;
		if (!BenCmd.getPermissionManager().getUserFile().userExists(event.getPlayer().getName())) {
			if (BenCmd.getMainProperties().getBoolean("disallowNewUsers", false)) {
				event.disallow(Result.KICK_WHITELIST, BenCmd.getMainProperties().getString("newUserKick", "You aren't whitelisted on this server!"));
				return;
			} else {
				BenCmd.getPermissionManager().getUserFile().addUser(user = PermissionUser.newUser(event.getPlayer().getName(), new ArrayList<String>()));
			}
		} else {
			user = PermissionUser.matchUser(event.getPlayer().getName());
		}
		if (BenCmd.getPermissionManager().getGroupFile().getAllUserGroups(user).isEmpty()) {
			BenCmd.getPermissionManager().getGroupFile().getGroup(BenCmd.getMainProperties().getString("defaultGroup", "default")).addUser(user);
		}
		if ((User.getUser(event.getPlayer())).isBanned() != null) {
			event.disallow(Result.KICK_BANNED, "You are currently banned from this server!");
			return;
		}
		long timeLeft;
		if ((timeLeft = BenCmd.getPermissionManager().getKickTracker().isBlocked(event.getPlayer().getName())) > 0) {
			event.disallow(Result.KICK_OTHER, "You cannot connect for " + String.valueOf((int) Math.ceil(timeLeft / 60000.0)) + " more minutes...");
			return;
		}
		switch (BenCmd.getPermissionManager().getMaxPlayerHandler().join(User.getUser(event.getPlayer()))) {
			case NO_SLOT_NORMAL:
				event.disallow(Result.KICK_FULL, BenCmd.getMainProperties().getString("noNormal", "There are no normal slots currently available!"));
				break;
			case NO_SLOT_RESERVED:
				event.disallow(Result.KICK_FULL, BenCmd.getMainProperties().getString("noReserved", "There are no normal slots or reserved slots currently available!"));
				break;
		}
		User.finalizeUser(User.getUser(event.getPlayer()));
	}

	private void jailPickupCheck(PlayerPickupItemEvent event) {
		User user = User.getUser(event.getPlayer());
		if (user.isJailed() != null) {
			event.setCancelled(true);
		}
	}

	private void jailDropCheck(PlayerDropItemEvent event) {
		User user = User.getUser(event.getPlayer());
		if (user.isJailed() != null) {
			event.setCancelled(true);
		}
	}

	private void npcInteractEntity(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof CraftPlayer) {
			if (((CraftPlayer) event.getRightClicked()).getHandle() instanceof EntityNPC) {
				NPC npc = BenCmd.getNPCFile().getNPC((EntityNPC) ((CraftPlayer) event.getRightClicked()).getHandle());
				if (npc == null) {
					BenCmd.log(Level.WARNING, "Ghost NPC detected... Try restarting the server...");
					return;
				}
				if (event.getPlayer().getItemInHand().getType() == Material.STICK && User.getUser(event.getPlayer()).hasPerm("bencmd.npc.info")) {
					npcInfo(event.getPlayer(), npc);
					return;
				}
				if (npc instanceof Clickable) {
					((Clickable) npc).onRightClick(event.getPlayer());
				}
			}
		}
	}

	private void npcInfo(Player p, NPC n) {
		if (BenCmd.isSpoutConnected() && BenCmd.getSpoutConnector().enabled(p)) {
			BenCmd.getSpoutConnector().showNPCScreen(p, n);
		} else {
			p.sendMessage(ChatColor.GRAY + "NPC ID: " + n.getID());
			if (n instanceof BankerNPC) {
				p.sendMessage(ChatColor.GRAY + "NPC Type: Banker");
			} else if (n instanceof BankManagerNPC) {
				p.sendMessage(ChatColor.GRAY + "NPC Type: Bank Manager");
			} else if (n instanceof BlacksmithNPC) {
				p.sendMessage(ChatColor.GRAY + "NPC Type: Blacksmith");
			} else if (n instanceof StaticNPC) {
				p.sendMessage(ChatColor.GRAY + "NPC Type: Static");
			} else {
				p.sendMessage(ChatColor.GRAY + "NPC Type: Unknown");
			}
			p.sendMessage(ChatColor.GRAY + "NPC Name: " + n.getName());
			p.sendMessage(ChatColor.GRAY + "Skin URL: " + n.getSkinURL());
		}
	}

	// Split-off events

	public void onPlayerChat(PlayerChatEvent event) {
		chat(event);
	}

	public void onPlayerJoin(PlayerJoinEvent event) {
		userInit(event);
	}

	public void onPlayerQuit(PlayerQuitEvent event) {
		quitFinalize(event);
	}

	public void onPlayerKick(PlayerKickEvent event) {
		kickFinalize(event);
	}

	public void onPlayerInteract(PlayerInteractEvent event) {
		bookshelfInteract(event);
		lockInteract(event);
		disposalChestInteract(event);
		strikeBind(event);
		lotSelectInteract(event);
	}

	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		lotBucketEmpty(event);
	}

	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		lotBucketFill(event);
	}

	public void onPlayerRespawn(PlayerRespawnEvent event) {
		pvpRespawn(event);
	}

	public void onPlayerMove(PlayerMoveEvent event) {
		sendSkins(event.getPlayer(), event.getTo());
		areaMoveCheck(event);
	}

	public void onPlayerPortal(PlayerPortalEvent event) {
		checkPortal(event);
		flyPortal(event);
	}

	public void onPlayerTeleport(PlayerTeleportEvent event) {
		sendSkins(event.getPlayer(), event.getTo());
		flyTeleport(event);
	}

	public void onPlayerLogin(PlayerLoginEvent event) {
		loginCheck(event);
	}

	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		jailPickupCheck(event);
	}

	public void onPlayerDropItem(PlayerDropItemEvent event) {
		jailDropCheck(event);
	}

	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		npcInteractEntity(event);
	}

}
