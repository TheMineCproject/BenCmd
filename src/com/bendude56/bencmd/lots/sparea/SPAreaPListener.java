package com.bendude56.bencmd.lots.sparea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.bendude56.bencmd.BenCmd;
import com.bendude56.bencmd.advanced.npc.NPC;
import com.bendude56.bencmd.money.Currency;


public class SPAreaPListener extends PlayerListener {
	BenCmd plugin;
	HashMap<Player, List<SPArea>> areas;
	List<Player> ignore;
	HashMap<Player, List<NPC>> sent;

	public SPAreaPListener(BenCmd instance) {
		plugin = instance;
		areas = new HashMap<Player, List<SPArea>>();
		ignore = new ArrayList<Player>();
		sent = new HashMap<Player, List<NPC>>();
	}

	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (plugin.returns.containsKey(event.getPlayer())) {
			for (ItemStack i : plugin.returns.get(event.getPlayer())) {
				event.getPlayer().getInventory().addItem(i);
			}
			plugin.returns.remove(event.getPlayer());
		}
	}
	
	public void sendSkins(Player p, Location l) {
		if (plugin.spoutcraft) {
			if(!sent.containsKey(p)) {
				sent.put(p, new ArrayList<NPC>());
			}
			for (NPC n : BenCmd.getPlugin().npcs.allNPCs()) {
				if (n.isSpawned() && l.getWorld().equals(n.getCurrentLocation().getWorld()) && l.distance(n.getCurrentLocation()) < 50) {
					if (!sent.get(p).contains(n)) {
						sent.get(p).add(n);
						plugin.spoutconnect.sendSkin(p, n.getEntityId(), n.getSkinURL());
					}
				} else {
					if (sent.get(p).contains(n)) {
						sent.get(p).remove(n);
						plugin.spoutconnect.sendSkin(p, n.getEntityId(), "http://s3.amazonaws.com/MinecraftSkins/" + n.getName() + ".png");
					}
				}
			}
		}
	}

	public void onPlayerMove(PlayerMoveEvent event) {
		sendSkins(event.getPlayer(), event.getTo());
		Player p = event.getPlayer();
		if (ignore.contains(p)) {
			event.setCancelled(true);
			return;
		}
		if (!areas.containsKey(p)) {
			areas.put(p, new ArrayList<SPArea>());
		}
		for (SPArea a : plugin.spafile.listAreas()) {
			if (a instanceof PVPArea && a.insideArea(p.getLocation())) {
				int money = 0;
				for (Currency c : plugin.prices.getCurrencies()) {
					for (ItemStack i : p.getInventory().all(c.getMaterial())
							.values()) {
						if (i.getDurability() == c.getDurability()) {
							money += Math.floor(i.getAmount() * c.getPrice());
						}
					}
				}
				if (money < ((PVPArea) a).getMinimumCurrency()) {
					ignore.add(p);
					p.sendMessage(ChatColor.RED + "You must have "
							+ ((PVPArea) a).getMinimumCurrency()
							+ " worth of currency to PVP in this area...");
					int c = 1;
					while (true) {
						Location f = p.getLocation();
						f.setX(f.getX() + c);
						if (!a.insideArea(f)
								&& f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setX(f.getX() - (c * 2));
						if (!a.insideArea(f)
								&& f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setX(f.getX() + c);
						f.setZ(f.getZ() + c);
						if (!a.insideArea(f)
								&& f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setZ(f.getZ() - (c * 2));
						if (!a.insideArea(f)
								&& f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setZ(f.getZ() + c);
						f.setY(f.getY() + c);
						if (!a.insideArea(f)
								&& f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setY(f.getY() - (c * 2));
						if (!a.insideArea(f)
								&& f.getBlock().getType() == Material.AIR) {
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
					p.sendMessage(ChatColor.RED
							+ "You cannot enter this area at this time!");
					int c = 1;
					while (true) {
						Location f = p.getLocation();
						f.setX(f.getX() + c);
						if (!a.insideArea(f)
								&& f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setX(f.getX() - (c * 2));
						if (!a.insideArea(f)
								&& f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setX(f.getX() + c);
						f.setZ(f.getZ() + c);
						if (!a.insideArea(f)
								&& f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setZ(f.getZ() - (c * 2));
						if (!a.insideArea(f)
								&& f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setZ(f.getZ() + c);
						f.setY(f.getY() + c);
						if (!a.insideArea(f)
								&& f.getBlock().getType() == Material.AIR) {
							event.setTo(f);
							event.setCancelled(false);
							ignore.remove(p);
							return;
						}
						f.setY(f.getY() - (c * 2));
						if (!a.insideArea(f)
								&& f.getBlock().getType() == Material.AIR) {
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
						if (((MsgArea) a).getEnterMessage().startsWith("§")
								&& ((MsgArea) a).getEnterMessage().length() == 2) {
							return;
						}
						p.sendMessage(((MsgArea) a).getEnterMessage());
						areas.get(p).add(a);
					}
				} else {
					if (areas.get(p).contains(a)) {
						if (((MsgArea) a).getLeaveMessage().startsWith("§")
								&& ((MsgArea) a).getLeaveMessage().length() == 2) {
							return;
						}
						p.sendMessage(((MsgArea) a).getLeaveMessage());
						areas.get(p).remove(a);
					}
				}
			}
		}
	}
}