package io.flixion.races.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import io.flixion.races.Races;
import io.flixion.races.Util;
import io.flixion.races.ability.AbilityType;

public class PlayerProfile {
	private UUID u;
	private List<AbilityType> abilityPermissions = new ArrayList<>();
	private Set<String> unlockedPermissions = new HashSet<>();
	private int cycleIndex = 0;
	private Race activeRace;
	private Set<Race> unlockedRaces = new HashSet<>();
	private Cooldown playerCooldown;
	private BukkitTask playerTask;
	private Player p;
	
	public PlayerProfile(UUID u, Race activeRace, Set<Race> unlockedRaces,
			Cooldown playerCooldown) {
		super();
		this.u = u;
		this.activeRace = activeRace;
		if (unlockedRaces != null) {
			this.unlockedRaces = unlockedRaces;
		}
		this.playerCooldown = playerCooldown;
		loadAbilityPermissions();
		if (activeRace != null) {
			activeRace.applyRaceToPlayer(Bukkit.getPlayer(u), false);
		}
		p = Bukkit.getPlayer(u);
		playerTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Races.getPL(), new Runnable() {
			
			@Override
			public void run() {
				if (p != null) {
					if (p.isOnline()) {
						if (Races.getRaceHandler().hasActiveRace(p)) {
							if (p.getWorld().hasStorm()) {
								if (!Util.isBlockAbovePlayer(p.getEyeLocation())) {
									if (!p.isDead()) {
										doDamage(p);
									}
								}
							}
							if (p.getLocation().subtract(0, 1, 0).getBlock().getType().toString().contains("SNOW") || p.getLocation().subtract(0, 1, 0).getBlock().getType().toString().contains("ICE")) {
								if (!p.isDead()) {
									doDamage(p);
								}
							}
							if (p.getLocation().getBlock().getType().toString().contains("WATER")) {
								if (!p.isDead()) {
									doDamage(p);
								}
							}
						}
					} else {
						playerTask.cancel();
					}
				} else {
					playerTask.cancel();
				}
			}
		}, 20, 20);
	}
	
	private Integer getIndex() {
		 if (cycleIndex <= abilityPermissions.size() - 1) {
			 cycleIndex++;
			 return cycleIndex;
		 } else {
			 cycleIndex = 0;
			 return cycleIndex;
		 }
	}
	
	
	public AbilityType getCurrentAbility(boolean increment) {
		try {
			if (increment == true) {
				getIndex();
				return abilityPermissions.get(cycleIndex);
			}
			if (cycleIndex == abilityPermissions.size()) {
				cycleIndex = 0;
				return abilityPermissions.get(cycleIndex);
			}
			return abilityPermissions.get(cycleIndex);
		} catch (IndexOutOfBoundsException e) {
			return abilityPermissions.get(0);
		}
	}
	
	private void doDamage(Player p) {
		p.damage(activeRace.getWaterAversionDamage());
	}

	public UUID getU() {
		return u;
	}

	public List<AbilityType> getAbilityPermissions() {
		return abilityPermissions;
	}

	public Race getActiveRace() {
		return activeRace;
	}

	public Set<Race> getUnlockedRaces() {
		return unlockedRaces;
	}

	public Cooldown getPlayerCooldown() {
		return playerCooldown;
	}
	
	public void setActiveRace(Race r) {
		activeRace = r;
		loadAbilityPermissions();
	}
	
	public void addUnlock(Race r) {
		unlockedRaces.add(r);
	}
	
	public void clearUnlocked() {
		unlockedRaces.clear();
		loadAbilityPermissions();
	}
	
	public void loadAbilityPermissions() {
		abilityPermissions.clear();
		for (int i = 0; i < AbilityType.values().length; i++) { //Get all abilities players have
			if (AbilityType.values()[i].toString().toLowerCase().equals("switch_race")) continue;
			if (Bukkit.getPlayer(u).hasPermission("aoraces.ability." + AbilityType.values()[i].toString().toLowerCase())) {
				abilityPermissions.add(AbilityType.values()[i]);
			}
		}
	}

	public Set<String> getUnlockedPermissions() {
		return unlockedPermissions;
	}

	public void setUnlockedPermissions(Set<String> unlockedPermissions) {
		this.unlockedPermissions = unlockedPermissions;
	}
}
