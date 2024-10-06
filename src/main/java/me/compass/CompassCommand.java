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
            sender.sendMessage("Цю команду може виконати лише гравець.");
            return true;
        }
        Player player = (Player) sender;
        
        if (args.length == 0) {
	        boolean hidden = !Brain.getInstance().getConfig().getBoolean(player.getUniqueId()+".hidden", false);
	        Brain.getInstance().getConfig().set(player.getUniqueId()+".hidden", hidden);
	        if (hidden) {
	        	Compass.removeCompassForPlayer(player);
	        } else {
	        	Compass.createCompassForPlayer(player);
	        }
	        player.sendMessage(ChatColor.YELLOW + "Увімкнено режим відображення компасу: "+(hidden ? ChatColor.RED+"сховано" : ChatColor.GREEN+"відображається"));
	        Brain.getInstance().saveConfig();
        } 
        
        else if (args[0].equalsIgnoreCase("addmarker")) {
            if (args.length < 3) {
                player.sendMessage("Неправильна кількість аргументів. Використовуйте: /command addmarker <назва> <світ>,<x>,<y>,<z>");
                return true;
            }
            Player markerPlayer = Bukkit.getPlayer(args[1]);
            String markerName = args[2];
            String[] locationArgs = args[3].split(",");
            if (locationArgs.length != 4) {
                player.sendMessage("Невірний формат локації. Використовуйте: <світ>,<x>,<y>,<z>");
                return true;
            }
            try {
                String worldName = locationArgs[0];
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    player.sendMessage("Світ з назвою '" + worldName + "' не знайдено.");
                    return true;
                }
                double x = Double.parseDouble(locationArgs[1]);
                double y = Double.parseDouble(locationArgs[2]);
                double z = Double.parseDouble(locationArgs[3]);
                Location location = new Location(world, x, y, z);
                if (markerPlayer == null) {
                    player.sendMessage("Гравця '" + args[1] + "' не знайдено.");
                    return true;
                }
                Compass.addMarker(markerPlayer, markerName, location);
                player.sendMessage("Маркер '" + markerName + "' успішно додано для "+markerPlayer.getName()+".");
            } catch (NumberFormatException e) {
                player.sendMessage("Координати мають бути числами.");
                return true;
            }
        }
        
        else if (args[0].equalsIgnoreCase("removemarker")) {
            if (args.length < 2) {
                player.sendMessage("Неправильна кількість аргументів. Використовуйте: /command addmarker <назва> <світ>,<x>,<y>,<z>");
                return true;
            }
            Player markerPlayer = Bukkit.getPlayer(args[1]);
            String markerName = args[2];
            if (markerPlayer == null) {
                player.sendMessage("Гравця '" + args[1] + "' не знайдено.");
                return true;
            }
            if (!Compass.getMarkers(player).containsKey(markerName)) {
                player.sendMessage("Маркеру з назвою '" + args[1] + "' не знайдено.");
                return true;
            }
            Compass.removeMarker(markerPlayer, markerName);
            player.sendMessage("Маркер '" + markerName + "' успішно видалено для "+markerPlayer.getName()+".");
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    	Player player = (Player) sender;
        // дія
        if (args.length == 1) {
            return List.of("addmarker", "removemarker");
        }
        // гравець
        else if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        }
        // назва маркеру
        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("removemarker")) {
                return Compass.getMarkers(player).entrySet().stream()
                        .map(entry -> entry.getKey())
                        .collect(Collectors.toList());
            } else {
                return List.of();
            }
        }
        // локація маркеру через коми (args[3] - локація)
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
    
}
