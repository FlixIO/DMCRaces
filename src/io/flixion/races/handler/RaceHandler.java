package io.flixion.races.handler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import io.flixion.races.HiddenStringUtils;
import io.flixion.races.Races;
import io.flixion.races.Util;
import io.flixion.races.ability.AbilityType;
import io.flixion.races.data.PlayerProfile;
import io.flixion.races.data.Race;
import io.flixion.races.gui.PersonalizedRaceGUI;
import io.flixion.races.gui.RaceGUI;

public class RaceHandler implements Listener {
	private Map<UUID, PlayerProfile> playerDataMap = new HashMap<>();
	private Map<UUID, Race> activeRaceMap = new HashMap<>();
	private Map<UUID, BukkitTask> fireAversionRunnables = new HashMap<>();
	private int raceSwitchCooldown;
	private ItemStack castWand;
	private boolean matchEnumOnly;
	
	public RaceHandler(ItemStack castWand, int raceSwitchCooldown, boolean matchEnumOnly) {
		this.raceSwitchCooldown = raceSwitchCooldown;
		this.castWand = castWand;
		this.matchEnumOnly = matchEnumOnly;
	}
	
	public boolean hasActiveRace(Player p) {
		if (playerDataMap.get(p.getUniqueId()).getActiveRace() != null){
			return true;
		}
		return false;
	}
	
	public Race getActiveRace (Player p) {
		return playerDataMap.get(p.getUniqueId()).getActiveRace();
	}
	
