package io.flixion.races.ability;

import org.bukkit.entity.Player;

public class Glow extends Ability {

	public Glow(int cooldownTime, int effectTime) {
		super(cooldownTime, effectTime);
	}

	@Override
	public void perform(Player caster) {
		if (activePlayers.contains(caster.getUniqueId())) { //toggle
			activePlayers.remove(caster.getUniqueId());
			caster.setGlowing(false);
		} else {
			activePlayers.add(caster.getUniqueId());
			caster.setGlowing(true);
		}
	}
}
