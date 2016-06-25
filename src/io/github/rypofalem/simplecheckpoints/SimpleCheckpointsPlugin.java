package io.github.rypofalem.simplecheckpoints;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleCheckpointsPlugin extends JavaPlugin implements Listener, CommandExecutor{
	HashMap<UUID, Location> playerCheckpoints;
	World world;
	
	@Override
	public void onEnable(){
		Bukkit.getPluginManager().registerEvents(this, this);
		playerCheckpoints = new HashMap<UUID, Location>();
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent event){
		if(world == null || world != event.getPlayer().getWorld()) return;
		if(event.getPlayer().getLocation().clone().add(0, -1, 0).getBlock().getType() != Material.ENCHANTMENT_TABLE) return;
		UUID uid = event.getPlayer().getUniqueId();
		Location location = event.getPlayer().getLocation().clone();
		location.setX(location.getBlockX() + .5);
		location.setY(location.getBlockY() + 1);
		location.setZ(location.getBlockZ() + .5);
		if(playerCheckpoints.containsKey(uid)){
			Location prevPoint = playerCheckpoints.get(uid);
			if(!prevPoint.getBlock().equals(location.getBlock())){
				setPoint(uid, location);
			}
		}else{
			setPoint(uid, location);
		}
	}
	
	@EventHandler (priority = EventPriority.LOW, ignoreCancelled=false)
	public void onEnderPearl(PlayerInteractEvent event){
		if(world == null || world != event.getPlayer().getWorld()) return;
		if(event.getItem().getType() != Material.ENDER_PEARL) return;
		event.setCancelled(true);
		Player player = event.getPlayer();
		player.updateInventory();
		if(playerCheckpoints.containsKey(player.getUniqueId())){
			player.setFallDistance(0);
			player.teleport(playerCheckpoints.get(player.getUniqueId()));
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
	public void cancelPlayerDamage(EntityDamageEvent event){
		if(world == null || world != event.getEntity().getWorld()) return;
		if(event.getEntity() instanceof Player) event.setCancelled(true);
	}
	
	void setPoint(UUID uid, Location location){
		playerCheckpoints.put(uid, location);
		Bukkit.getPlayer(uid).sendTitle("", "Checkpoint set");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)){
			sender.sendMessage("Only players can use this command");
			return true;
		}
		
		if(args != null && args[0].equalsIgnoreCase("activate")){
			world = ((Player)sender).getWorld();
			sender.sendMessage("SimpleCheckpoints is now using this world!");
			return true;
		}
		
		return false;
	}
	
	void print(String message){
		Bukkit.broadcastMessage(message);
	}
}