	@EventHandler
	public void monitorHunger(FoodLevelChangeEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (hasActiveRace(p)) {
				if (getActiveRace(p).isHasHunger()) {
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void monitorAggression(EntityTargetLivingEntityEvent e) {
		if (e.getTarget() instanceof Player) {
			Player p = (Player) e.getTarget();
			if (hasActiveRace(p)) {
				if (getActiveRace(p).getPassiveHostileMobs().contains(e.getEntityType())) {
					e.setCancelled(true);
				} else if (e.getEntityType() == EntityType.WOLF && !getActiveRace(p).isWolfAggression()) {
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void monitorFireAversion(EntityCombustEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (hasActiveRace(p)) {
				if (getActiveRace(p).getFireAversionDamage() > 0) {
					BukkitTask t = Bukkit.getScheduler().runTaskTimer(Races.getPL(), new Runnable() {
						
						@Override
						public void run() {
							if (p.getFireTicks() > -20) {
								p.damage(getActiveRace(p).getFireAversionDamage());
							} else {
								fireAversionRunnables.get(p.getUniqueId()).cancel();
								fireAversionRunnables.remove(p.getUniqueId());
							}
						}
					}, 0, 20);
					fireAversionRunnables.put(p.getUniqueId(), t);
				}
			}
		}
	}
	
	@EventHandler
	public void monitorCombat(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (hasActiveRace(p)) {
				if (!getActiveRace(p).isResistKnockback()) {
					Bukkit.getScheduler().runTaskLater(Races.getPL(), new Runnable() {
						
						@Override
						public void run() {
							p.setVelocity(new Vector(0, 0, 0));
						}
					}, 1);
				}
				if (e.getDamager() instanceof Player) {
					Player attacker = (Player) e.getDamager();
					if (Races.getPexHandler().getUser(attacker).has("aoraces.passive.damageboost")){
						if (Util.generateRandomInt(100, 0) <= getActiveRace(p).getDamageboostChance()) {
							e.setDamage(e.getDamage() + getActiveRace(p).getDamageBoostDamage());
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void detectCastWand(PlayerInteractEvent e) {
		if (hasActiveRace(e.getPlayer())) {
			if (e.getItem() != null && e.getItem().getType() != Material.AIR) {
				if (Util.compareItemStack(castWand, e.getItem(), matchEnumOnly)) {
					Player p = e.getPlayer();
					if (getPlayerProfiles().get(p.getUniqueId()).getAbilityPermissions().size() > 0) {
						if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) { //cycle
							AbilityType abilityType = getPlayerProfiles().get(p.getUniqueId()).getCurrentAbility(true);
							Date cooldown = getPlayerProfiles().get(p.getUniqueId()).getPlayerCooldown().checkCooldown(Races.abilityMap.get(abilityType).getCooldownTime(), abilityType);
							StringBuilder s = new StringBuilder();
							s.append("Current Selected Ability: &a ");
							s.append(Util.formatEnumString(abilityType));
							if (cooldown == null) {
								s.append(" | &cCooldown: false");
							} else {
								s.append(" | &cCooldown: true " + "[" + cooldown.getSeconds() + "s]");
							}
							p.sendMessage(Util.sendPluginMessage(s.toString()));
						} else if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) { //cast
							AbilityType abilityType = getPlayerProfiles().get(p.getUniqueId()).getCurrentAbility(false);
							if (abilityType == AbilityType.SWITCH_RACE) {
								return;
							}
							Date cooldown = getPlayerProfiles().get(p.getUniqueId()).getPlayerCooldown().checkCooldown(Races.abilityMap.get(abilityType).getCooldownTime(), abilityType);
							if (cooldown == null) {
								Races.abilityMap.get(abilityType).perform(p);
								p.sendMessage(Util.sendPluginMessage("You have performed: " + Util.formatEnumString(abilityType)));
								getPlayerProfiles().get(p.getUniqueId()).getPlayerCooldown().addCooldown(abilityType);
							} else {
								p.sendMessage(Util.sendPluginMessage("This ability is still under cooldown! " + "[" + cooldown.getSeconds() + "s]"));
							}
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void handleGUIClicks(InventoryClickEvent e) {
		if (e.getClickedInventory() != null && e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();
			if (e.getClickedInventory().getName().startsWith("Race Menu")) {
				e.setCancelled(true);
				if (e.getCurrentItem().getType() != Material.AIR) {
					ItemStack i = e.getCurrentItem();
					String data = HiddenStringUtils.extractHiddenString(i.getItemMeta().getLore().get(i.getItemMeta().getLore().size() - 1));
					Race r = activeRaceMap.get(UUID.fromString(data));
					p.openInventory(new PersonalizedRaceGUI(r, p.getUniqueId()).get());
				}
			} else if (e.getClickedInventory().getName().startsWith("Race Info")) {
				e.setCancelled(true);
				if (e.getCurrentItem().getType() != Material.AIR) {
					ItemStack i = e.getCurrentItem();
					if (i.getType() == Material.COMPASS) {
						p.openInventory(RaceGUI.getMainRaceGUI(p.getUniqueId()));
						return;
					}
					String data = HiddenStringUtils.extractHiddenString(i.getItemMeta().getLore().get(i.getItemMeta().getLore().size() - 1));
					Race r = activeRaceMap.get(UUID.fromString(data));
					if (e.isLeftClick()) {
						if (playerDataMap.get(p.getUniqueId()).getUnlockedRaces().contains(r)) {
							if (playerDataMap.get(p.getUniqueId()).getActiveRace() == null) {
								p.sendMessage(Util.sendPluginMessage("Successfully equipped race: " + r.getRaceName()));
								p.closeInventory();
								r.applyRaceToPlayer((Player) p, true);
							} else if (r.isParent()){
								p.sendMessage(Util.sendPluginMessage("You cannot equip a tier 1 race"));
								p.closeInventory();
							} else {
								if (!playerDataMap.get(p.getUniqueId()).getActiveRace().getRaceUUID().equals(r.getRaceUUID())) {
									p.sendMessage(Util.sendPluginMessage("Successfully equipped race: " + r.getRaceName()));
									p.closeInventory();
									r.applyRaceToPlayer((Player) p, true);
								} else {
									p.sendMessage(Util.sendPluginMessage("This race is already set as your active race!"));
								}
							}
						} else {
							p.sendMessage(Util.sendPluginMessage("You have not unlocked this race"));
							p.closeInventory();
						}
					} else if (e.isRightClick()) { //unlock race
						if (!playerDataMap.get(p.getUniqueId()).getUnlockedRaces().contains(r)) {
							boolean valid = true;
							for (int j = 0; j < e.getSlot(); j++) {
								if (e.getClickedInventory().getItem(j).getItemMeta().getDisplayName().contains("Locked")) {
									p.sendMessage(Util.sendPluginMessage("You must first unlock previous race tiers!"));
									p.closeInventory();
									valid = false;
									break;
								}
							}
							if (valid) {
								int amount = 0;
								for (int j = 0; j < p.getInventory().getContents().length; j++) {
									ItemStack item = p.getInventory().getContents()[j];
									if (amount >= r.getUnlockItemAmount()) {
										break;
									}
									if (Util.compareItemStack(r.getUnlockItem(), item, r.isMatchByEnum())) {
										amount += item.getAmount();
										p.getInventory().setItem(j, null);
										p.updateInventory();
									}
								}
								if (amount >= r.getUnlockItemAmount()) {
									if (amount - r.getUnlockItemAmount() > 0) {
										ItemStack remainder = new ItemStack(r.getUnlockItem());
										remainder.setAmount(amount - r.getUnlockItemAmount());
										p.getInventory().addItem(remainder);
										p.updateInventory();
									}
									if (r.isParent()) {
										Date cooldown = playerDataMap.get(p.getUniqueId()).getPlayerCooldown().checkCooldown(raceSwitchCooldown, AbilityType.SWITCH_RACE);
										if (cooldown == null) {
											playerDataMap.get(p.getUniqueId()).clearUnlocked();
											playerDataMap.get(p.getUniqueId()).addUnlock(r);
											playerDataMap.get(p.getUniqueId()).getPlayerCooldown().addCooldown(AbilityType.SWITCH_RACE);
											p.sendMessage(Util.sendPluginMessage("Successfully unlocked and equipped race: " + r.getRaceName()));
											p.closeInventory();
											r.applyRaceToPlayer((Player) p, true);
										} else {
											p.sendMessage(Util.sendPluginMessage("You have " + cooldown.getHours() + "h " + cooldown.getMinutes() + "m " + cooldown.getSeconds() + "s until you can switch to a new race!"));
											p.closeInventory();
										}
									} else {
										p.sendMessage(Util.sendPluginMessage("You have successfully unlocked and equipped race: " + r.getRaceName()));
										playerDataMap.get(p.getUniqueId()).addUnlock(r);
										r.applyRaceToPlayer((Player) p, true);
										p.closeInventory();
									}
								} else {
									if (amount > 0) {
										ItemStack remainder = new ItemStack(r.getUnlockItem());
										remainder.setAmount(amount);
										p.getInventory().addItem(remainder);	
									}
									p.sendMessage(Util.sendPluginMessage("You lack the required items to unlock this race"));
									p.closeInventory();
								}
							}
						} else {
							p.sendMessage(Util.sendPluginMessage("You have already unlocked this race!"));
							p.closeInventory();
						}
					}
				}
			}
		}
	}
	
	public Map<UUID, PlayerProfile> getPlayerProfiles() {
		return playerDataMap;
	}
	
	public void addPlayerData (Player p, PlayerProfile pp) {
		playerDataMap.put(p.getUniqueId(), pp);
	}

	public Map<UUID, Race> getActiveRaceMap() {
		return activeRaceMap;
	}
	
	public void addActiveRace(UUID u, Race r) {
		activeRaceMap.put(u, r);
	}
	
	public void addChildRace(UUID parent, UUID child, int rank) {
		activeRaceMap.get(parent).addChild(rank, activeRaceMap.get(child));
	}
}
