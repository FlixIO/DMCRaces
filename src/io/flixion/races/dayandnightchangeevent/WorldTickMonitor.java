package io.flixion.races.dayandnightchangeevent;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import io.flixion.races.Races;

public class WorldTickMonitor implements Listener {
	Map<World, WorldTime> affectedWorlds = new HashMap<>();
	
	public WorldTickMonitor() {
		for (World w : Bukkit.getWorlds()) {
			if (w.getEnvironment() == Environment.NORMAL) {
				w.setFullTime(0L);
				affectedWorlds.put(w, WorldTime.DAY);
				Bukkit.getScheduler().runTaskTimerAsynchronously(Races.getPL(), new Runnable() {
					
					@Override
					public void run() {
						for (Map.Entry<World, WorldTime> entry : affectedWorlds.entrySet()) {
							Bukkit.getScheduler().runTaskLater(Races.getPL(), new Runnable() {
								
								@Override
								public void run() {
									if (entry.getKey() != null) {
										Bukkit.getPluginManager().callEvent(new DayAndNightChangeEvent(entry.getKey(), WorldTime.DAY));
									}
								}
							}, 1000);
							Bukkit.getScheduler().runTaskLater(Races.getPL(), new Runnable() {
								
								@Override
								public void run() {
									if (entry.getKey() != null) {
										Bukkit.getPluginManager().callEvent(new DayAndNightChangeEvent(entry.getKey(), WorldTime.NIGHT));
									}
								}
							}, 13000);
						}
					}
				}, 24000, 24000);
			}
		}
	}
	
	@EventHandler
	public void monitorNewWorld(WorldLoadEvent e) {
		if (e.getWorld().getEnvironment() == Environment.NORMAL) {
			affectedWorlds.put(e.getWorld(), WorldTime.DAY);
			e.getWorld().setFullTime(0L);
		}
	}
	
	@EventHandler
	public void monitorUnload(WorldUnloadEvent e) {
		if (e.getWorld().getEnvironment() == Environment.NORMAL) {
			affectedWorlds.remove(e.getWorld());
		}
	}
	
	public Map<World, WorldTime> getWorldTimeMap() {
		return affectedWorlds;
	}
}
