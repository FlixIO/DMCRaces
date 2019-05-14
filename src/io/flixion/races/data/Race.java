package io.flixion.races.data;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import io.flixion.races.Races;
import io.flixion.races.Util;

public class Race {
	private String raceName;
	private UUID raceUUID;
	private ItemStack unlockItem;
	private int unlockItemAmount;
	private Map<Integer, Race> childRaces = new TreeMap<>();
	private ItemStack raceIcon;
	private boolean isParent;
	private boolean matchByEnum;
	
	private boolean hasHunger;
	private boolean wolfAggression;
	private boolean resistKnockback;
	private double waterAversionDamage;
	private double fireAversionDamage;
	private int demonFireballEffectTicks;
	private Set<PotionEffect> nativePotionEffects;
	private Set<String> permissions;
	private Set<EntityType> passiveHostileMobs;
	private int damageboostChance;
	private double damageBoostDamage;
	
	public Race(String raceName, UUID raceUUID, ItemStack unlockItem, boolean hasHunger,
			boolean wolfAggression, boolean resistKnockback, double waterAversionDamage, double fireAversionDamage,
			int demonFireballEffectTicks, Set<PotionEffect> nativePotionEffects, Set<String> permissions,
			Set<EntityType> passiveHostileMobs, int damageboostChance, ItemStack raceIcon, boolean isParent, double damageBoostDamage, boolean matchByEnum) {
		super();
		this.raceName = raceName;
		this.raceUUID = raceUUID;
		this.unlockItem = unlockItem;
		this.hasHunger = hasHunger;
		this.wolfAggression = wolfAggression;
		this.resistKnockback = resistKnockback;
		this.waterAversionDamage = waterAversionDamage;
		this.fireAversionDamage = fireAversionDamage;
		this.demonFireballEffectTicks = demonFireballEffectTicks;
		this.nativePotionEffects = nativePotionEffects;
		this.permissions = permissions;
		this.passiveHostileMobs = passiveHostileMobs;
		this.damageboostChance = damageboostChance;
		this.raceIcon = raceIcon;
		this.isParent = isParent;
		this.matchByEnum = matchByEnum;
		this.damageboostChance = damageboostChance;
		this.unlockItemAmount = unlockItem.getAmount();
		unlockItem.setAmount(1);
	}
	
	public void applyRaceToPlayer(Player p, boolean newRace) {
		Bukkit.getScheduler().runTask(Races.getPL(), new Runnable() {
			
			@Override
			public void run() {
				if (newRace) {
					if (Races.getRaceHandler().getPlayerProfiles().get(p.getUniqueId()).getActiveRace() != null) { //Clean up old race data
						Race r = Races.getRaceHandler().getPlayerProfiles().get(p.getUniqueId()).getActiveRace();
						for (PotionEffect pE : r.getNativePotionEffects()) { //Remove potion effects from last race
							p.removePotionEffect(pE.getType());
						}
						for (String s : r.getPermissions()) { //Reset permission changes
//							if (s.startsWith("-")) {
//								Races.getPexHandler().getUser(p).addPermission(s.substring(1));
//							} else {
//								Races.getPexHandler().getUser(p).removePermission(s);
//							}
							Races.getPexHandler().getUser(p).removePermission(s);
						}
					}
					Set<String> unlockedPermissions = new HashSet<>();
					for (String s : permissions) {
						Races.getPexHandler().getUser(p).addPermission(s);
						if (!s.startsWith("-") && s.toLowerCase().startsWith("aoraces")) {
							unlockedPermissions.add(s);
						}
					}
					Races.getRaceHandler().getPlayerProfiles().get(p.getUniqueId()).setUnlockedPermissions(unlockedPermissions);
					Races.getRaceHandler().getPlayerProfiles().get(p.getUniqueId()).setActiveRace(getRace());
				}
				p.addPotionEffects(nativePotionEffects);
			}
		});
	}
	
	public void addChild(int rank, Race r) {
		childRaces.put(rank, r);
	}

	public String getRaceName() {
		return raceName;
	}

	public UUID getRaceUUID() {
		return raceUUID;
	}

	public ItemStack getUnlockItem() {
		return unlockItem;
	}
	
	public int getUnlockItemAmount() {
		return unlockItemAmount;
	}

	public Map<Integer, Race> getChildRaces() {
		return childRaces;
	}

	public boolean isHasHunger() {
		return hasHunger;
	}

	public boolean isWolfAggression() {
		return wolfAggression;
	}

	public boolean isResistKnockback() {
		return resistKnockback;
	}

	public double getWaterAversionDamage() {
		return waterAversionDamage;
	}

	public double getFireAversionDamage() {
		return fireAversionDamage;
	}

	public int getDemonFireballEffectTicks() {
		return demonFireballEffectTicks;
	}

	public Set<PotionEffect> getNativePotionEffects() {
		return nativePotionEffects;
	}

	public Set<String> getPermissions() {
		return permissions;
	}

	public Set<EntityType> getPassiveHostileMobs() {
		return passiveHostileMobs;
	}

	public int getDamageboostChance() {
		return damageboostChance;
	}

	public void setChildRaces(Map<Integer, Race> childRaces) {
		this.childRaces = childRaces;
	}
	
	public ItemStack getRaceIcon(boolean personalize, UUID u) {
		if (personalize) {
			ItemMeta m = raceIcon.getItemMeta();
			if (Races.getRaceHandler().getPlayerProfiles().get(u).getUnlockedRaces().contains(this)) {
				m.setDisplayName(Util.addColor(raceName + " &8[&aUnlocked&8]"));
			} else {
				m.setDisplayName(Util.addColor(raceName + " &8[&cLocked&8]"));
			}
			if (Races.getRaceHandler().getPlayerProfiles().get(u).getActiveRace() != null) {
				if (Races.getRaceHandler().getPlayerProfiles().get(u).getActiveRace().getRaceUUID().equals(raceUUID)) {
					m.setDisplayName(Util.addColor(m.getDisplayName() + " &8[&bEquipped&8]"));
				}
			}
			raceIcon.setItemMeta(m);
			return raceIcon;
		} else {
			return raceIcon;
		}
	}
	
	public boolean unlockNextChildRace(UUID u) {
		for (Map.Entry<Integer, Race> entry : childRaces.entrySet()) {
			if (!Races.getRaceHandler().getPlayerProfiles().get(u).getUnlockedRaces().contains(entry.getValue())) {
				Races.getRaceHandler().getPlayerProfiles().get(u).addUnlock(entry.getValue());
				return true;
			}
		}
		return false;
	}
	
	public boolean isParent() {
		return isParent;
	}
	
	public Race getRace() {
		return this;
	}

	public double getDamageBoostDamage() {
		return damageBoostDamage;
	}

	public void setDamageBoostDamage(double damageBoostDamage) {
		this.damageBoostDamage = damageBoostDamage;
	}
	
	public boolean isMatchByEnum() {
		return this.matchByEnum;
	}
	
}
