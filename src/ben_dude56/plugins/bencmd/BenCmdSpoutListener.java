package ben_dude56.plugins.bencmd;

import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
import org.getspout.spoutapi.event.spout.SpoutListener;
import org.getspout.spoutapi.packet.PacketSkinURL;
import org.getspout.spoutapi.player.SpoutPlayer;

import ben_dude56.plugins.bencmd.advanced.npc.NPC;

public class BenCmdSpoutListener extends SpoutListener {
	@Override
	public void onSpoutCraftEnable(SpoutCraftEnableEvent event) {
		SpoutPlayer p = (SpoutPlayer) event.getPlayer();
		if (p.getVersion() > 4) {
			for (NPC n : BenCmd.getPlugin().npcs.allNPCs()) {
				if (n.isSpawned()) {
					p.sendPacket(new PacketSkinURL(n.getEntityId(), n.getSkinURL()));
				}
			}
		}
	}
}