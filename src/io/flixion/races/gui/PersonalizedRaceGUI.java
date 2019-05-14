package io.flixion.races.gui;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.flixion.races.Races;
import io.flixion.races.Util;
import io.flixion.races.data.Race;

public class PersonalizedRaceGUI {
	private Inventory inventory;
	private ItemStack backButton;
	
	public PersonalizedRaceGUI(Race r, UUID u) {
		Race playerRace = Races.getRaceHandler().getPlayerProfiles().get(u).getActiveRace();
		if (playerRace != null) {
			inventory = Bukkit.createInventory(null, 36, "Race Info | [Equipped: " + Util.stripColor(playerRace.getRaceName()) + "]");
		} else {
			inventory = Bukkit.createInventory(null, 36, "Race Info | [Equipped: None]");
		}
		for(Map.Entry<Integer, Race> entry : r.getChildRaces().entrySet()) {
			inventory.addItem(entry.getValue().getRaceIcon(true, u));
		}
		backButton = new ItemStack(Material.COMPASS);
		ItemMeta meta = backButton.getItemMeta();
		meta.setDisplayName(Util.addColor("&eClick to go back"));
		backButton.setItemMeta(meta);
		inventory.setItem(35, backButton);
	}
	
	public Inventory get() {
		return inventory;
	}
}
