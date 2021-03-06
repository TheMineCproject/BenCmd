package com.bendude56.bencmd.multiworld;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import com.bendude56.bencmd.BenCmd;

public class BenCmdWorld {
	
	private World world;
	private long dangerTime;
	private boolean allowSpawnPassive;
	private boolean allowSpawnNeutral;
	private boolean allowSpawnAggressive;

	public BenCmdWorld(World world, boolean passive, boolean neutral, boolean aggro) {
		this.world = world;
		this.dangerTime = 0;
		this.allowSpawnPassive = passive;
		this.allowSpawnNeutral = neutral;
		this.allowSpawnAggressive = aggro;
	}
	
	public void reset() throws IOException {
		deleteBukkit();
		WorldCreator w = new WorldCreator(world.getName());
		w.seed(world.getSeed());
		world = w.createWorld();
		BenCmd.getWorlds().updateWorldEntry(this, true);
		BenCmd.log("World '" + world.getName() + "' has been reset...");
	}
	
	public void remove() {
		unload();
		BenCmd.getWorlds().removeWorldEntry(this);
		BenCmd.log("World '" + world.getName() + "' has been removed...");
	}
	
	public void delete() throws IOException {
		deleteBukkit();
		BenCmd.getWorlds().removeWorldEntry(this);
		BenCmd.log("World '" + world.getName() + "' has been deleted...");
	}
	
	private void unload() {
		while (world.getPlayers().size() > 0) {
			world.getPlayers().get(0).sendMessage(ChatColor.RED + "The world you were in has been deleted!");
			world.getPlayers().get(0).sendMessage(ChatColor.RED + "You have been teleported back to the main world.");
			world.getPlayers().get(0).teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
		}
		Bukkit.unloadWorld(world, true);
	}
	
	private void deleteBukkit() throws IOException {
		unload();
		if (!deleteFolder(new File(world.getName()))) {
			throw new IOException("Failed to delete folder!");
		}
	}
	
	private static boolean deleteFolder(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    if (!deleteFolder(f)) {
                        return false;
                    }
                }
            }
            file.delete();
            return !file.exists();
        } else {
            return false;
        }
    }

	public String getName() {
		return world.getName();
	}

	public long getSeed() {
		return world.getSeed();
	}

	public long getDangerTime() {
		return dangerTime;
	}

	public void setDangerTime(long dangerTime) {
		this.dangerTime = dangerTime;
	}

	public boolean getAllowSpawnPassive() {
		return allowSpawnPassive;
	}

	public void setAllowSpawnPassive(boolean allowSpawnPassive) {
		this.allowSpawnPassive = allowSpawnPassive;
	}

	public boolean getAllowSpawnNeutral() {
		return allowSpawnNeutral;
	}

	public void setAllowSpawnNeutral(boolean allowSpawnNeutral) {
		this.allowSpawnNeutral = allowSpawnNeutral;
	}

	public boolean getAllowSpawnAggressive() {
		return allowSpawnAggressive;
	}

	public void setAllowSpawnAggressive(boolean allowSpawnAggressive) {
		this.allowSpawnAggressive = allowSpawnAggressive;
	}

}
