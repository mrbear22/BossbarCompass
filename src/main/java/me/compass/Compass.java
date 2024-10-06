package me.compass;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Compass implements Listener {

	private Plugin plugin = Brain.getInstance();
    private static final Map<Player, BossBar> playerBossBars = new HashMap<>();
    private static final Map<Player, Map<String, Location>> markers = new HashMap<>();
	
    public void register() {
        Bukkit.getPluginManager().registerEvents(this, Brain.getInstance());
        new CompassUpdateTask().runTaskTimer(plugin, 0, 1);
        for (Player player : Bukkit.getOnlinePlayers()) {
            createCompassForPlayer(player);
            updateMarkers(player);
        }
        Brain.getInstance().getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
    }
    
    public void unregister() {
        for (Player player : Bukkit.getOnlinePlayers()) {
        	removeCompassForPlayer(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        createCompassForPlayer(event.getPlayer());
    }

    public static void addMarker(Player player, String name, Location location) {
        Brain.getInstance().getConfig().set("markers."+player.getUniqueId()+"."+name+".location", location.getWorld().getName()+","+location.getX()+","+location.getY()+","+location.getZ());
        Brain.getInstance().saveConfig();
        updateMarkers(player);
    }


    public static void removeMarker(Player player, String name) {
        Brain.getInstance().getConfig().set("markers."+player.getUniqueId()+"."+name, null);
        Brain.getInstance().saveConfig();
        updateMarkers(player);
    }
    
    public static Map<String, Location> getMarkers(Player player) {
        Map<String, Location> playerMarkers = new HashMap<>(markers.getOrDefault(player, new HashMap<>()));
        if (player.getRespawnLocation() != null) {
        	playerMarkers.put("🛌", player.getRespawnLocation());
        }
        /*Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        for (String warp : essentials.getWarps().getList()) {
        	try {
        		//essentials.getWarps().getLastOwner(warp)
        		playerMarkers.put(warp, essentials.getWarps().getWarp(warp));
			} catch (WarpNotFoundException | InvalidWorldException e) {
				e.printStackTrace();
			}
        }*/
		return playerMarkers;
	}
    
    private static void updateMarkers(Player player) {
        Map<String, Location> playerMarkers = markers.getOrDefault(player, new HashMap<>());
        playerMarkers = addMarkersFromConfig("globalmarkers", player, playerMarkers);
        String playerIdPath = "markers." + player.getUniqueId();
        if (Brain.getInstance().getConfig().get(playerIdPath) != null) {
        	playerMarkers = addMarkersFromConfig(playerIdPath, player, playerMarkers);
        }
        markers.put(player, playerMarkers);
    }

    private static Map<String, Location> addMarkersFromConfig(String configPath, Player player, Map<String, Location> playerMarkers) {
        for (String marker : Brain.getInstance().getConfig().getConfigurationSection(configPath).getKeys(true)) {
            String permission = Brain.getInstance().getConfig().getString(configPath + "." + marker + ".permission", null);
            if (permission == null || player.hasPermission(permission)) {
                String locationString = Brain.getInstance().getConfig().getString(configPath + "." + marker + ".location", "");
                Location location = parseLocation(locationString);
                if (location != null) {
                    playerMarkers.put(marker, location);
                }
            }
        }
        return playerMarkers;
    }

    private static Location parseLocation(String locationString) {
        String[] parts = locationString.split(",");
        if (parts.length < 4) {
            return null;
        }
        
        try {
            String worldName = parts[0];
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            
            return new Location(Bukkit.getWorld(worldName), x, y, z);
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }
    
    public static void createCompassForPlayer(Player player) {
        BossBar bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
        bossBar.setProgress(0);
        bossBar.addPlayer(player);
        playerBossBars.put(player, bossBar);
    }

    public static void removeCompassForPlayer(Player player) {
        BossBar bossBar = playerBossBars.get(player);
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    public void updateCompassForPlayer(Player player) {
        BossBar bossBar = playerBossBars.get(player);
        if (bossBar != null) {
            String compass = getCompass(player);
            bossBar.setTitle(compass);
        }
    }

    private String getCompass(Player player) {
        String baseCompass = "N" + " ".repeat(89) + "E" + " ".repeat(89) + "S" + " ".repeat(89) + "W" + " ".repeat(89); // 360 symbols
        int compassLength = baseCompass.length(); // 360
        float playerYaw = player.getLocation().getYaw();
        float normalizedYaw = (playerYaw + 360) % 360;
        int startIndex = (int) (normalizedYaw / 360 * compassLength);
        String compassDisplay = baseCompass.substring(startIndex) + baseCompass.substring(0, startIndex);
        StringBuilder compassBuilder = new StringBuilder(compassDisplay);
        for (Map.Entry<String, Location> entry : getMarkers(player).entrySet()) {
            String name = entry.getKey();
            Location location = entry.getValue();

            if (location.getWorld() == player.getWorld()) {
	            double angleToMarker = getAngleToMarker(player.getLocation(), location);
	            int markerIndex = (int) (angleToMarker / 360 * compassLength);
	            int actualIndex = (markerIndex - startIndex + compassLength) % compassLength;
	            String marker = name + " (" + (int) player.getLocation().distance(location) + "m)";
	            if (actualIndex + marker.length() <= compassLength) {
	                compassBuilder.replace(actualIndex, actualIndex + marker.length(), marker);
	            }
            }
        }
        return "§7"+compassBuilder;//.substring(120,240);
    }

	private double getAngleToMarker(Location playerLoc, Location markerLoc) {
        double deltaX = markerLoc.getX() - playerLoc.getX();
        double deltaZ = markerLoc.getZ() - playerLoc.getZ();
        double angle = Math.toDegrees(Math.atan2(deltaZ, deltaX)) + 90;
        return (angle + 360) % 360;
    }
    
    class CompassUpdateTask extends BukkitRunnable {
        @Override
        public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
            	updateCompassForPlayer(player);
            }
        }
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() == Material.COMPASS &&
            event.getAction().toString().contains("RIGHT_CLICK")) {
            Bukkit.dispatchCommand(player, "compass");
        }
    }
    
}
