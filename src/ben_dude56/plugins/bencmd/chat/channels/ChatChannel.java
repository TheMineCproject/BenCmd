package ben_dude56.plugins.bencmd.chat.channels;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import ben_dude56.plugins.bencmd.User;
import ben_dude56.plugins.bencmd.chat.ChatChecker;
import ben_dude56.plugins.bencmd.chat.SlowMode;
import ben_dude56.plugins.bencmd.permissions.PermissionUser;

public class ChatChannel {
	
	//General properties
	private String name;
	private PermissionUser owner;
	private ChatLevel defLevel;
	private String motd;
	
	// Loaded allow/deny lists
	private List<PermissionUser> mods;
	private List<PermissionUser> guests;
	private List<PermissionUser> banned;
	private List<PermissionUser> muted;
	
	// Memory-only variables
	private ChatChannelController control;
	private List<User> inChannel;
	private List<User> spies;
	private SlowMode slow;
	
	// Constructors
	protected static ChatChannel getChannel(ChatChannelController control, String key, String value) {
		if(value.split("/").length != 7) {
			control.plugin.log.severe("ChatChannel " + key + " encountered a fatal error while loading and has been disabled:");
			control.plugin.log.severe("Invalid number of entries.");
			return null;
		}
		PermissionUser owner = PermissionUser.matchUser(value.split("/")[0], control.plugin);
		if(owner == null) {
			control.plugin.log.severe("ChatChannel " + key + " encountered a fatal error while loading and has been disabled:");
			control.plugin.log.severe("Owner \"" + value.split("/")[0] + "\" doesn't exist in users.db.");
			return null;
		}
		List<PermissionUser> mods = new ArrayList<PermissionUser>();
		for(String player : value.split("/")[1].split(",")) {
			PermissionUser mod = PermissionUser.matchUser(player, control.plugin);
			if(mod == null) {
				control.plugin.log.severe("ChatChannel " + key + " encountered a fatal error while loading and has been disabled:");
				control.plugin.log.severe("Mod \"" + player + "\" doesn't exist in users.db.");
				return null;
			}
			mods.add(mod);
		}
		List<PermissionUser> guests = new ArrayList<PermissionUser>();
		for(String player : value.split("/")[2].split(",")) {
			PermissionUser guest = PermissionUser.matchUser(player, control.plugin);
			if(guest == null) {
				control.plugin.log.severe("ChatChannel " + key + " encountered a fatal error while loading and has been disabled:");
				control.plugin.log.severe("Guest \"" + player + "\" doesn't exist in users.db.");
				return null;
			}
			guests.add(guest);
		}
		List<PermissionUser> banned = new ArrayList<PermissionUser>();
		for(String player : value.split("/")[3].split(",")) {
			PermissionUser ban = PermissionUser.matchUser(player, control.plugin);
			if(ban == null) {
				control.plugin.log.severe("ChatChannel " + key + " encountered a fatal error while loading and has been disabled:");
				control.plugin.log.severe("Banned player \"" + player + "\" doesn't exist in users.db.");
				return null;
			}
			banned.add(ban);
		}
		List<PermissionUser> muted = new ArrayList<PermissionUser>();
		for(String player : value.split("/")[4].split(",")) {
			PermissionUser mute = PermissionUser.matchUser(player, control.plugin);
			if(mute == null) {
				control.plugin.log.severe("ChatChannel " + key + " encountered a fatal error while loading and has been disabled:");
				control.plugin.log.severe("Muted player \"" + player + "\" doesn't exist in users.db.");
				return null;
			}
			muted.add(mute);
		}
		ChatLevel joinType;
		if(value.split("/")[5].equalsIgnoreCase("b")) {
			joinType = ChatLevel.BANNED;
		} else if (value.split("/")[5].equalsIgnoreCase("m")) {
			joinType = ChatLevel.MUTED;
		} else if (value.split("/")[5].equalsIgnoreCase("d")) {
			joinType = ChatLevel.DEFAULT;
		} else {
			control.plugin.log.warning("ChatChannel " + key + " encountered a minor error while loading:");
			control.plugin.log.warning("\"" + value.split("/") + "\" is not a valid value for Default Join Type. Assumed \"d\".");
			joinType = ChatLevel.DEFAULT;
		}
		String motd = value.split("/")[6];
		return new ChatChannel(control, key, owner, mods, guests, banned, muted, joinType, motd);
	}

