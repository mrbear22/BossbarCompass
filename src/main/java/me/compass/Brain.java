package me.compass;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Brain extends JavaPlugin implements Listener {

	private static Brain INSTANCE;
	
	public static Brain getInstance() {
		return INSTANCE;
	}

    @Override
    public void onEnable() {
    	
    	INSTANCE = this;

    	new Config().register();
    	new Compass().register();
    	new CompassCommand().register();
    	
    }
    
    @Override
    public void onDisable() {
    	new Compass().unregister();
    }
}