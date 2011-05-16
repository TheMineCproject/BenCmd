package ben_dude56.plugins.bencmd.warps;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import ben_dude56.plugins.bencmd.BenCmd;

public class Jail {
	Warp jailWarp;
	BenCmd plugin;
	Logger log = Logger.getLogger("minecraft");

	public Jail(BenCmd instance) {
		plugin = instance;
		jailWarp = loadJail();
	}

	public Warp loadJail() {
		Warp warp;
		String str = plugin.mainProperties.getString("jailLocation",
				"0,0,0,0.0,0.0,world");
		double x = Integer.parseInt(str.split(",")[0]);
		double y = Integer.parseInt(str.split(",")[1]);
		double z = Integer.parseInt(str.split(",")[2]);
		double yaw = Double.parseDouble(str.split(",")[3]);
		double pitch = Double.parseDouble(str.split(",")[4]);
		String world = str.split(",")[5];
		warp = new Warp(x, y, z, yaw, pitch, world, "jail", "", plugin);
		return warp;
	}

	public void setJail(Location loc) {
		jailWarp.loc = new Location(loc.getWorld(), loc.getX(), loc.getY(),
				loc.getZ(), loc.getYaw(), loc.getPitch());
		saveJail();
	}

	public void saveJail() {
		int x = (int) jailWarp.loc.getX();
		int y = (int) jailWarp.loc.getY();
		int z = (int) jailWarp.loc.getZ();
		Double yaw = (double) jailWarp.loc.getYaw();
		Double pitch = (double) jailWarp.loc.getPitch();
		String world = jailWarp.loc.getWorld().getName();
		plugin.mainProperties.setProperty("jailLocation", x + "," + y + "," + z
				+ "," + yaw.toString() + "," + pitch.toString() + "," + world);
		plugin.mainProperties.saveFile("-BenCmd Main Config-");
	}

	public boolean SendToJail(Player player) {
		if (plugin.perm.userFile.hasPermission(player.getName(),
				"cannotBeJailed", true, true)) {
			return false;
		}
		jailWarp.WarpHere(new WarpableUser(plugin, player));
		plugin.perm.userFile.addPermission(player.getName(), "isJailed");
		player.sendMessage(ChatColor.RED
				+ plugin.mainProperties.getString("jailMessage",
						"You have been sent to jail!"));
		log.info(player.getName() + " has been jailed.");
		return true;
	}

	public void LeaveJail(Player player) {
		player.teleport(player.getWorld().getSpawnLocation());
		plugin.perm.userFile.removePermission(player.getName(), "isJailed");
		player.sendMessage(ChatColor.GREEN
				+ plugin.mainProperties.getString("unjailMessage",
						"You have been released..."));
		log.info(player.getName() + " has been unjailed.");
	}
}
