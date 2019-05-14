package io.flixion.races.ability;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class Disperse extends Ability {

	public Disperse(int cooldownTime, int effectTime) {
		super(cooldownTime, effectTime);
	}

	@EventHandler
	public void cancelDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (getActivePlayers().contains(((Player) e.getEntity()).getUniqueId())) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void cancelDoingDamage(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player) {
			if (getActivePlayers().contains(((Player) e.getDamager()).getUniqueId())) {
				e.setCancelled(true);
			}
		}
	}

	@Override
	public void perform(Player p) {
		eventAbilityPerform(p);
	}
}
