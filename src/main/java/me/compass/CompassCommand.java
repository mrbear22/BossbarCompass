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

import net.md_5.bungee.api.ChatColor;

public class CompassCommand implements CommandExecutor, TabCompleter {

    public void register() {
        Brain.getInstance().getCommand("compass").setExecutor(this);
        Brain.getInstance().getCommand("bc").setExecutor(this);
        Brain.getInstance().getLogger().info(String.valueOf(getClass().getSimpleName()) + " module has been initialized!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Brain.getInstance().getConfig().getString("messages.only-player-command"));
            return true;
        }
        Player player = (Player) sender;
        
        if (args.length == 0) {
            boolean hidden = !Brain.getInstance().getConfig().getBoolean("players."+player.getUniqueId()+".hidden", false);
            Brain.getInstance().getConfig().set("players."+player.getUniqueId()+".hidden", hidden);
            if (hidden) {
                Compass.removeCompassForPlayer(player);
            } else {
                Compass.createCompassForPlayer(player);
            }
            String message = hidden ? Brain.getInstance().getConfig().getString("messages.compass-hidden") : Brain.getInstance().getConfig().getString("messages.compass-visible");
            sendMessage(player,Brain.getInstance().getConfig().getString("messages.compass-status") + message);
            Brain.getInstance().saveConfig();
        } 
        
        else if (args[0].equalsIgnoreCase("addmarker")) {
            if (args.length < 3) {
                sendMessage(player, Brain.getInstance().getConfig().getString("messages.invalid-args"));
                return true;
            }
            Player markerPlayer = Bukkit.getPlayer(args[1]);
            String markerName = args[2];
            String[] locationArgs = args[3].split(",");
            if (locationArgs.length != 4) {
                sendMessage(player, Brain.getInstance().getConfig().getString("messages.invalid-location-format"));
                return true;
            }
            try {
                String worldName = locationArgs[0];
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    sendMessage(player, Brain.getInstance().getConfig().getString("messages.world-not-found").replace("%world%", worldName));
                    return true;
                }
                double x = Double.parseDouble(locationArgs[1]);
                double y = Double.parseDouble(locationArgs[2]);
                double z = Double.parseDouble(locationArgs[3]);
                Location location = new Location(world, x, y, z);
                if (markerPlayer == null) {
                    sendMessage(player, Brain.getInstance().getConfig().getString("messages.player-not-found").replace("%player%", args[1]));
                    return true;
                }
                Compass.addMarker(markerPlayer, markerName, location);
                sendMessage(player, Brain.getInstance().getConfig().getString("messages.marker-added").replace("%marker%", markerName).replace("%player%", markerPlayer.getName()));
            } catch (NumberFormatException e) {
                sendMessage(player, Brain.getInstance().getConfig().getString("messages.coordinates-not-numbers"));
                return true;
            }
        } 
        
        else if (args[0].equalsIgnoreCase("removemarker")) {
            if (args.length < 2) {
                sendMessage(player, Brain.getInstance().getConfig().getString("messages.invalid-args"));
                return true;
            }
            Player markerPlayer = Bukkit.getPlayer(args[1]);
            String markerName = args[2];
            if (markerPlayer == null) {
                sendMessage(player, Brain.getInstance().getConfig().getString("messages.player-not-found").replace("%player%", args[1]));
                return true;
            }
            if (!Compass.getMarkers(player).containsKey(markerName)) {
                sendMessage(player, Brain.getInstance().getConfig().getString("messages.marker-not-found").replace("%marker%", markerName));
                return true;
            }
            Compass.removeMarker(markerPlayer, markerName);
            sendMessage(player, Brain.getInstance().getConfig().getString("messages.marker-removed").replace("%marker%", markerName).replace("%player%", markerPlayer.getName()));
        }
        
        return true;
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
                return Bukkit.getWorlds().stream()
                        .map(world -> world.getName() + ",")
                        .collect(Collectors.toList());
            } else if (parts.length == 2) {
                return List.of(parts[0] + "," + parts[1] + ",");
            } else if (parts.length == 3) {
                return List.of(parts[0] + "," + parts[1] + "," + parts[2] + ",");
            }
        }
        return List.of();
    }
    
    public void sendMessage(Player player, String message) {
    	player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
