package me.krconv.WinDooMVoteShop;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MyPlayerListener implements Listener{

	WinDooMVoteShop plugin;

	MyPlayerListener(WinDooMVoteShop instance) {
		plugin = instance;
	}
	@EventHandler (priority = EventPriority.LOW)
	public void JoinEvent(PlayerJoinEvent event) {
		if (plugin.queue.contains(event.getPlayer().getName())) {
			String player = event.getPlayer().getName();
			List<String> commandToRun = plugin.queue.getStringList(player);
			for (String command : commandToRun) {
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),command);
				plugin.logger.info("[WinDooMVoteShop] Executed command for Player:" + player + " Command:/" + command + "on login");
				plugin.writeLogFile("Executed command for Player:" + player + " Command:" + command + "on login");
			}
			List<String> queueList = plugin.queue.getStringList(player);
			queueList.clear();
			plugin.queue.set(player, queueList);
			plugin.saveQueueFile();
		}
	}
}
