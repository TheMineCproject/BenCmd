package ben_dude56.plugins.bencmd.multiworld;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPortalEvent;

import ben_dude56.plugins.bencmd.BenCmd;
import ben_dude56.plugins.bencmd.User;

public class PortalListener extends PlayerListener {
	BenCmd plugin;

	public PortalListener(BenCmd instance) {
		plugin = instance;
	}

	public void onPlayerPortal(PlayerPortalEvent event) {
		Portal portal;
		Location loc = event.getPlayer().getLocation();
		loc.setX(loc.getX() * 8);
		loc.setZ(loc.getZ() * 8);
		if ((portal = plugin.portals.getPortalAt(loc)) == null) {
			event.getPlayer().sendMessage(
					ChatColor.RED + "That portal doesn't lead anywhere!");
			event.setCancelled(true);
			return;
		}
		if(portal.getGroup() != null && !User.getUser(plugin, event.getPlayer()).inGroup(portal.getGroup())) {
			event.getPlayer().sendMessage(
					ChatColor.RED + "You're not allowed to use that portal!");
			event.setCancelled(true);
			return;
		}
		event.useTravelAgent(false);
		event.setTo(portal.getWarp().loc);
		//plugin.checkpoints.SetPreWarp(event.getPlayer());
	}
}
