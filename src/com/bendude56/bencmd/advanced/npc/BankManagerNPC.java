package com.bendude56.bencmd.advanced.npc;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

import com.bendude56.bencmd.BenCmd;
import com.bendude56.bencmd.User;
import com.bendude56.bencmd.advanced.bank.BankInventory;
import com.bendude56.bencmd.money.BuyableItem;

public class BankManagerNPC extends NPC implements Clickable {

	public BankManagerNPC(int id, Location l) {
		super("Bank Manager", id, l, new ItemStack(Material.BOOK));
	}

	@Override
	public String getSkinURL() {
		return "http://s3.amazonaws.com/squirt/i4e4d4fbbb8f288300604535471306130503032.png";
	}

	@Override
	public void onRightClick(Player p) {
		User u = User.getUser(p);
		if (User.getUser(p).hasPerm("bencmd.bank.admin")) {
			BenCmd.getLocale().sendMessage(u, "npc.bank.adminNoUse");
			return;
		}
		if (!BenCmd.getBankController().hasBank(p.getName())) {
			BenCmd.getBankController().addBank(new BankInventory(p.getName()));
		}
		if (BenCmd.getBankController().getBank(p.getName()).isUpgraded()) {
			BenCmd.getLocale().sendMessage(u, "command.bank.selfAlreadyUpgraded");
		} else {
			if (BuyableItem.hasMoney(User.getUser(p), BenCmd.getMainProperties().getDouble("bankUpgradeCost", 4096), new ArrayList<Material>())) {
				BuyableItem.remMoney(User.getUser(p), BenCmd.getMainProperties().getDouble("bankUpgradeCost", 4096), new ArrayList<Material>());
				BenCmd.getBankController().upgradeBank(p.getName());
				BenCmd.log(BenCmd.getLocale().getString("log.bank.upgradeSelf", p.getName()));
				BenCmd.getLocale().sendMessage(u, "command.bank.selfUpgrade");
			} else {
				BenCmd.getLocale().sendMessage(u, "basic.insufficientMoney", BenCmd.getMainProperties().getDouble("bankUpgradeCost", 4096) + "");
			}
		}
	}

	@Override
	public void onLeftClick(Player p) {

	}

	@Override
	public String getValue() {
		Location l = super.getLocation();
		return "m|" + l.getWorld().getName() + "," + l.getX() + "," + l.getY() + "," + l.getZ() + "," + l.getYaw() + "," + l.getPitch();
	}

}
