package io.flixion.races.gui;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.flixion.races.Races;
import io.flixion.races.Util;
import io.flixion.races.data.Race;

public class RaceGUI {
	private static ItemStack[] contents = new ItemStack[35];
	
	public RaceGUI() {
		if (Races.getRaceHandler().getActiveRaceMap().size() > 0) {
			int index = 0;
			for (Map.Entry<UUID, Race> entry : Races.getRaceHandler().getActiveRaceMap().entrySet()) {
				if (entry.getValue().isParent()) {
					contents[index] = entry.getValue().getRaceIcon(false, null);
					index++;
				}
			}
		}
	}
	
	public static Inventory getMainRaceGUI(UUID u) {
		Inventory raceInventory;
		if (Races.getRaceHandler().getPlayerProfiles().get(u).getActiveRace() != null) {
			raceInventory = Bukkit.createInventory(null, 36, "Race Menu | [Equipped: " + Util.stripColor(Races.getRaceHandler().getPlayerProfiles().get(u).getActiveRace().getRaceName() + "]"));
		} else {
			raceInventory = Bukkit.createInventory(null, 36, "Race Menu | [Equipped: None]");
		}
		raceInventory.setContents(contents);
		return raceInventory;
	}
}
