package io.flixion.races.ability;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Sting extends Ability {
	private Set<PotionEffect> appliedEffects = new HashSet<>();

	public Sting(int cooldownTime, int effectTime) {
		super(cooldownTime, effectTime);
		appliedEffects.add(new PotionEffect(PotionEffectType.POISON, super.getEffectTime(), 1, true, true));
		appliedEffects.add(new PotionEffect(PotionEffectType.CONFUSION, super.getEffectTime(), 1, true, true));
		appliedEffects.add(new PotionEffect(PotionEffectType.SLOW, super.getEffectTime(), 3, true, true));
	}
	
	@EventHandler
	public void detect(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
			if (getActivePlayers().contains(((Player) e.getDamager()).getUniqueId())) {
				Player p = (Player) e.getEntity();
				p.addPotionEffects(appliedEffects);
			}
		}
	}

	@Override
	public void perform(Player p) {
		eventAbilityPerform(p);
	}
}
