package ben_dude56.plugins.bencmd.warps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.util.FileUtil;

import ben_dude56.plugins.bencmd.*;

public class WarpList {
	BenCmd plugin;
	HashMap<String, Warp> warps = new HashMap<String, Warp>();
	List<String> warpString = new ArrayList<String>();

	public WarpList(BenCmd instance) {
		plugin = instance;
		if (new File("plugins/BenCmd/_warps.db").exists()) {
			plugin.log.warning("Warp backup file found... Restoring...");
			if (FileUtil.copy(new File("plugins/BenCmd/_warps.db"), new File(
					"plugins/BenCmd/warps.db"))) {
				new File("plugins/BenCmd/_warps.db").delete();
				plugin.log.info("Restoration suceeded!");
			} else {
				plugin.log.warning("Failed to restore from backup!");
			}
		}
		LoadWarps();
	}

	public boolean updateWarp(Warp warp) {
		int ind = this.getIndex(warp);
		String name = warp.warpName;
		double x = warp.loc.getX();
		double y = warp.loc.getY();
		double z = warp.loc.getZ();
		Double yaw = (double) warp.loc.getYaw();
		Double pitch = (double) warp.loc.getPitch();
		String world = warp.loc.getWorld().getName();
		String group = warp.mustInheritGroup;
		String value = name + ":" + x + "," + y + "," + z + ","
				+ yaw.toString() + "," + pitch.toString() + ":" + world + ":"
				+ group;
		if (ind == -1) {
			warpString.add(value);
		} else {
			warpString.add(ind, value);
		}
		try {
			new File("plugins/BenCmd/_warps.db").createNewFile();
			if (!FileUtil.copy(new File("plugins/BenCmd/warps.db"), new File(
					"plugins/BenCmd/_warps.db"))) {
				plugin.log.warning("Failed to back up warp database!");
			}
		} catch (IOException e) {
			plugin.log.warning("Failed to back up warp database!");
		}
		SaveFile();
		try {
			new File("plugins/BenCmd/_warps.db").delete();
		} catch (Exception e) { }
		return true;
	}

	public boolean remWarp(String name) {
		int ind = this.getIndex(name);
		if (ind == -1) {
			return false;
		}
		warpString.remove(ind);
		try {
			new File("plugins/BenCmd/_warps.db").createNewFile();
			if (!FileUtil.copy(new File("plugins/BenCmd/warps.db"), new File(
					"plugins/BenCmd/_warps.db"))) {
				plugin.log.warning("Failed to back up warp database!");
			}
		} catch (IOException e) {
			plugin.log.warning("Failed to back up warp database!");
		}
		SaveFile();
		try {
			new File("plugins/BenCmd/_warps.db").delete();
		} catch (Exception e) { }
		return true;
	}

