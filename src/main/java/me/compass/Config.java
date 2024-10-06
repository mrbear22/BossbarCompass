package me.compass;

import java.io.File;

public class Config {

	private static File config;
	
    public void register() {
        if (!Brain.getInstance().getDataFolder().exists())
        	Brain.getInstance().getDataFolder().mkdirs();
        config = new File(Brain.getInstance().getDataFolder(), "config.yml");
        if (!config.exists()) {
        	Brain.getInstance().getLogger().info("config.yml not found, creating....");
        	Brain.getInstance().saveDefaultConfig();
            Brain.getInstance().getConfig().addDefault("globalmarkers.spawn.location", "world,0.0,60.0,0.0");
            Brain.getInstance().getConfig().addDefault("globalmarkers.spawn.permission", "example.permission.spawn");
            Brain.getInstance().getConfig().options().copyDefaults(true);
        } else {
        	Brain.getInstance().getLogger().info("config.yml found, loadinig.....");
        }
        
        Brain.getInstance().saveConfig();
    }
    	
}
