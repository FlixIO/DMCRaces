package io.flixion.races.ability;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Cannibalism extends Ability {
	private PotionEffect regenEffect;

	public Cannibalism(int cooldownTime, int effectTime, int regenLevel) {
		super(cooldownTime, effectTime);
		regenEffect = new PotionEffect(PotionEffectType.REGENERATION, super.getEffectTime(), regenLevel, true, true);
	}
	
	@EventHandler
	public void applyEffect(PlayerDeathEvent e) {
		if (e.getEntity().getKiller() != null) {
			if (e.getEntity().getKiller() instanceof Player) {
				if (getActivePlayers().contains(((Player) e.getEntity().getKiller()).getUniqueId())) {
					Player p = (Player) e.getEntity().getKiller();
					p.addPotionEffect(regenEffect);
				}
			}
		}
	}

	@Override
	public void perform(Player p) {
		eventAbilityPerform(p);
	}
}
