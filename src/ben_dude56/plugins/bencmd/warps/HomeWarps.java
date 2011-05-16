package ben_dude56.plugins.bencmd.warps;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import ben_dude56.plugins.bencmd.*;

public class HomeWarps {
	BenCmd plugin;
	HomeList homes;

	public HomeWarps(BenCmd instance) {
		plugin = instance;
		homes = new HomeList(plugin);
	}

	public void ReloadHomes() {
		homes.LoadHomes();
	}

	public void SetOwnHome(Player player, Integer HomeNumber) {
		int maxHomes = plugin.mainProperties.getInteger("maxHomes", 3);
		if (HomeNumber <= maxHomes) {
			double x = player.getLocation().getX();
			double y = player.getLocation().getY();
			double z = player.getLocation().getZ();
			double yaw = player.getLocation().getYaw();
			double pitch = player.getLocation().getPitch();
			String world = player.getWorld().getName();
			String name = player.getName() + HomeNumber.toString();
			if (homes.warps.containsKey(name)) {
				homes.removeHome(name);
			}
			homes.addHome(x, y, z, yaw, pitch, world, name, "");
			player.sendMessage(ChatColor.GREEN + "Your home #"
					+ HomeNumber.toString() + " has been successfully set!");
		} else {
			player.sendMessage(ChatColor.RED + "You are only allowed to have "
					+ maxHomes + " homes on this server.");
		}
	}

	public void WarpOwnHome(Player player, Integer HomeNumber) {
		int maxHomes = plugin.mainProperties.getInteger("maxHomes", 3);
		if (HomeNumber <= maxHomes) {
			String name = player.getName() + HomeNumber.toString();
			if (homes.warps.containsKey(name)) {
				homes.getHome(name).WarpHere(new WarpableUser(plugin, player));
			} else {
				player.sendMessage(ChatColor.RED
						+ "You must set that home first!");
			}
		} else {
			player.sendMessage(ChatColor.RED + "You are only allowed to have "
					+ maxHomes + " homes on this server.");
		}
	}

	public void WarpOtherHome(Player player, String otherPlayer,
			Integer HomeNumber) {
		int maxHomes = plugin.mainProperties.getInteger("maxHomes", 3);
		if (HomeNumber <= maxHomes) {
			String name = otherPlayer + HomeNumber.toString();
			if (homes.warps.containsKey(name)) {
				homes.getHome(name).WarpHere(new WarpableUser(plugin, player));
			} else {
				player.sendMessage(ChatColor.RED + otherPlayer
						+ " doesn't have a home #" + HomeNumber.toString()
						+ "!");
			}
		} else {
			player.sendMessage(ChatColor.RED + "You are only allowed to have "
					+ maxHomes + " homes on this server.");
		}
	}

	public void SetOtherHome(Player player, String otherPlayer,
			Integer HomeNumber) {
		int maxHomes = plugin.mainProperties.getInteger("maxHomes", 3);
		if (HomeNumber <= maxHomes) {
			double x = player.getLocation().getX();
			double y = player.getLocation().getY();
			double z = player.getLocation().getZ();
			double yaw = player.getLocation().getYaw();
			double pitch = player.getLocation().getPitch();
			String world = player.getWorld().getName();
			String name = otherPlayer + HomeNumber.toString();
			if (homes.warps.containsKey(name)) {
				homes.removeHome(name);
			}
			homes.addHome(x, y, z, yaw, pitch, world, name, "");
			player.sendMessage(ChatColor.GREEN + otherPlayer + "'s home #"
					+ HomeNumber.toString() + " has been successfully set!");
		} else {
			player.sendMessage(ChatColor.RED + "You are only allowed to have "
					+ maxHomes + " homes on this server.");
		}
	}

	public boolean DeleteHome(String player, Integer HomeNumber) {
		String name = player + HomeNumber.toString();
		if (homes.warps.containsKey(name)) {
			homes.removeHome(name);
			return true;
		} else {
			return false;
		}
	}
}
