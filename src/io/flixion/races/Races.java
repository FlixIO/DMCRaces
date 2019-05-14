package io.flixion.races;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import io.flixion.races.ability.Ability;
import io.flixion.races.ability.AbilityType;
import io.flixion.races.ability.AngelicGift;
import io.flixion.races.ability.Cannibalism;
import io.flixion.races.ability.DemonFireball;
import io.flixion.races.ability.Disperse;
import io.flixion.races.ability.Glow;
import io.flixion.races.ability.Shadowborn;
import io.flixion.races.ability.Sting;
import io.flixion.races.ability.Stoneform;
import io.flixion.races.ability.Traveller;
import io.flixion.races.data.PlayerProfile;
import io.flixion.races.data.Race;
import io.flixion.races.dayandnightchangeevent.WorldTickMonitor;
import io.flixion.races.gui.RaceGUI;
import io.flixion.races.handler.CommandHandler;
import io.flixion.races.handler.PlayerHandler;
import io.flixion.races.handler.RaceHandler;
import io.flixion.races.persist.PersistPlayerData;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Races extends JavaPlugin {
	private static Races instance;
	private static RaceHandler raceHandler;
	private static PermissionManager pexHandler;
	public static final Map<AbilityType, Ability> abilityMap = new HashMap<>();
	private BukkitTask intervalPersistance;

	//Note to future developer: There is no onDisable, plugin tasks and future tasks are taken care of on PlayerQuitEvent in each Ability
	
	public static Races getPL() {
		return instance;
	}

	public static RaceHandler getRaceHandler() {
		return raceHandler;
	}
	
	public static PermissionManager getPexHandler() {
		return pexHandler;
	}
	
	public void onDisable() {
		intervalPersistance.cancel();
	}

	public void onEnable() {
		instance = this;
		try {
			folderVerification();
		} catch (IOException e) {
			e.printStackTrace();
		}
		importData();
		importRaces();
		pexHandler = PermissionsEx.getPermissionManager();
		getCommand("race").setExecutor(new CommandHandler());
		Bukkit.getPluginManager().registerEvents(raceHandler, this);
		Bukkit.getPluginManager().registerEvents(new PlayerHandler(), this);
		new RaceGUI();
		scheduledPersistance();
	}

	private boolean folderVerification() throws IOException {
		new File(getDataFolder().getPath() + "/races").mkdirs();
		new File(getDataFolder().getPath() + "/data").mkdirs();
		if (!new File(getDataFolder(), "config.yml").exists()) {
			saveResource("config.yml", false);
		}
		return true;
	}
	
	private void importData() {
		File f = new File(getDataFolder(), "config.yml");
		if (!f.exists()) {
			saveResource("config.yml", false);
		}
		YamlConfiguration fileConf = YamlConfiguration.loadConfiguration(f);
		try {
			ItemStack castItem = null;
			if (fileConf.getBoolean("castItemData.matchByEnum")) {
				castItem = new ItemStack(
						Material.valueOf(fileConf.getString("castItemData.itemType")));
			} else {
				castItem = new ItemStack(
						Material.valueOf(fileConf.getString("castItemData.itemType")),
						1, (short) fileConf.getInt("castItemData.itemData"));
				ItemMeta castItemMeta = castItem.getItemMeta();
				List<String> lore = new ArrayList<>();
				for (String s : fileConf.getStringList("castItemData.itemLore")) {
					lore.add(Util.addColor(s));
				}
				castItemMeta.setDisplayName(Util.addColor(fileConf.getString("castItemData.itemName")));
				castItemMeta.setLore(lore);
				castItem.setItemMeta(castItemMeta);
			}
			
			abilityMap.put(AbilityType.ANGELIC_GIFT, new AngelicGift(fileConf.getInt("abilities.angelicgift.cooldownTime"), fileConf.getInt("abilities.angelicgift.effectTime"), fileConf.getDouble("abilities.angelicgift.amountToHeal"), fileConf.getInt("abilities.angelicgift.effectRadius"), fileConf.getInt("abilities.angelicgift.slownessLevel"), fileConf.getInt("abilities.angelicgift.weaknessLevel")));
			abilityMap.put(AbilityType.CANNIBALISM, new Cannibalism(fileConf.getInt("abilities.canniblism.cooldownTime"), fileConf.getInt("abilities.canniblism.effectTime"), fileConf.getInt("abilities.canniblism.regenEffectLevel")));
			abilityMap.put(AbilityType.DEMON_FIREBALL, new DemonFireball(fileConf.getInt("abilities.demonfireball.cooldownTime"), 0, fileConf.getDouble("abilities.demonfireball.fireballDamage"), fileConf.getDouble("abilities.demonfireball.castInflictedDamage")));
			abilityMap.put(AbilityType.DISPERSE, new Disperse(fileConf.getInt("abilities.disperse.cooldownTime"), fileConf.getInt("abilities.disperse.effectTime")));
			abilityMap.put(AbilityType.GLOW, new Glow(0, 0));
			abilityMap.put(AbilityType.SHADOWBORN, new Shadowborn(0, 0, new WorldTickMonitor()));
			abilityMap.put(AbilityType.STING, new Sting(fileConf.getInt("abilities.sting.cooldownTime"), fileConf.getInt("abilities.sting.effectTime")));
			abilityMap.put(AbilityType.STONEFORM, new Stoneform(fileConf.getInt("abilities.stoneform.cooldownTime"), fileConf.getInt("abilities.stoneform.effectTime")));
			abilityMap.put(AbilityType.TRAVELLER, new Traveller(fileConf.getInt("abilities.traveller.cooldownTime"), fileConf.getInt("abilities.traveller.immobilizeTime"), fileConf.getInt("abilities.traveller.teleportDistance")));
			
			for(Map.Entry<AbilityType, Ability> entry : abilityMap.entrySet()) {
				Bukkit.getPluginManager().registerEvents(entry.getValue(), this);
			}
			
			raceHandler = new RaceHandler(castItem, fileConf.getInt("raceSwitchCooldown"), fileConf.getBoolean("castItemData.matchByEnum"));
			
		} catch (IllegalArgumentException e) {
			getLogger().log(Level.SEVERE, "Failed to import config data, plugin will be disabled!", e.getMessage());
			getServer().getPluginManager().disablePlugin(this);
		}
	}
	
	private void scheduledPersistance() {
		intervalPersistance = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
			
			@Override
			public void run() {
				for (Map.Entry<UUID, PlayerProfile> entry : raceHandler.getPlayerProfiles().entrySet()) {
					if (entry.getValue() != null) {
						new PersistPlayerData(entry.getKey(), entry.getValue().getActiveRace(), entry.getValue().getUnlockedRaces(), entry.getValue().getPlayerCooldown());
					}
				}
			}
		}, 18000, 18000);
	}

	private void importRaces() {
		long init = System.currentTimeMillis();
		File[] persistedRaces = new File(getDataFolder().getPath() + "/races/").listFiles();
		if (persistedRaces != null) {
			for (int i = 0; i < persistedRaces.length; i++) {
				if (!persistedRaces[1].getName().endsWith(".yml")) {
					continue;
				}
				YamlConfiguration fileConf = YamlConfiguration.loadConfiguration(persistedRaces[i]);
				String fileName = FilenameUtils.getBaseName(persistedRaces[i].getName());
				try {
					if (!raceHandler.getActiveRaceMap().containsKey(Util.genUUID(fileName))) {
						ItemStack unlockItem = null;
						if (fileConf.getBoolean("unlockItem.matchByEnum")) {
							unlockItem = new ItemStack(Material.valueOf(fileConf.getString("unlockItem.materialEnum")), fileConf.getInt("unlockItem.amount"));
						} else {
							unlockItem = new ItemStack(
									Material.valueOf(fileConf.getString("unlockItem.materialEnum")),
									fileConf.getInt("unlockItem.amount"), (short) fileConf.getInt("unlockItem.byteData"));
							ItemMeta unlockItemMeta = unlockItem.getItemMeta();
							List<String> lore = new ArrayList<>();
							for (String s : fileConf.getStringList("unlockItem.lore")) {
								lore.add(Util.addColor(s));
							}
							unlockItemMeta.setDisplayName(Util.addColor(fileConf.getString("unlockItem.name")));
							unlockItemMeta.setLore(lore);
							unlockItem.setItemMeta(unlockItemMeta);
						}
						ItemStack raceIcon = new ItemStack(
								Material.valueOf(fileConf.getString("raceIcon.materialEnum")), 1,
								(short) fileConf.getInt("raceIcon.byteData"));
						ItemMeta raceIconMeta = raceIcon.getItemMeta();
						List<String> raceIconLore = new ArrayList<>();
						for (String s : fileConf.getStringList("raceIcon.lore")) {
							raceIconLore.add(Util.addColor(s));
						}
						raceIconLore.add(HiddenStringUtils.encodeString(Util.genUUID(fileName).toString()));
						raceIconMeta.setDisplayName(Util.addColor(fileConf.getString("raceName")));
						raceIconMeta.setLore(raceIconLore);
						raceIcon.setItemMeta(raceIconMeta);

						Set<PotionEffect> nativePotionEffects = new HashSet<>();
						for (String s : fileConf.getStringList("passive.nativePotionEffects")) {
							String[] data = s.split("#");
							nativePotionEffects.add(new PotionEffect(PotionEffectType.getByName(data[0]),
									Integer.MAX_VALUE, Integer.parseInt(data[1]), Boolean.parseBoolean(data[2])));
						}
						Set<String> permissions = new HashSet<>(fileConf.getStringList("passive.permissions"));
						Set<EntityType> passiveHostileEntities = new HashSet<>();
						for (String s : fileConf.getStringList("passive.hostileMobAggression")) {
							passiveHostileEntities.add(EntityType.valueOf(s));
						}
						Race r = new Race(Util.addColor(fileConf.getString("raceName")), Util.genUUID(fileName),
								unlockItem, fileConf.getBoolean("passive.hasHunger"),
								fileConf.getBoolean("passive.wolfAggression"),
								fileConf.getBoolean("passive.resistKnockback"),
								fileConf.getDouble("passive.waterAversionDamage"),
								fileConf.getDouble("passive.fireAversionDamage"),
								fileConf.getInt("passive.fireballVulernability.effectLength"), nativePotionEffects,
								permissions, passiveHostileEntities, fileConf.getInt("passive.damageBoost.chance"),
								raceIcon, fileConf.getBoolean("raceHeirachy.isParentRace"),
								fileConf.getDouble("passive.damageBoost.damage"), fileConf.getBoolean("unlockItem.matchByEnum"));
						raceHandler.addActiveRace(r.getRaceUUID(), r);
					} else {
						throw new IllegalArgumentException(
								"Duplicate Race! Race already exists in memory! Skipping race");
					}
				} catch (IllegalArgumentException e) {
					getLogger().log(Level.SEVERE,
							Util.sendPluginMessage(
									"Failed to import race (Invalid Enum/Number Processing/Duplicate Race): "
											+ persistedRaces[i].getName()),
							e.getMessage());
					e.printStackTrace();
					continue;
				} catch (NullPointerException e) {
					e.printStackTrace();
					continue;
				}
			}
			for (int i = 0; i < persistedRaces.length; i++) {
				if (!persistedRaces[1].getName().endsWith(".yml")) {
					continue;
				}
				String fileName = FilenameUtils.getBaseName(persistedRaces[i].getName());
				YamlConfiguration fileConf = YamlConfiguration.loadConfiguration(persistedRaces[i]);
				try {
					if (fileConf.getBoolean("raceHeirachy.isParentRace")) {
						if (raceHandler.getActiveRaceMap().containsKey(Util.genUUID(fileName))) {
							raceHandler.addChildRace(Util.genUUID(fileName), Util.genUUID(fileName), 0);
							for (String s : fileConf.getStringList("raceHeirachy.childRaces")) {
								String[] data = s.split("#");
								if (raceHandler.getActiveRaceMap().containsKey(Util.genUUID(data[0]))) {
									raceHandler.addChildRace(Util.genUUID(fileName), Util.genUUID(data[0]),
											Integer.parseInt(data[1]));
								} else {
									throw new IllegalArgumentException(
											Util.sendPluginMessage("Unknown child entered " + data[0]));
								}
							}
						}
					}
				} catch (IllegalArgumentException e) {
					getLogger().log(Level.SEVERE,
							Util.sendPluginMessage(
									"Failed to add child race (Race does not exist): " + persistedRaces[i].getName()),
							e.getMessage());
					e.printStackTrace();
					continue;
				} catch (NullPointerException e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		getLogger().log(Level.INFO, Util.sendPluginMessage("Imported " + raceHandler.getActiveRaceMap().size()
				+ " race data files in: [" + (System.currentTimeMillis() - init) + "ms]"));
	}
}
