package me.krconv.WinDoomVoteShop;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MyPlayerListener implements Listener{

	WinDoomVoteShop plugin;

	MyPlayerListener(WinDoomVoteShop instance) {
		plugin = instance;
	}

	@EventHandler (priority = EventPriority.LOW)
	public void JoinEvent(PlayerJoinEvent event) {
		// If player has queued command, schedule event to take place a few seconds later
		// in case player does not survive login event (e.g. kick from ban, disconnected due to error, etc.)
		if (plugin.queue.contains(event.getPlayer().getName())) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new PlayerExecuteCommandTask(plugin, event.getPlayer().getName(), event.getPlayer().getEntityId()), 200L);
		}
	}

}
