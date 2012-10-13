package me.krconv.WinDooMVoteShop;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerExecuteCommand {
	
	WinDooMVoteShop plugin;

	PlayerExecuteCommand(WinDooMVoteShop instance) {
		plugin = instance;
	}
	
	public double stealCash(String playerName, double amount) {
		OfflinePlayer thisPlayer = plugin.getServer().getOfflinePlayer(playerName); // Use offline player - it returns player regardless of online/offline
		if (thisPlayer == null) {
			// Should always return some kind of offline player object, but just in case...
			return -1;
		}
		if (thisPlayer.hasPlayedBefore()) {
			// Player has been on server before, so we know we have a valid player entity with economy account and valid name
			plugin.getLogger().info("Player " + thisPlayer.getName() + " has played here before and is validated (updated validation process). Proceeding with StealCash command.");
		} else {
			// Player has not been on server before, so no economy account and can't even validate player name
			plugin.getLogger().warning("Player " + thisPlayer.getName() + " has NOT played here before and cannot be validated (updated validation process).  Aborting StealCash command.");
			return -1;
		}
		// Steal $amount from all online players and give to player (e.g. $50)
		Player[] players = plugin.getServer().getOnlinePlayers();
		double totalGrab = 0;
		if (players != null) {
			for (int i=0; i < players.length; i++) {
				Player currPlayer = players[i];
				if (!currPlayer.getDisplayName().equalsIgnoreCase(thisPlayer.getName())) {
					// Take from online players (skipping player who is doing the stealing of course)
					EconomyResponse result1;
					result1 = WinDooMVoteShop.economy.withdrawPlayer(currPlayer.getName(), amount);
					if (result1.type == EconomyResponse.ResponseType.SUCCESS) {
						plugin.getLogger().info("Successfully stolen " + String.format("%10.2f", amount).trim() + " from " + currPlayer.getName() + ". Result: " + result1.type.name());
						totalGrab += amount;
					} else {
						plugin.getLogger().warning("Error stealing " + String.format("%10.2f", amount).trim() + " from " + currPlayer.getName() + ". Result: " + result1.type.name());
					}
				}
				
			}
		}

		plugin.getServer().broadcastMessage(ChatColor.AQUA + playerName + " has stolen " + ChatColor.RED + "$" + String.format("%10.2f", amount).trim() + ChatColor.AQUA + " from each online player. Total grab: " + ChatColor.RED + "$" + String.format("%10.2f", totalGrab).trim() + ChatColor.GRAY + " -VoteShop");
		
		EconomyResponse result2;
		result2 = WinDooMVoteShop.economy.depositPlayer(thisPlayer.getName(), totalGrab);
		if (result2.type == EconomyResponse.ResponseType.SUCCESS){
			plugin.getLogger().info("Successfully given " + String.format("%10.2f", totalGrab).trim() + " to " + thisPlayer.getName() + ". Result: " + result2.type.name());
		} else {
			plugin.getLogger().warning("Error giving " + String.format("%10.2f", totalGrab).trim() + " to " + thisPlayer.getName() + ". Result: " + result2.type.name());
		}
		
		return totalGrab;
	}
	
	public void announceMessage(String playerName, String message) {
		plugin.getServer().broadcastMessage(ChatColor.AQUA + message + ChatColor.GRAY + " -VoteShop");
	}
	
}
