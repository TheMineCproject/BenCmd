package ben_dude56.plugins.bencmd.advanced.npc;

import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

import ben_dude56.plugins.bencmd.BenCmd;

public class NPC {
	protected EntityNPC enpc;
	private int id;
	private Location l;
	protected BenCmd plugin;
	private String n;

	public NPC(BenCmd instance, String name, int id, Location l) {
		plugin = instance;
		this.id = id;
		this.l = l;
		this.n = name;
		if (l.getWorld().isChunkLoaded(l.getBlock().getChunk())) {
			spawn();
		}
	}

	public void spawn() {
		if (enpc == null) {
			WorldServer ws = ((CraftWorld) l.getWorld()).getHandle();
			MinecraftServer ms = ((CraftServer) ws.getServer()).getServer();
			enpc = new EntityNPC(ms, ws, n, new ItemInWorldManager(ws));
			enpc.setLocation(l.getX(), l.getY(), l.getZ(), l.getYaw(),
					l.getPitch());
			ws.addEntity(enpc);
			ws.players.remove(enpc);
		}
	}

	public void despawn() {
		if (enpc != null) {
			WorldServer ws = ((CraftWorld) l.getWorld()).getHandle();
			enpc.die();
			ws.removeEntity(enpc);
			enpc = null;
		}
	}

	public String getValue() {
		throw new UnsupportedOperationException("getValue() not overridden!");
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return enpc.name;
	}

	public Location getCurrentLocation() {
		return new Location(enpc.world.getWorld(), enpc.locX, enpc.locY,
				enpc.locZ, enpc.yaw, enpc.pitch);
	}

	public Location getLocation() {
		return l;
	}
}