package io.flixion.races.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.flixion.races.ability.AbilityType;

public class Cooldown {
	private Map<AbilityType, Long> cooldownTimeMap = new HashMap<>();
	
	public Cooldown() {
		for (int i = 0; i < AbilityType.values().length; i++) {
			cooldownTimeMap.put(AbilityType.values()[i], 0L);
		}
	}
	
	public Cooldown(Set<String> cooldownData) {
		for (String s : cooldownData) {
			String [] data = s.split("#");
			cooldownTimeMap.put(AbilityType.valueOf(data[0]), Long.parseLong(data[1]));
		}
	}
	
	@SuppressWarnings("deprecation")
	public Date checkCooldown(int timeDifference, AbilityType abilityType) {
		timeDifference *= 1000;
		long initCooldownTime = cooldownTimeMap.get(abilityType);
		if (System.currentTimeMillis() > initCooldownTime + timeDifference) {
			return null;
		} else {
			Date d = new Date(initCooldownTime + timeDifference - System.currentTimeMillis());
			d.setHours(d.getHours() - 2);
			return d;
		}
	}
	
	public void addCooldown(AbilityType abilityType) {
		cooldownTimeMap.put(abilityType, System.currentTimeMillis());
	}
	
	public List<String> serialize() {
		List<String> data = new ArrayList<>();
		for (Map.Entry<AbilityType, Long> entry : cooldownTimeMap.entrySet()) {
			data.add(entry.getKey().toString() + "#" + entry.getValue().toString());
		}
		return data;
	}
}
