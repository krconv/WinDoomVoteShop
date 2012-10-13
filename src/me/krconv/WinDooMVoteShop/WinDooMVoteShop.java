package me.krconv.WinDooMVoteShop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class WinDooMVoteShop extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Minecraft");
	public static WinDooMVoteShop plugin;
	public FileConfiguration queue = null;
	public File queueFile = null;
	public File logFile;
	public static Economy economy = null;
	PluginDescriptionFile pdfFile; // Moved declaration up to make it accessible in all methods
	PlayerExecuteCommand executer;
	public boolean IsEconomyAvailable = false;

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		//this.getLogger().info(pdfFile.getName() + " has been disabled!");
		this.getLogger().info(pdfFile.getName() + " has been disabled!");
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new MyPlayerListener(this), this);
		executer = new PlayerExecuteCommand(this);
		this.setupQueueFile();
		this.saveQueueFile();
		this.setupLogFile();
		//PluginDescriptionFile pdfFile = this.getDescription(); Moved declaration up to make it accessible in all methods
		pdfFile = this.getDescription();
		//this.getLogger().info(pdfFile.getName() + " v" + pdfFile.getVersion() + " has been enabled!");
		this.getLogger().info(pdfFile.getName() + " v" + pdfFile.getVersion() + " has been enabled!");
		IsEconomyAvailable = setupEconomy();
		
		if (!IsEconomyAvailable) {
			this.getLogger().warning("No economy plugin has been detected. Any features requiring economy are disabled (currently only StealCash)");	
		}
	}
	
	private boolean setupEconomy()
    {
		
		try {
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
	        if (economyProvider != null) {
	            economy = economyProvider.getProvider();
	        }	
		} catch (NoClassDefFoundError e) {
			this.getLogger().info(pdfFile.getName() + " is still enabled but could not hook into Vault, required by the StealCash feature. Is Vault installed? See error below:");
			e.printStackTrace();
		}

        return (economy != null);
    }

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!commandLabel.equalsIgnoreCase("voteshop")) {
			return false;
		}
		if (sender instanceof Player) {
			sender.sendMessage(ChatColor.RED + "This command can only be run from console!");
			return false;
		}
		// Must check that at least one arg (command) was sent
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Please do /voteshop HELP for a list of commands.");
			return false;
		}
		if (args[0].equalsIgnoreCase("ping")) {
			sender.sendMessage("Pong");
			return true;
		} else if (args[0].equalsIgnoreCase("command")) {
			// Must see if any args exist after "command"
			if (args.length >= 3) {
				String commandToRun = stripLeadingSlash(mergeRemainingArgs(args, 2));
				Player player = getServer().getPlayer(args[1]);
				if (player != null) {
					this.getLogger().info("Executed command for Player: " + args[1] + ", Command: " + commandToRun);
					sender.sendMessage("Executed");
					getServer().dispatchCommand(getServer().getConsoleSender(), commandToRun);
					writeLogFile("Executed command for Player:" + args[1] + " Command:" + commandToRun);
					player.sendMessage(ChatColor.GREEN + "[VoteShop]  Vote Shop purchase has been issued.");
					return true;
				} else {
					this.getLogger().info("Queued command for Player: " + args[1] + ", Command: " + commandToRun);
					sender.sendMessage("Queued");
					List<String> queueList = null;
					if (queue.getStringList(args[1]) != null) {
						queueList = queue.getStringList(args[1]);
					}
					queueList.add(commandToRun);
					queue.getStringList(args[1]);
					queue.set(args[1], queueList);
					saveQueueFile();
					writeLogFile("Queued command for Player: " + args[1] + ", Command: " + commandToRun);
				}	
			} else {
				// Incomplete command sent
				sender.sendMessage(ChatColor.RED + "Format is /voteshop command <Player> <Command To Run>");
				return false;
			}
		} else if (args[0].equalsIgnoreCase("StealCash")) {
			// This can be run regardless of player being online or not (no need to queue)
			// Requires Vault to be enabled
			if (!IsEconomyAvailable) {
				sender.sendMessage("NoEconomy:  No economy system is available.");
				return false;
			}
			// Must see if any args exist after "command"
			if (args.length == 3) {
				String playerName = args[1];
				if (playerName != null) {
					double amount;
					try {
						amount = Double.parseDouble(stripLeadingSlash(mergeRemainingArgs(args, 2)));	
					} catch (Exception e) {
						sender.sendMessage(ChatColor.RED + "Format is /voteshop StealCash <Player> <Amount>");
						return false;
					}
					this.getLogger().info("Executing StealCash command for Player: " + playerName + ", Amount: $" + amount);
					// Steal an amount from all online players & give to player
					sender.sendMessage("Executed");
					double totalGrab = executer.stealCash(playerName, amount);
					if (totalGrab == -1) {
						writeLogFile("Error executing StealCash command for Player: " + args[1] + ", Amount: $" + amount + ":  Has player ever played here?");
					} else {
						writeLogFile("Executed StealCash command for Player: " + args[1] + ", Amount: $" + amount + ", TotalGrab: " + totalGrab);	
					}
					
					// Notify player if online
					Player player = getServer().getPlayer(args[1]);
					if (player != null) {
						player.sendMessage(ChatColor.GREEN + "[VoteShop]  You have stolen $" + amount + " from all online players. Total grab: $" + totalGrab + "!");	
					}
					return true;
				}
			} else {
				// Incomplete command sent
				sender.sendMessage(ChatColor.RED + "Format is /voteshop StealCash <Player> <Amount>");
				return false;
			}
		}
		else if (args[0].equalsIgnoreCase("Announce")) {
			// This can be run regardless of player being online or not (no need to queue)
			// Must see if any args exist after "command"
			if (args.length >= 3) {
				String playerName = args[1];
				String message = mergeRemainingArgs(args, 2);
				if (playerName != null && message != null) {
					this.getLogger().info("Executing Announce command for Player: " + playerName + ", Message: $" + message);
					// Announce message
					sender.sendMessage("Executed");
					executer.announceMessage(playerName, message);
					writeLogFile("Executed Announce command for Player: " + args[1] + ", Message: " + message);
					
					// Notify player if online
					Player player = getServer().getPlayer(args[1]);
					if (player != null) {
						player.sendMessage(ChatColor.GREEN + "[VoteShop]  Your message has been announced.");
					}
					return true;
				}
			} else {
				// Incomplete command sent
				sender.sendMessage(ChatColor.RED + "Format is /voteshop Announce <Player> <Message>");
				return false;
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Unknown command.  Do /voteshop HELP for a list of commands.");
			return false;
		}
		return false;
	}
	
	private void setupLogFile() {
		logFile = new File(getDataFolder(), "Transaction Log.txt");
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(
						logFile, true));
				writer.write("# Log of VoteShop Transactions");
				writer.newLine();
				writer.close();
			} catch (IOException e) {
				this.getLogger().severe("Could not create 'Transaction Log.txt'!");
				e.printStackTrace();
				return;
			}
		}
	}

	public void writeLogFile(String stringLog) {
		String stringToWrite = new String(); // Formatted string to write
		Date timeStamp = new Date(); // TimeStamp of event (timeStamp = now)
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd"); // Date formatter
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss:SSS"); // Time formatter
		stringToWrite = "[" + sdfDate.format(timeStamp) + "] ["
				+ sdfTime.format(timeStamp) + "] - ";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
			writer.write(stringToWrite + stringLog);
			writer.newLine();
			writer.close();

			return;

		} catch (IOException e) {
			this.getLogger().severe("Could not write to 'Transaction Log.txt'!");
			e.printStackTrace();
		}
		return; // If we get here, it didn't work
	}

	private void setupQueueFile() {
		if (queueFile == null) {
			queueFile = new File(getDataFolder(), "Queue List.yml");
		}
		queue = YamlConfiguration.loadConfiguration(queueFile);
	}

	private FileConfiguration getQueueFile() {
		if (queue == null) {
			setupQueueFile();
		}
		return queue;
	}

	public void saveQueueFile() {
		if (queue == null || queueFile == null) {
			return;
		}
		queue.options().copyDefaults(true);
		try {
			getQueueFile().save(queueFile);
		} catch (Exception e) {
			getLogger().severe("Could not save to 'Queue List.yml'!");
			e.printStackTrace();
		}
	}

	private String stripLeadingSlash(String str) {
		// Removes leading "/" if one exists
		if (str.startsWith("/")) {
			return str.substring(1); // Returns everything beyond 1 character in
		} else {
			return str;
		}
	}
	
	private String mergeRemainingArgs(String[] args, int argsToSkip) {
		String mergedArgs = new String();
		for (int i = argsToSkip; i < args.length; i++) {
			mergedArgs += args[i]; 
			if (i < args.length - 1) {
				mergedArgs += " "; // Avoid adding space after final entry
			}
		}
		return mergedArgs;
	}
	
}
