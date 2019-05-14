package io.flixion.races.ability;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.massivecraft.factions.FPlayers;

public class AngelicGift extends Ability {
	private double amountToHeal;
	private int effectRadius;
	private PotionEffect slowness;
	private PotionEffect weakness;

	public AngelicGift(int cooldownTime, int effectTime, double amountToHeal, int effectRadius, int slownessLevel, int weaknessLevel) {
		super(cooldownTime, effectTime);
		this.amountToHeal = amountToHeal;
		this.effectRadius = effectRadius;
		slowness = new PotionEffect(PotionEffectType.SLOW, super.getEffectTime(), slownessLevel, false, true);
		weakness = new PotionEffect(PotionEffectType.WEAKNESS, super.getEffectTime(), weaknessLevel, false, true);
	}
	
	public void perform(Player caster) {
		for (Entity e : caster.getWorld().getNearbyEntities(caster.getLocation(), effectRadius, effectRadius, effectRadius)){
			if (e instanceof Player) {
				Player p = (Player) e;
				if (p.getUniqueId().equals(caster.getUniqueId())) {
					continue;
				}
				if (FPlayers.getInstance().getByPlayer(caster).getFaction().getId().equals(FPlayers.getInstance().getByPlayer(p).getFaction().getId())) {
					if (p.getHealth() + amountToHeal >= 20) {
						p.setHealth(20);
					} else {
						p.setHealth(p.getHealth() + amountToHeal);
					}
				}
			}
		}
		caster.addPotionEffect(slowness);
		caster.addPotionEffect(weakness);
		caster.damage(amountToHeal);
	}
	
}
