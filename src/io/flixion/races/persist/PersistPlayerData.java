package io.flixion.races.persist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;

import io.flixion.races.Races;
import io.flixion.races.Util;
import io.flixion.races.data.Cooldown;
import io.flixion.races.data.Race;

public class PersistPlayerData implements Runnable {
	private UUID u;
	private YamlConfiguration fileHandler;
	private Race activeRace;
	private List<String> unlockedRaces = new ArrayList<>();
	private Cooldown playerCooldown;
	
	public PersistPlayerData(UUID u, Race activeRace, Set<Race> unlockedRaces, Cooldown playerCooldown) {
		super();
		this.u = u;
		this.activeRace = activeRace;
		this.playerCooldown = playerCooldown;
		for (Race r : unlockedRaces) {
			this.unlockedRaces.add(r.getRaceUUID().toString());
		}
	}

	@Override
	public void run() {
		File f = new File(Races.getPL().getDataFolder().getPath() + "/data/", u.toString() + ".yml");
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				Races.getPL().getLogger().log(Level.SEVERE, Util.sendPluginMessage("Unable to create player data file"), e.getStackTrace());
			}
		} try {
			fileHandler = YamlConfiguration.loadConfiguration(f);
			fileHandler.set("unlockedRaces", unlockedRaces);
			if (activeRace != null) {
				fileHandler.set("activeRace", activeRace.getRaceUUID().toString());
			}
			fileHandler.set("cooldowns", playerCooldown.serialize());
			fileHandler.save(f);
		} catch (IOException e) {
			Races.getPL().getLogger().log(Level.SEVERE, Util.sendPluginMessage("Unable to save player data file"), e.getStackTrace());
		}
	}
	
}
