package io.flixion.races.ability;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class Stoneform extends Ability implements Listener {
	private Set<DamageCause> vulnerableDamageCauses = new HashSet<>();
	
	public Stoneform (int cooldownTime, int effectTime) {
		super(cooldownTime, effectTime);
		vulnerableDamageCauses.add(DamageCause.BLOCK_EXPLOSION);
		vulnerableDamageCauses.add(DamageCause.ENTITY_EXPLOSION);
		vulnerableDamageCauses.add(DamageCause.FALL);
	}
	
	@EventHandler
	public void cancelDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (getActivePlayers().contains(((Player) e.getEntity()).getUniqueId())) {
				if (!vulnerableDamageCauses.contains(e.getCause())) {
					e.setCancelled(true);
				}
			}
		}
	}

	@Override
	public void perform(Player p) {
		eventAbilityPerform(p);
	}
}
