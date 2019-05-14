package io.flixion.races.ability;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import io.flixion.races.Races;

public abstract class Ability implements Listener {
	private int cooldownTime;
	private int effectTime;
	protected Set<UUID> activePlayers = new HashSet<>();
	
	public Ability(int cooldownTime, int effectTime) {
		this.cooldownTime = cooldownTime;
		this.effectTime = effectTime;
		this.effectTime *= 20;
	}
	
	public void eventAbilityPerform(Player p) {
		activePlayers.add(p.getUniqueId());
		Bukkit.getScheduler().runTaskLater(Races.getPL(), new Runnable() {
			
			@Override
			public void run() {
				if (activePlayers.contains(p.getUniqueId())) {
					activePlayers.remove(p.getUniqueId());
				}
			}
		}, effectTime);
	}
	
	public abstract void perform(Player p);
	
	@EventHandler
	public void suspendOnQuit(PlayerQuitEvent e) {
		if (activePlayers.contains(e.getPlayer().getUniqueId())) {
			activePlayers.remove(e.getPlayer().getUniqueId());
		}
	}
	
	public Set<UUID> getActivePlayers() {
		return activePlayers;
	}

	public int getCooldownTime() {
		return cooldownTime;
	}

	public void setCooldownTime(int cooldownTime) {
		this.cooldownTime = cooldownTime;
	}

	public int getEffectTime() {
		return effectTime;
	}
}
