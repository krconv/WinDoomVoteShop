package me.krconv.WinDoomVoteShop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class WinDoomVoteShop extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Minecraft");
	public static WinDoomVoteShop plugin;
	public FileConfiguration queue = null;
	public File queueFile = null;
	public File logFile;
	PluginDescriptionFile pdfFile; // Moved declaration up to make it accessible in all methods

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		//this.getLogger().info(pdfFile.getName() + " has been disabled!");
		this.getLogger().info(pdfFile.getName() + " has been disabled!");
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(
				new MyPlayerListener(this), this);
		this.setupQueueFile();
		this.saveQueueFile();
		this.setupLogFile();
		//PluginDescriptionFile pdfFile = this.getDescription(); Moved declaration up to make it accessible in all methods
		pdfFile = this.getDescription();
		//this.getLogger().info(pdfFile.getName() + " v" + pdfFile.getVersion() + " has been enabled!");
		this.getLogger().info(pdfFile.getName() + " v" + pdfFile.getVersion() + " has been enabled!");
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
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
			if (args.length > 2) {
				String commandToRun = stripLeadingSlash(intoCommand(args, 2));
				if (getServer().getPlayer(args[1]) != null) {
					this.getLogger().info("Executed command for Player: " + args[1] + ", Command: " + commandToRun);
					sender.sendMessage("Executed");
					getServer().dispatchCommand(getServer().getConsoleSender(), commandToRun);
					writeLogFile("Executed command for Player:" + args[1] + " Command:" + commandToRun);
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
	
	private String intoCommand(String[] args, int preCommand) {
		String command = new String();
		for (int i = preCommand; i < args.length; i++) {
			command += args[i]; 
			if (i < args.length - 1) {
				command += " "; // Avoid adding space after final entry
			}
		}
		return command;
	}
}
