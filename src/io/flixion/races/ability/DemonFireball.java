package io.flixion.races.ability;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.flixion.races.Races;

public class DemonFireball extends Ability implements Listener {
	private double fireballDamage;
	private double casterDamage;
	private PotionEffect immobilize;
	private PotionEffect blindness;
	private Map<UUID, Fireball> fireballTracking = new HashMap<>();

	public DemonFireball(int cooldownTime, int effectTime, double fireballDamage, double casterDamage) {
		super(cooldownTime, effectTime);
		this.fireballDamage = fireballDamage;
		this.casterDamage = casterDamage;
		immobilize = new PotionEffect(PotionEffectType.SLOW, super.getEffectTime(), 128, false, false);
		blindness = new PotionEffect(PotionEffectType.BLINDNESS, super.getEffectTime(), 2, false, false);
	}

	@Override
	public void perform(Player caster) {
		Fireball fireball = caster.launchProjectile(Fireball.class);
		fireball.setBounce(false);
		fireball.setIsIncendiary(false);
		caster.damage(casterDamage);
		fireball.setVelocity(fireball.getVelocity().multiply(2));
		fireballTracking.put(fireball.getUniqueId(), fireball);
	}
	
	@EventHandler
	public void projectileMiss(ProjectileHitEvent e) {
		if (e.getEntity() instanceof Fireball) {
			if (fireballTracking.containsKey(e.getEntity().getUniqueId())) {
				Bukkit.getScheduler().runTaskLater(Races.getPL(), new Runnable() {
					
					@Override
					public void run() {
						if (fireballTracking.containsKey(e.getEntity().getUniqueId())) {
							fireballTracking.remove(e.getEntity().getUniqueId());
						}
					}
				}, 20);
			}
		}
	}
	
	@EventHandler
	public void detectHit(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Fireball && e.getEntity() instanceof Player) {
			if (fireballTracking.containsKey(e.getDamager().getUniqueId())) {
				e.setDamage(fireballDamage);
				fireballTracking.remove(e.getEntity().getUniqueId());
				Player p = (Player) e.getEntity();
				if (Races.getRaceHandler().hasActiveRace(p)) {
					immobilize.apply(p);
					blindness.apply(p);
				}
			}
		}
	}
}
