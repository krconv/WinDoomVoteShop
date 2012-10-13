package me.krconv.WinDooMVoteShop;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PlayerExecuteCommandTask implements Runnable {

	WinDooMVoteShop plugin;
	String playerName;
	int entityID;
	
	PlayerExecuteCommandTask (WinDooMVoteShop plugin, String playerName, int entityID) {
		this.plugin = plugin;
		this.playerName = playerName;
		this.entityID = entityID;
	}
	
	@Override
	public void run() {
		issueCommandForOnlinePlayer();
	}
	
	public void issueCommandForOnlinePlayer (){
		// If player connects and relogs before the scheduled command is issued, it may result in 2 commands
		// being scheduled. Match current entity ID with entity ID from when event was scheduled to identify
		// whether or not the player has relogged or anything of the sort since the event was scheduled.
		// Only issue command if the entity IDs match.
		
		Player newPlayerObject = plugin.getServer().getPlayer(playerName); // Get current player object for player
		int newEntityID; // Hold current entity ID for player
		
		if (newPlayerObject != null) {
			newEntityID = newPlayerObject.getEntityId(); // Get current entity ID for player
		} else {
			// New player object is null, player must not be online
			return;
		}
		
		if (newEntityID == entityID) {
			// Entity ID passed to this task matches the current entity ID of this player, so 
			// this task can continue.
			List<String> commandToRun = plugin.queue.getStringList(playerName);
			for (String command : commandToRun) {
				plugin.getLogger().info("Executing queued command on login of Player: " + playerName + ", Command: " + command);
				plugin.writeLogFile("Executing queued command on login of Player: " + playerName + ", Command: " + command);
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
				newPlayerObject.sendMessage(ChatColor.GREEN + "[VoteShop]  Queued vote shop purchase has been issued.");
			}
			List<String> queueList = plugin.queue.getStringList(playerName);
			queueList.clear();
			plugin.queue.set(playerName, queueList);
			plugin.saveQueueFile();
		} else {
			// Entity IDs do not match, player must have relogged
			// Do nothing
			return;
		}
	}

}
