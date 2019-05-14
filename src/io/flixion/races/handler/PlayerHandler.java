package io.flixion.races.handler;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import io.flixion.races.Races;
import io.flixion.races.Util;
import io.flixion.races.data.Cooldown;
import io.flixion.races.data.PlayerProfile;
import io.flixion.races.data.Race;
import io.flixion.races.persist.PersistPlayerData;

public class PlayerHandler implements Listener {
	
	@EventHandler
	public void loadPlayerData(PlayerJoinEvent e) {
		Bukkit.getScheduler().runTaskAsynchronously(Races.getPL(), new Runnable() {
			
			@Override
			public void run() {
				try {
					e.getPlayer().sendMessage(Util.sendPluginMessage("&aAttempting to load your player data!"));
					File f = new File(Races.getPL().getDataFolder().getPath() + "/data/", e.getPlayer().getUniqueId() + ".yml");
					if (f.exists()) { //load
						YamlConfiguration fileConf = YamlConfiguration.loadConfiguration(f);
						Set<Race> unlockedRaces = new HashSet<>();
						
						Race activeRace = null;
						for (String s : fileConf.getStringList("unlockedRaces")) {
							if (Races.getRaceHandler().getActiveRaceMap().containsKey(UUID.fromString(s))) {
								unlockedRaces.add(Races.getRaceHandler().getActiveRaceMap().get(UUID.fromString(s)));
							}
						}
						if (fileConf.getString("activeRace") != null) {
							if (Races.getRaceHandler().getActiveRaceMap().containsKey(UUID.fromString(fileConf.getString("activeRace")))) {
								activeRace = Races.getRaceHandler().getActiveRaceMap().get(UUID.fromString(fileConf.getString("activeRace")));
							}
						}
						Races.getRaceHandler().addPlayerData(e.getPlayer(), new PlayerProfile(e.getPlayer().getUniqueId(), activeRace, unlockedRaces, new Cooldown(new HashSet<>(fileConf.getStringList("cooldowns")))));
					} else { //new player profile
						Races.getRaceHandler().addPlayerData(e.getPlayer(), new PlayerProfile(e.getPlayer().getUniqueId(), null, new HashSet<>(), new Cooldown()));
					}
					e.getPlayer().sendMessage(Util.sendPluginMessage("&aPlayer data successfully loaded!"));
				} catch (IllegalArgumentException e) {
					Races.getPL().getLogger().log(Level.SEVERE, Util.sendPluginMessage("Invalid player data file!"), e.getMessage());
				}
			}
		});
	}
	
	@EventHandler
	public void persistPlayerData(PlayerQuitEvent e) {
		PlayerProfile profile = Races.getRaceHandler().getPlayerProfiles().get(e.getPlayer().getUniqueId());
		Bukkit.getScheduler().runTaskAsynchronously(Races.getPL(), new PersistPlayerData(profile.getU(), profile.getActiveRace(), profile.getUnlockedRaces(), profile.getPlayerCooldown()));
	}
}