	protected ChatChannel(ChatChannelController control, String name, PermissionUser owner,
			List<PermissionUser> mods, List<PermissionUser> guests,
			List<PermissionUser> banned, List<PermissionUser> muted,
			ChatLevel defaultLevel, String motd) {
		this.control = control;
		this.name = name;
		this.owner = owner;
		this.mods = mods;
		this.guests = guests;
		this.banned = banned;
		this.muted = muted;
		this.defLevel = defaultLevel;
		this.motd = motd;
		this.inChannel = new ArrayList<User>();
		this.spies = new ArrayList<User>();
		this.slow = new SlowMode(control.plugin, control.plugin.mainProperties.getInteger(
				"slowTime", 10000));
	}
	
	// Value-retrieving functions
	protected String getValue() {
		String value = owner.getName() + "/";
		for(int i = 0; i < mods.size(); i++) {
			if(i != 0) {
				value += ",";
			}
			value += mods.get(i).getName();
		}
		value += "/";
		for(int i = 0; i < guests.size(); i++) {
			if(i != 0) {
				value += ",";
			}
			value += guests.get(i).getName();
		}
		value += "/";
		for(int i = 0; i < banned.size(); i++) {
			if(i != 0) {
				value += ",";
			}
			value += banned.get(i).getName();
		}
		value += "/";
		for(int i = 0; i < muted.size(); i++) {
			if(i != 0) {
				value += ",";
			}
			value += muted.get(i).getName();
		}
		value += "/";
		switch (defLevel) {
		case BANNED:
			value += "b";
		case MUTED:
			value += "m";
		default:
			value += "d";
		}
		return value;
	}
	
	public String getName() {
		return name;
	}
	
	// Level-checking functions
	public ChatLevel getDefaultLevel() {
		return defLevel;
	}
	
	public boolean isOwner(PermissionUser user) {
		return owner.getName().equals(user.getName());
	}
	
