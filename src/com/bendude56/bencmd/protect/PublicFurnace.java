package com.bendude56.bencmd.protect;

import java.util.List;

import org.bukkit.Location;

public class PublicFurnace extends PublicBlock {
	public PublicFurnace(int id, String owner, List<String> guests, Location loc) {
		super(id, owner, guests, loc);
	}
}
