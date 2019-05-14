package io.flixion.races.ability;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import io.flixion.races.Util;
import io.flixion.races.Util.CardinalDirection;

public class Traveller extends Ability {
	private int teleportDistance;

	public Traveller(int cooldownTime, int effectTime, int teleportDistance) {
		super(cooldownTime, effectTime);
		this.teleportDistance = teleportDistance;
	}

	public boolean verifyTeleport(CardinalDirection direction, Player p) {
		switch (direction) {
		case EAST:
			return blockIteration(p, 1, 0);
		case NORTH:
			return blockIteration(p, 0, -1);
		case NORTH_EAST:
			return blockIteration(p, 1, -1);
		case NORTH_WEST:
			return blockIteration(p, -1, -1);
		case SOUTH:
			return blockIteration(p, 0, 1);
		case SOUTH_EAST:
			return blockIteration(p, 1, 1);
		case SOUTH_WEST:
			return blockIteration(p, -1, 1);
		case WEST:
			return blockIteration(p, -1, 0);
		}
		return false;
	}
	
	private boolean blockIteration(Player p, int xOffset, int zOffset) {
		Location loc = p.getEyeLocation();
		for (int i = 1; i < teleportDistance + 1; i++) {
			if (loc.getWorld().getBlockAt(loc.getBlockX() + xOffset, loc.getBlockY(), loc.getBlockZ() + zOffset).getType() == Material.BARRIER) {
				return false;
			}
		}
		p.teleport(loc.add(xOffset * teleportDistance, 0, zOffset * teleportDistance));
		return true;
	}

	@Override
	public void perform(Player caster) {
		Bukkit.broadcastMessage(caster.getEyeLocation().getYaw() + "");
		Bukkit.broadcastMessage(Util.returnNearestCardinalDirection(caster.getEyeLocation().getYaw()) + "");
		if (!verifyTeleport(Util.returnNearestCardinalDirection(caster.getEyeLocation().getYaw()), caster)) {
			caster.sendMessage(Util.addColor("There is a barrier block in the direction you are trying to teleport! Teleportation failed"));
		}
	}
}
