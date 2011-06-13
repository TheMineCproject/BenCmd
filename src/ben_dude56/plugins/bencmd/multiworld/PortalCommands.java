package ben_dude56.plugins.bencmd.multiworld;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ben_dude56.plugins.bencmd.BenCmd;
import ben_dude56.plugins.bencmd.Commands;
import ben_dude56.plugins.bencmd.User;
import ben_dude56.plugins.bencmd.permissions.PermissionGroup;
import ben_dude56.plugins.bencmd.warps.Warp;

public class PortalCommands implements Commands {

	BenCmd plugin;
	
	public PortalCommands(BenCmd instance) {
		plugin = instance;
	}
	
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		User user;
		try {
			user = User.getUser(plugin, (Player) sender);
		} catch (ClassCastException e) {
			user = User.getUser(plugin);
		}
		if(commandLabel.equalsIgnoreCase("setportal")
				&& user.hasPerm("canEditWarps")) {
			SetPortal(args, user);
			return true;
		}
		return false;
	}
	
	public void SetPortal(String[] args, User user) {
		if(args.length == 0) {
			user.sendMessage(ChatColor.YELLOW
					+ "Proper use is /setportal <warp> [group]");
		} else {
			Block pointedAt = user.getHandle().getTargetBlock(null, 4);
			if (pointedAt.getType() != Material.PORTAL){
				user.sendMessage(ChatColor.RED + "You're not pointing at a portal!");
			}
			Location handle = Portal.getHandleBlock(pointedAt.getLocation());
			Warp warp = null;
			Integer homeNumber = null;
			if(args[0].startsWith("home")) {
				try {
					homeNumber = Integer.parseInt(args[0].replaceFirst("home", ""));
				} catch (NumberFormatException e) {
					user.sendMessage(ChatColor.RED + args[0].replaceFirst("home", "") + " cannot be converted into a number!");
					return;
				}
			} else if((warp = plugin.warps.getWarp(args[0])) == null) {
				user.sendMessage(ChatColor.RED + "That warp doesn't exist!");
				return;
			}
			PermissionGroup group = null;
			if(args.length == 2) {
				try {
					group = new PermissionGroup(plugin, args[1]);
				} catch (NullPointerException e) {
					user.sendMessage(ChatColor.RED + "That group doesn't exist!");
					return;
				}
			}
			if(homeNumber == null) {
				plugin.portals.addPortal(new Portal(handle, group, warp));
				user.sendMessage(ChatColor.GREEN + "That portal has been set to warp " + warp.warpName + "!");
			} else {
				plugin.portals.addPortal(new HomePortal(plugin, handle, group, homeNumber));
				user.sendMessage(ChatColor.GREEN + "That portal has been set to home #" + homeNumber + "!");
			}
		}
	}

}
