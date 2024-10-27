package me.compass;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class CompassCommand implements CommandExecutor, TabCompleter {

    public void register() {
        Brain.getInstance().getCommand("compass").setExecutor(this);
        Brain.getInstance().getCommand("bc").setExecutor(this);
        Brain.getInstance().getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            toggleCompassVisibility(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "addmarker":
                if (!sender.hasPermission("bossbarcompass.addmarker")) {
                    sendMessage(sender, "no-permission", "You don't have permission to add markers.");
                    return true;
                }
                handleAddMarker(sender, args);
                break;
                
            case "removemarker":
                if (!sender.hasPermission("bossbarcompass.removemarker")) {
                    sendMessage(sender, "no-permission", "You don't have permission to remove markers.");
                    return true;
                }
                handleRemoveMarker(sender, args);
                break;
                
            default:
                sendMessage(sender, "none", "Unknown command.");
        }
        
        return true;
    }

    private void toggleCompassVisibility(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, "only-player-command", "This command can only be used by players.");
            return;
        }
        boolean hidden = !Brain.getInstance().getConfig().getBoolean("players." + player.getUniqueId() + ".hidden", false);
        Brain.getInstance().getConfig().set("players." + player.getUniqueId() + ".hidden", hidden);
        if (hidden) {
            Compass.removeCompassForPlayer(player);
        } else {
            Compass.createCompassForPlayer(player);
        }
        String message = hidden ? "hidden." : "visible.";
        sendMessage(player, "compass-status", "Compass status: " + message);
        Brain.getInstance().saveConfig();
    }

    private void handleAddMarker(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendMessage(sender, "", "Invalid arguments. Usage:");
            sendMessage(sender, "", "/compass addmarker <player> <name> <world>,<x>,<y>,<z>");
            sendMessage(sender, "", "/compass removemarker <player> <name>");
            return;
        }

        Player markerPlayer = Bukkit.getPlayer(args[1]);
        String markerName = args[2];
        String locationArg = (args.length > 3 && !args[3].equalsIgnoreCase("here")) ? args[3] : getPlayerLocation(sender);

        if (locationArg == null) {
            sendMessage(sender, "invalid-location-format", "Invalid location.");
            return;
        }

        Location location = parseLocation(sender, locationArg);
        if (location == null) return;

        Compass.addMarker(markerPlayer, markerName, location);
        sendMessage(sender, "none", "Marker " + markerName + " added for " + markerPlayer.getName() + ".");
    }

    private void handleRemoveMarker(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendMessage(sender, "none", "Invalid arguments. Usage: /command removemarker <player> <name>");
            return;
        }

        Player markerPlayer = Bukkit.getPlayer(args[1]);
        String markerName = args[2];

        if (markerPlayer == null) {
            sendMessage(sender, "player-not-found", "Player not found.");
            return;
        }

        if (!Compass.getMarkers(markerPlayer).containsKey(markerName)) {
            sendMessage(sender, "marker-not-found", "Marker not found.");
            return;
        }

        Compass.removeMarker(markerPlayer, markerName);
        sendMessage(sender, "none", "Marker " + markerName + " removed for " + markerPlayer.getName() + ".");
    }

    private String getPlayerLocation(CommandSender sender) {
        if (!(sender instanceof Player player)) return null;
        return player.getWorld().getName() + "," + player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ();
    }

    private Location parseLocation(CommandSender sender, String locationArg) {
        String[] locationParts = locationArg.split(",");
        if (locationParts.length != 4) {
            sendMessage(sender, "invalid-location-format", "Invalid location format.");
            return null;
        }

        try {
            World world = Bukkit.getWorld(locationParts[0]);
            if (world == null) {
                sendMessage(sender, "world-not-found", "World not found.");
                return null;
            }

            double x = Double.parseDouble(locationParts[1]);
            double y = Double.parseDouble(locationParts[2]);
            double z = Double.parseDouble(locationParts[3]);

            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            sendMessage(sender, "coordinates-not-numbers", "Coordinates must be numbers.");
            return null;
        }
    }

    private void sendMessage(CommandSender sender, String node, String message) {
    	if (Brain.getInstance().getConfig().get(node) != null) {
    		message = Brain.getInstance().getConfig().getString(node);
    	}
        sender.sendMessage(message);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    	Player player = (Player) sender;
        if (args.length == 1) {
            return List.of("addmarker", "removemarker");
        }
        else if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        }
        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("removemarker")) {
                return Compass.getMarkers(player).entrySet().stream()
                        .map(entry -> entry.getKey())
                        .collect(Collectors.toList());
            } else {
                return List.of("name");
            }
        }
        else if (args.length == 4 && args[0].equalsIgnoreCase("addmarker")) {
            String input = args[3];
            String[] parts = input.split(",");

            if (parts.length == 1) {
                List<String> worlds = Bukkit.getWorlds().stream()
                        .map(world -> world.getName() + ",")
                        .collect(Collectors.toList());
                worlds.add("here");
                return worlds;
            } else if (parts.length == 2) {
                return List.of(parts[0] + "," + parts[1] + ",");
            } else if (parts.length == 3) {
                return List.of(parts[0] + "," + parts[1] + "," + parts[2] + ",");
            }
        }
        return List.of();
    }
}