	public boolean isMod(PermissionUser user) {
		for(PermissionUser mod : mods) {
			if(mod.getName().equals(user.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isBanned(PermissionUser user) {
		for(PermissionUser ban : banned) {
			if(ban.getName().equals(user.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isMuted(PermissionUser user) {
		for(PermissionUser mute : muted) {
			if(mute.getName().equals(user.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isGuested(PermissionUser user) {
		for(PermissionUser guest : guests) {
			if(guest.getName().equals(user.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public ChatLevel getLevel(PermissionUser user) {
		if(isOwner(user)) {
			return ChatLevel.OWNER;
		}
		if(isMod(user)) {
			return ChatLevel.MOD;
		}
		if(isGuested(user)) {
			return ChatLevel.DEFAULT;
		}
		if(user.hasPerm("isUniversalMod")) {
			return ChatLevel.DEFAULT;
		}
		if(isBanned(user)) {
			return ChatLevel.BANNED;
		}
		if(isMuted(user)) {
			return ChatLevel.MUTED;
		}
		return defLevel;
	}
	
	public String getMotd() {
		return motd;
	}
	
	// Slow mode functions
	public void enableSlow() {
		slow.EnableSlow();
		this.broadcastMessage(
				ChatColor.GRAY + "Slow mode has been enabled. You must wait "
						+ (slow.getDefTime() / 1000)
						+ " seconds between each chat message.");
	}
	
	public void enableSlow(int millis) {
		slow.EnableSlow(millis);
		this.broadcastMessage(
				ChatColor.GRAY + "Slow mode has been enabled. You must wait "
						+ (slow.getDefTime() / 1000)
						+ " seconds between each chat message.");
	}
	
	public void disableSlow() {
		slow.DisableSlow();
		this.broadcastMessage(
				ChatColor.GRAY + "Slow mode has been disabled.");
	}
	
	public boolean isSlow() {
		return slow.isEnabled();
	}
	
	// Online-status functions and methods
	private void forceJoin(User user) {
		if(isOwner(user)) {
			broadcastMessage(ChatColor.GOLD + "*" + user.getColor() + user.getName() + ChatColor.WHITE + " has joined the chat");
		} else if(isMod(user)) {
			broadcastMessage(ChatColor.GRAY + "*" + user.getColor() + user.getName() + ChatColor.WHITE + " has joined the chat");
		} else {
			broadcastMessage(user.getColor() + user.getName() + ChatColor.WHITE + " has joined the chat");
		}
		inChannel.add(user);
	}
	
	public ChatLevel joinChannel(User user) {
		ChatLevel level;
		switch(level = getLevel(user)) {
		case BANNED:
			user.sendMessage(ChatColor.RED + "You're not allowed to join that channel!");
		case MUTED:
			user.sendMessage(ChatColor.WHITE + "You have joined " + ChatColor.GREEN + this.name);
			user.sendMessage(ChatColor.YELLOW + motd);
			user.sendMessage(ChatColor.RED + "Please note that you are muted on this channel...");
			forceJoin(user);
		default:
			user.sendMessage(ChatColor.WHITE + "You have joined " + ChatColor.GREEN + this.name);
			user.sendMessage(ChatColor.YELLOW + motd);
			if(user.hasPerm("isUniversalMod") && !isMod(user) && !isOwner(user)) {
				Mod(user);
			}
			forceJoin(user);
		}
		return level;
	}
	
	public void leaveChannel(User user) {
		for(int i = 0; i < inChannel.size(); i++) {
			if(inChannel.get(i).getName().equalsIgnoreCase(user.getName())) {
				inChannel.remove(i);
				if(isOwner(user)) {
					broadcastMessage(ChatColor.GOLD + "*" + user.getColor() + user.getName() + ChatColor.WHITE + " has left the chat");
				} else if(isMod(user)) {
					broadcastMessage(ChatColor.GRAY + "*" + user.getColor() + user.getName() + ChatColor.WHITE + " has left the chat");
				} else {
					broadcastMessage(user.getColor() + user.getName() + ChatColor.WHITE + " has left the chat");
				}
				return;
			}
		}
		user.sendMessage(ChatColor.GRAY + "You successfully left the chat channel: " + name);
	}
	
	public boolean Kick(User user) {
		if(isOwner(user)) {
			return false;
		}
		if(isMod(user)) {
			return false;
		}
		if(isOnline(user) != null) {
			leaveChannel(user);
			user.sendMessage(ChatColor.RED + "You have been kicked from your active chat channel.");
			return true;
		} else {
			return false;
		}
	}
	
	public User isOnline(PermissionUser user) {
		for(User online : inChannel) {
			if(online.getName().equals(user.getName())) {
				return online;
			}
		}
		return null;
	}
	
	public boolean Spy(User user) {
		if(isMod(user)) {
			if(isOnline(user) != null) {
				user.sendMessage(ChatColor.RED + "You can't spy on a channel that you're already in!");
				return false;
			}
			if(isSpying(user)) {
				user.sendMessage(ChatColor.RED + "You're already spying on that channel!");
				return false;
			}
			spies.add(user);
			user.sendMessage(ChatColor.GRAY + "You are now spying on the Chat Channel: " + name);
			user.sendMessage(ChatColor.YELLOW + motd);
			return true;
		} else {
			user.sendMessage(ChatColor.RED + "You must be a mod in that channel to spy on it!");
			return false;
		}
	}
	
	public boolean isSpying(User user) {
		for(User online : spies) {
			if(online.getName().equals(user.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean Unspy(User user) {
		if(isMod(user)) {
			if(!isSpying(user)) {
				user.sendMessage(ChatColor.RED + "You're not spying on that channel!");
				return false;
			}
			for(int i = 0; i < spies.size(); i++) {
				if(spies.get(i).getName().equalsIgnoreCase(user.getName())) {
					spies.remove(i);
				}
			}
			user.sendMessage(ChatColor.GRAY + "You are no longer spying on Chat Channel: " + name);
			return true;
		} else {
			user.sendMessage(ChatColor.RED + "You must be a mod in that channel to spy on it!");
			return false;
		}
	}
	
	// Messaging methods
	public void sendChat(User user, String message) {
		if(isMuted(user)) {
			user.sendMessage(ChatColor.GRAY
					+ control.plugin.mainProperties.getString("muteMessage",
							"You are muted..."));
		}
		boolean blocked = ChatChecker.checkBlocked(message, control.plugin);
		if (blocked) {
			user.sendMessage(ChatColor.GRAY
					+ control.plugin.mainProperties.getString("blockMessage",
							"You used a blocked word..."));
			return;
		}
		long slowTimeLeft = slow.playerBlocked(user.getName());
		if (!(isMod(user) || isOwner(user)) && slow.isEnabled()) {
			if (slowTimeLeft > 0) {
				user.sendMessage(ChatColor.GRAY
						+ "Slow mode is enabled! You must wait "
						+ (int) Math.ceil(slowTimeLeft / 1000)
						+ " more second(s) before you can talk again.");
				return;
			} else {
				slow.playerAdd(user.getName());
			}
		}
		String username = user.getColor() + user.getName();
		if(isOwner(user)) {
			username = ChatColor.GOLD + "*" + username;
		} else if(isMod(user)) {
			username = ChatColor.GRAY + "*" + username;
		}
		String prefix;
		if (user.getGroup() != null
				&& !(prefix = user.getGroup().getPrefix()).isEmpty()) {
			message = user.getGroup().getPrefixColor() + "[" + prefix + "] "
					+ username + ": " + ChatColor.WHITE + message;
			this.broadcastMessage(message);
		} else {
			message = username + ": " + ChatColor.WHITE + message;
			this.broadcastMessage(message);
		}
	}
	
	protected void broadcastMessage(String message) {
		control.plugin.log.info("(" + name + ") " + message);
		for(User user : inChannel) {
			user.sendMessage(ChatColor.YELLOW + "(" + this.name + ") " + ChatColor.WHITE + message);
		}
		for(User user : spies) {
			user.sendMessage(ChatColor.YELLOW + "(" + this.name + ") " + ChatColor.WHITE + message);
		}
	}
	
	public void listUsers(User user) {
		String value = "";
		for(User online : inChannel) {
			if(value.isEmpty()) {
				value += online.getName();
			} else {
				value += ", " + online.getName();
			}
		}
		user.sendMessage(ChatColor.GRAY + "The following users are on this chat channel: ");
		user.sendMessage(ChatColor.GRAY + value);
	}
	
	// Permission-changing functions
	protected void changeOwner(PermissionUser user) {
		PermissionUser oldowner = owner;
		owner = user;
		Mod(oldowner);
		control.saveChannel(this);
	}
	
	protected void setMotd(String message) {
		motd = message;
		control.saveChannel(this);
	}
	
	private boolean Unmod(PermissionUser user) {
		User online;
		if((online = isOnline(user)) != null && isSpying(online)) {
			online.unspyChannel(this);
		}
		for(int i = 0; i < mods.size(); i++) {
			if(mods.get(i).getName().equalsIgnoreCase(user.getName())) {
				mods.remove(i);
				return true;
			}
		}
		return false;
	}
	
	public boolean Mod(PermissionUser user) {
		if(isOwner(user)) {
			return false;
		}
		if(isMod(user)) {
			return false;
		}
		if(isGuested(user)) {
			Unguest(user);
		}
		if(isMuted(user)) {
			Unmute(user);
		}
		if(isBanned(user)) {
			Unban(user);
		}
		mods.add(user);
		User online;
		if((online = isOnline(user)) != null) {
			online.sendMessage(ChatColor.GREEN + "You have been promoted to mod in this channel.");
		}
		control.saveChannel(this);
		return true;
	}
	
	private boolean Unguest(PermissionUser user) {
		for(int i = 0; i < guests.size(); i++) {
			if(guests.get(i).getName().equalsIgnoreCase(user.getName())) {
				guests.remove(i);
				return true;
			}
		}
		return false;
	}
	
	public boolean Guest(PermissionUser user) {
		if(isOwner(user)) {
			return false;
		}
		if(isGuested(user)) {
			return false;
		}
		if(isMod(user)) {
			Unmod(user);
		}
		if(isMuted(user)) {
			Unmute(user);
		}
		if(isBanned(user)) {
			Unban(user);
		}
		guests.add(user);
		User online;
		if((online = isOnline(user)) != null) {
			online.sendMessage(ChatColor.GREEN + "You are now a guest in this channel.");
		}
		control.saveChannel(this);
		return true;
	}
	
	private boolean Unmute(PermissionUser user) {
		for(int i = 0; i < muted.size(); i++) {
			if(muted.get(i).getName().equalsIgnoreCase(user.getName())) {
				muted.remove(i);
				return true;
			}
		}
		return false;
	}

	public boolean Mute(PermissionUser user) {
		if(isOwner(user)) {
			return false;
		}
		if(isMuted(user)) {
			return false;
		}
		if(isMod(user)) {
			Unmod(user);
		}
		if(isGuested(user)) {
			Unguest(user);
		}
		if(isBanned(user)) {
			Unban(user);
		}
		muted.add(user);
		User online;
		if((online = isOnline(user)) != null) {
			online.sendMessage(ChatColor.RED + "You have been muted in this channel.");
		}
		control.saveChannel(this);
		return true;
	}
	
	private boolean Unban(PermissionUser user) {
		if(isOwner(user)) {
			return false;
		}
		for(int i = 0; i < banned.size(); i++) {
			if(banned.get(i).getName().equalsIgnoreCase(user.getName())) {
				banned.remove(i);
				return true;
			}
		}
		return false;
	}
	
	public boolean Ban(PermissionUser user) {
		if(isOwner(user)) {
			return false;
		}
		if(isBanned(user)) {
			return false;
		}
		if(isMod(user)) {
			Unmod(user);
		}
		if(isGuested(user)) {
			Unguest(user);
		}
		if(isMuted(user)) {
			Unmute(user);
		}
		banned.add(user);
		User online;
		if((online = isOnline(user)) != null) {
			Kick(online);
		}
		return true;
	}
	
	public static enum ChatLevel {
		OWNER, MOD, DEFAULT, MUTED, BANNED
	}
}
