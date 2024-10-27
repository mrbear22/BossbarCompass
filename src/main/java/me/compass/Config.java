package me.compass;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;

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
        
        FileConfiguration config = Brain.getInstance().getConfig();
        
        config.addDefault("messages.only-player-command", "&cOnly a player can execute this command.");
        config.addDefault("messages.compass-status", "&eCompass display mode is now: ");
        config.addDefault("messages.compass-hidden", "&chidden");
        config.addDefault("messages.compass-visible", "&avisible");
        config.addDefault("messages.invalid-args", "&cInvalid number of arguments. Use: /compass addmarker <name> <world>,<x>,<y>,<z>");
        config.addDefault("messages.invalid-location-format", "&cInvalid location format. Use: <world>,<x>,<y>,<z>");
        config.addDefault("messages.world-not-found", "&cWorld with name '%world%' not found.");
        config.addDefault("messages.player-not-found", "&cPlayer '%player%' not found.");
        config.addDefault("messages.marker-added", "&aMarker '%marker%' successfully added for %player%.");
        config.addDefault("messages.coordinates-not-numbers", "&cCoordinates must be numbers.");
        config.addDefault("messages.marker-not-found", "&cMarker with name '%marker%' not found.");
        config.addDefault("messages.marker-removed", "&aMarker '%marker%' successfully removed for %player%.");

        
        /*
        config.addDefault("messages.only-player-command", "Цю команду може виконати лише гравець.");
        config.addDefault("messages.compass-status", "Увімкнено режим відображення компасу: ");
        config.addDefault("messages.compass-hidden", "сховано");
        config.addDefault("messages.compass-visible", "відображається");
        config.addDefault("messages.invalid-args", "Неправильна кількість аргументів. Використовуйте: /compass addmarker <назва> <світ>,<x>,<y>,<z>");
        config.addDefault("messages.invalid-location-format", "Невірний формат локації. Використовуйте: <світ>,<x>,<y>,<z>");
        config.addDefault("messages.world-not-found", "Світ з назвою '%world%' не знайдено.");
        config.addDefault("messages.player-not-found", "Гравця '%player%' не знайдено.");
        config.addDefault("messages.marker-added", "Маркер '%marker%' успішно додано для %player%.");
        config.addDefault("messages.coordinates-not-numbers", "Координати мають бути числами.");
        config.addDefault("messages.marker-not-found", "Маркеру з назвою '%marker%' не знайдено.");
        config.addDefault("messages.marker-removed", "Маркер '%marker%' успішно видалено для %player%.");
        */
        
        config.options().copyDefaults(true);
        Brain.getInstance().saveDefaultConfig();
        Brain.getInstance().saveConfig();
    }
    	
}
