package com.bendude56.bencmd.protect;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

import com.bendude56.bencmd.BenCmd;
import com.bendude56.bencmd.User;


public class ProtectPlayerListener extends PlayerListener {
	BenCmd plugin;

	public ProtectPlayerListener(BenCmd instance) {
		plugin = instance;
	}

	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK
				&& event.getClickedBlock().getType() == Material.BED_BLOCK) {
			if (event.getPlayer().getWorld().getEnvironment() == Environment.NETHER) {
				event.getPlayer().sendMessage(
						ChatColor.RED
								+ "Did you really think that would work!?");
				plugin.log.info(event.getPlayer().getDisplayName()
						+ " attempted to use a bed in the Nether.");
				plugin.bLog.info(event.getPlayer().getDisplayName()
						+ " attempted to use a bed in the Nether.");
				event.setCancelled(true);
			}
		}
		if ((event.getAction() != Action.RIGHT_CLICK_BLOCK && !(event
				.getAction() == Action.LEFT_CLICK_BLOCK && event
				.getClickedBlock().getType() == Material.WOODEN_DOOR))
				|| event.isCancelled()) {
			return;
		}
		int id;
		ProtectedBlock block;
		if ((id = plugin.protectFile.getProtection(event.getClickedBlock()
				.getLocation())) != -1) {
			block = plugin.protectFile.getProtection(id);
			User user = User.getUser(plugin, event.getPlayer());
			if (!block.canUse(user) && !user.hasPerm("bencmd.lock.peek")) {
				event.setCancelled(true);
				user.sendMessage(ChatColor.RED
						+ "That block is locked! Use /protect info for more information...");
			} else {
				if (!user.getName()
						.equalsIgnoreCase(block.getOwner().getName())) {
					plugin.log.info(user.getDisplayName() + " has accessed "
							+ block.getOwner().getName()
							+ "'s protected block. (" + block.GetId() + ")");
					plugin.bLog.info(user.getDisplayName() + " has accessed "
							+ block.getOwner().getName()
							+ "'s protected block. (" + block.GetId() + ")");
				}
			}
		}
	}
}