	public boolean LoadWarps() {
		warpString.clear();
		warps.clear();
		File warpFile;
		warpFile = new File(plugin.propDir + "warps.db");
		String str = "";
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					warpFile)));
		} catch (FileNotFoundException e) {
			plugin.log.warning("warps.db not found. Attempting to create...");
			plugin.bLog.warning("warps.db not found. Attempting to create...");
			try {
				warpFile.createNewFile();
				br = new BufferedReader(new InputStreamReader(
						new FileInputStream(warpFile)));
			} catch (IOException ex) {
				plugin.bLog.log(Level.SEVERE, "Couldn't create warps.db:", e);
				plugin.log.severe("Couldn't create warps.db:");
				ex.printStackTrace();
				return false;
			}
		}
		try {
			while ((str = br.readLine()) != null) {
				try {
					warpString.add(str);
					String name = str.split(":")[0];
					double x = Double
							.parseDouble(str.split(":")[1].split(",")[0]);
					double y = Double
							.parseDouble(str.split(":")[1].split(",")[1]);
					double z = Double
							.parseDouble(str.split(":")[1].split(",")[2]);
					double yaw;
					double pitch;
					try {
						yaw = Double
								.parseDouble(str.split(":")[1].split(",")[3]);
						pitch = Double
								.parseDouble(str.split(":")[1].split(",")[4]);
					} catch (IndexOutOfBoundsException e) {
						yaw = 0;
						pitch = 0;
					}
					String world = str.split(":")[2];
					String group = "";
					if (str.split(":").length == 4) {
						group = str.split(":")[3];
					}
					warps.put(name, new Warp(x, y, z, yaw, pitch, world, name,
							group, plugin));
				} catch (IndexOutOfBoundsException e) {
					plugin.bLog.log(Level.SEVERE,
							"Couldn't load one of the warps!", e);
					plugin.log.warning("Couldn't load one of the warps!");
					e.printStackTrace();
				} catch (NumberFormatException e) {
					plugin.bLog.log(Level.SEVERE,
							"Couldn't load one of the warps!", e);
					plugin.log.warning("Couldn't load one of the warps!");
					e.printStackTrace();
				}
			}
			br.close();
		} catch (IOException e) {
			plugin.bLog.log(Level.SEVERE, "Couldn't read warps.db:", e);
			plugin.log.severe("Couldn't read warps.db:");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public int getIndex(Warp warp) {
		for (int i = 0; i < warpString.size(); i++) {
			String value = warpString.get(i);
			if (value.split(":")[0].equals(warp.warpName)) {
				return i;
			}
		}
		return -1;
	}

	public int getIndex(String name) {
		for (int i = 0; i < warpString.size(); i++) {
			String value = warpString.get(i);
			if (value.split(":")[0].equals(name)) {
				return i;
			}
		}
		return -1;
	}

	public boolean SaveFile() {
		File warpFile;
		warpFile = new File(plugin.propDir + "warps.db");
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(warpFile));
		} catch (IOException e) {
			plugin.bLog.log(Level.SEVERE,
					"Unable to open warps.db for writing:", e);
			plugin.log.severe("Unable to open warps.db for writing:");
			e.printStackTrace();
			return false;
		}
		for (String value : warpString) {
			try {
				bw.write(value);
				if (!warpString.get(warpString.size() - 1).equals(value)) {
					bw.newLine();
				}
			} catch (IOException e) {
				plugin.bLog.log(Level.SEVERE, "BenCmd failed to save warp "
						+ value.split(":")[0] + ":", e);
				plugin.log.severe("BenCmd failed to save warp "
						+ value.split(":")[0] + ":");
				e.printStackTrace();
			}
		}
		try {
			bw.close();
		} catch (IOException e) {
			plugin.bLog.log(Level.SEVERE, "Failed to save warps:", e);
			plugin.log.severe("Failed to save warps:");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public Warp getWarp(String name) {
		if (!warps.containsKey(name)) {
			return null;
		}
		return warps.get(name);
	}

	public boolean addWarp(double x, double y, double z, double yaw,
			double pitch, String world, String name, String group) {
		Warp warp;
		try {
			warp = new Warp(x, y, z, yaw, pitch, world, name, group, plugin);
			warps.put(name, warp);
		} catch (Exception e) {
			plugin.bLog.log(Level.SEVERE, "Couldn't add new warp:", e);
			plugin.log.severe("Couldn't add new warp:");
			e.printStackTrace();
			return false;
		}
		return updateWarp(warp);
	}

	public boolean removeWarp(String name) {
		if (warps.containsKey(name)) {
			warps.remove(name);
			return remWarp(name);
		} else {
			return false;
		}
	}

	public List<Warp> listWarps(Player player) {
		List<Warp> list = new ArrayList<Warp>();
		for (Warp warp : warps.values()) {
			if (warp.canWarpHere(new WarpableUser(plugin, player))) {
				list.add(warp);
			}
		}
		return list;
	}

	public List<Warp> listAllWarps() {
		return (List<Warp>) warps.values();
	}

}
