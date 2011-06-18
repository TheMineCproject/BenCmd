package ben_dude56.plugins.bencmd.advanced;

import org.bukkit.craftbukkit.entity.CraftPlayer;

import net.minecraft.server.ContainerPlayer;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.InventoryPlayer;

public class ViewableInventory extends InventoryPlayer {
	
	public static void replInv(CraftPlayer player) {
		EntityPlayer entityplayer = player.getHandle();
		entityplayer.inventory = new ViewableInventory(entityplayer.inventory);
	    entityplayer.defaultContainer = new ContainerPlayer(entityplayer.inventory, !entityplayer.world.isStatic);
	    entityplayer.activeContainer = entityplayer.defaultContainer;
	    player.setHandle(entityplayer);
	}

	public ViewableInventory(InventoryPlayer inv) {
		super(inv.d);
		this.armor = inv.armor;
		this.items = inv.items;
		this.itemInHandIndex = inv.itemInHandIndex;
		this.e = inv.e;
		this.b(inv.j());
		inv.d.defaultContainer = new ContainerPlayer(this, !inv.d.world.isStatic);
		inv.d.activeContainer = inv.d.defaultContainer;
	}

	public String getName() {
		return ((EntityPlayer)this.d).name;
	}
	
	public boolean a_(EntityHuman entityHuman) {
		return true;
	}
}