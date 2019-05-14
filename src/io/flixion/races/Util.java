package io.flixion.races;

import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.flixion.races.ability.AbilityType;

public class Util {
	private static Random random = new Random();
	
	public static UUID genUUID(String s) {
		return UUID.nameUUIDFromBytes(s.toLowerCase().getBytes());
	}
	
	public static String addColor(String m) {
		return ChatColor.translateAlternateColorCodes('&', m);
	}
	
	public static String sendPluginMessage(String m) {
		return ChatColor.translateAlternateColorCodes('&', "[DMCRaces] " + m);
	}
	
	public static String stripColor(String m) {
		return ChatColor.stripColor(m);
	}
	
	public static int generateRandomInt(int upperBound, int lowerBound) {
		return random.nextInt(upperBound - lowerBound + 1) + lowerBound;
	}
	
	public static boolean generateRandomBoolean() {
		return random.nextBoolean();
	}
	
	public static boolean isBlockAbovePlayer(Location loc) {
		for (int i = loc.getBlockY(); i < 256; i++) {
			if (loc.getBlock().getType() != Material.AIR) {
				return true;
			}
			loc.setY(i);
		}
		return false;
	}
	
	public static boolean compareItemStack(ItemStack i, ItemStack k, boolean matchEnumOnly) {
		if (k == null) {
			return false;
		}
		if (matchEnumOnly) {
			if (i.getType() == k.getType()) {
				return true;
			}
		}
		else {
			if (i.hasItemMeta() && k.hasItemMeta()) {
				if (i.getItemMeta().hasDisplayName() && k.getItemMeta().hasDisplayName()) {
					if (stripColor(i.getItemMeta().getDisplayName()).equals(stripColor(k.getItemMeta().getDisplayName()))) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static String formatEnumString(AbilityType a) {
		return WordUtils.capitalize(a.toString().replaceAll("_", " "));
	}
	
	public static CardinalDirection returnNearestCardinalDirection(float yaw) { //8 points
		if (yaw >= 0 && yaw < 22.5) {
			return CardinalDirection.SOUTH;
		} else if (yaw >= 22.5 && yaw < 67.5) {
			return CardinalDirection.SOUTH_WEST;
		} else if (yaw >= 67.5 && yaw < 112.5) {
			return CardinalDirection.WEST;
		} else if (yaw >= 112.5 && yaw < 157.5) {
			return CardinalDirection.NORTH_WEST;
		} else if (yaw >= 157.5 && yaw <= 180) {
			return CardinalDirection.NORTH;
		} else if (yaw > -180 && yaw < -157.5) {
			return CardinalDirection.NORTH;
		} else if (yaw >= -157.5 && yaw < -112.5) {
			return CardinalDirection.NORTH_EAST;
		} else if (yaw >= -112.5 && yaw < -67.5) {
			return CardinalDirection.EAST;
		} else if (yaw >= -67.5 && yaw < -22.5) {
			return CardinalDirection.SOUTH_EAST;
		} else if (yaw >= -22.5 && yaw < 0) {
			return CardinalDirection.SOUTH;
		} else {
			return CardinalDirection.NORTH;
		}
	}
	
	public enum CardinalDirection {
		NORTH,
		SOUTH,
		EAST,
		WEST,
		NORTH_EAST,
		NORTH_WEST,
		SOUTH_EAST,
		SOUTH_WEST
	}
}
