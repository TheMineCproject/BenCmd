package ben_dude56.plugins.bencmd.nofly;

import java.util.HashMap;
import java.util.Timer;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import ben_dude56.plugins.bencmd.BenCmd;
import ben_dude56.plugins.bencmd.User;

public class FlyDetect {
	public BenCmd plugin;
	public Timer flyTime;
	private HashMap<Player, Integer> players;
	private HashMap<String, Integer> offenders;
	private final Logger log = Logger.getLogger("minecraft");
	
	public FlyDetect(BenCmd instance) {
		plugin = instance;
		flyTime = new Timer();
		flyTime.schedule(new FlyTimer(this), 0, 2000);
		players = new HashMap<Player, Integer>();
		offenders = new HashMap<String, Integer>();
	}
	
	public boolean isDetected(Player player) {
		return players.containsKey(player);
	}
	
	public void addToList(Player player) {
		players.put(player, 0);
	}
	
	public int getOffences(Player player) {
		if(new User(plugin, player).hasPerm("isJailed" ,false)) {
			return 3;
		}
		if(offenders.containsKey(player.getName())) {
			return offenders.get(player.getName());
		} else {
			return 0;
		}
	}
	
	public void addOffence(Player player) {
		if(new User(plugin, player).hasPerm("isJailed" ,false)) {
			return;
		}
		if(offenders.containsKey(player.getName())) {
			offenders.put(player.getName(), getOffences(player) + 1);
		} else {
			offenders.put(player.getName(), 1);
		}
	}
	
	public void detect(Player player) {
		if(new User(plugin, player).hasPerm("canFly")) {
			return;
		}
		if(isDetected(player)) {
			Integer time = players.get(player);
			players.put(player, time + 1000);
			if(time == 3000) {
				switch(getOffences(player)) {
				case 0:
					player.sendMessage(ChatColor.RED + "Flying detected. Stop, or you will be respawned in 3...");
					break;
				case 1:
					player.sendMessage(ChatColor.RED + "Flying detected. Stop, or you will be kicked in 3...");
					break;
				case 2:
					player.sendMessage(ChatColor.RED + "Flying detected. Stop, or you will be jailed in 3...");
					break;
				default:
					player.sendMessage(ChatColor.RED + "Flying detected. Stop, or you will be kicked in 3...");
					break;
				}
				log.info("Possible flying from " + player.getName());
			} else if (time == 4000) {
				player.sendMessage(ChatColor.RED + "2...");
			} else if (time == 5000) {
				player.sendMessage(ChatColor.RED + "1...");
			} else if (time == 6000) {
				switch(getOffences(player)) {
				case 0:
					new User(plugin, player).Spawn();
					plugin.checkpoints.RemovePreWarp(player);
					break;
				case 1:
					new User(plugin, player).Kick("You were auto-detected flying!");
					break;
				case 2:
					if(!new User(plugin, player).hasPerm("isJailed", false)) {
						new User(plugin, player).toggleJail();
					}
					break;
				default:
					new User(plugin, player).Kick("You were auto-detected flying!");
					break;
				}
				log.warning("Action taken on " + player.getName() + " for flying!");
				undetect(player);
				addOffence(player);
			}
		} else {
			addToList(player);
		}
	}
	
	public void undetect(Player player) {
		if(isDetected(player)) {
			players.remove(player);
		}
	}
}
