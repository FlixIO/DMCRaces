package io.flixion.races.dayandnightchangeevent;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DayAndNightChangeEvent extends Event {
	private static final HandlerList HANDLERS_LIST = new HandlerList();
	private World w;
	private WorldTime type;
	
	public DayAndNightChangeEvent(World w, WorldTime type) {
		this.w = w;
		this.type = type;
	}
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}
	
    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

	public World getW() {
		return w;
	}
    
	public WorldTime getWorldTimeType() {
		return type;
	}
}
