package io.flixion.races.ability;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.flixion.races.Races;
import io.flixion.races.Util;
import io.flixion.races.dayandnightchangeevent.DayAndNightChangeEvent;
import io.flixion.races.dayandnightchangeevent.WorldTickMonitor;
import io.flixion.races.dayandnightchangeevent.WorldTime;

public class Shadowborn extends Ability {
	private PotionEffect invisibility;
	private PotionEffect blindness;
	private WorldTickMonitor worldTickMonitor;

	public Shadowborn(int cooldownTime, int effectTime, WorldTickMonitor worldTickMonitor) {
		super(cooldownTime, effectTime);
		this.worldTickMonitor = worldTickMonitor;
		invisibility = new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 2, false, false);
		blindness = new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 1, false, false);
		monitorWalking();
	}
	
	@Override
	public void perform(Player caster) {
		if (activePlayers.contains(caster.getUniqueId())) { //toggle
			activePlayers.remove(caster.getUniqueId());
			caster.removePotionEffect(PotionEffectType.INVISIBILITY);
			caster.removePotionEffect(PotionEffectType.BLINDNESS);
		} else {
			activePlayers.add(caster.getUniqueId());
		}
	}
	
	@EventHandler
	public void detectTime(DayAndNightChangeEvent e) {
		if (e.getWorldTimeType() == WorldTime.NIGHT) {
			for (UUID u : activePlayers) {
				Player p = Bukkit.getPlayer(u);
				p.addPotionEffect(invisibility);
				p.addPotionEffect(blindness);
			}
		} else {
			for (UUID u : activePlayers) {
				Player p = Bukkit.getPlayer(u);
				p.removePotionEffect(PotionEffectType.INVISIBILITY);
				p.removePotionEffect(PotionEffectType.BLINDNESS);
			}
		}
	}
	
	public void monitorWalking() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(Races.getPL(), new Runnable() {
			@Override
			public void run() {
				for (UUID u : getActivePlayers()) {
					if (Bukkit.getPlayer(u) != null) {
						Player p = Bukkit.getPlayer(u);
						if (worldTickMonitor.getWorldTimeMap().get(p.getWorld()) == WorldTime.DAY){
							if (Util.isBlockAbovePlayer(p.getEyeLocation())) {
								addEffectSync(p);
							} else {
								removeEffectSync(p);
							}
						}
					}
				}
			}
		}, 20, 20);
	}
	
	private void removeEffectSync(Player p) {
		Bukkit.getScheduler().runTask(Races.getPL(), new Runnable() {
			
			@Override
			public void run() {
				p.removePotionEffect(PotionEffectType.BLINDNESS);
				p.removePotionEffect(PotionEffectType.INVISIBILITY);
			}
		});
	}

	private void addEffectSync(Player p) {
		Bukkit.getScheduler().runTask(Races.getPL(), new Runnable() {
			
			@Override
			public void run() {
				p.addPotionEffect(invisibility);
				p.addPotionEffect(blindness);
			}
		});
	}
}
