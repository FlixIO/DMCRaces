package io.flixion.races.handler;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.flixion.races.Races;
import io.flixion.races.Util;
import io.flixion.races.gui.RaceGUI;
import io.flixion.races.persist.PersistRace;

public class CommandHandler implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("race")) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					p.openInventory(RaceGUI.getMainRaceGUI(p.getUniqueId()));
				}
			} else if (args.length >= 2) {
				if (args[0].equalsIgnoreCase("create")) {
					StringBuilder s = new StringBuilder();
					for (int i = 1; i < args.length; i++) {
						s.append(args[i] + " ");
					}
					if (!Races.getRaceHandler().getActiveRaceMap().containsKey(Util.genUUID(s.toString().trim()))) {
						Bukkit.getScheduler().runTaskAsynchronously(Races.getPL(), new PersistRace(s.toString().trim(), sender));
					} else {
						sender.sendMessage(Util.sendPluginMessage("A race with this name already exists!"));
					}
				}
			}
		}
		return true;
	}
}
