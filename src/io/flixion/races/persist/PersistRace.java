package io.flixion.races.persist;

import java.io.File;

import org.bukkit.command.CommandSender;

import io.flixion.races.Races;

public class PersistRace implements Runnable{
	private String raceName;
	private CommandSender sender;
	
	public PersistRace(String raceName, CommandSender sender) {
		this.raceName = raceName;
		this.sender = sender;
	}
	
	@Override
	public void run() {
		sender.sendMessage("Attempting to save new race: " + raceName);
		Races.getPL().saveResource("race_template.yml", false);
		File template = new File(Races.getPL().getDataFolder(), "race_template.yml");
		File newFile = new File(Races.getPL().getDataFolder().getPath() + "/races/", raceName.toLowerCase() + ".yml");
		template.renameTo(newFile);
		sender.sendMessage("Successfully created new race: " + raceName);
	}

}
