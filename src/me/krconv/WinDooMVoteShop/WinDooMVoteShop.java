package me.krconv.WinDooMVoteShop;

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

public class WinDooMVoteShop extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Minecraft");
	public static WinDooMVoteShop plugin;
	public FileConfiguration queue = null;
	public File queueFile = null;
	public File logFile;

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " has been disabled!");
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(
				new MyPlayerListener(this), this);
		this.setupQueueFile();
		this.saveQueueFile();
		this.setupLogFile();
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " v" + pdfFile.getVersion()
				+ " has been enabled!");
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		if (!commandLabel.equalsIgnoreCase("voteshop")) {
			return false;
		}
		if (sender instanceof Player) {
			sender.sendMessage(ChatColor.RED
					+ "This command can only be run from console!");
			return false;
		}
		if (args[0].equalsIgnoreCase("ping")) {
			sender.sendMessage("Pong");
			return true;
		} else if (args[0].equalsIgnoreCase("command")) {
			String commandToRun = intoCommand(args, 2);
			if (getServer().getPlayer(args[1]) != null) {
				this.logger.info("[WinDooMVoteShop] Executed command for Player:" + args[1] + " Command:/" + commandToRun);
				sender.sendMessage("Executed");
				getServer().dispatchCommand(getServer().getConsoleSender(),
						commandToRun);
				writeLogFile("Executed command for Player:" + args[1] + " Command:" + commandToRun);
				return true;
			} else {
				this.logger.info("[WinDooMVoteShop] Queued command for Player:" + args[1] + " Command:/" + commandToRun);
				sender.sendMessage("Queued");
				List<String> queueList = null;
				if (queue.getStringList(args[1]) != null) {
					queueList = queue.getStringList(args[1]);
				}
				queueList.add(commandToRun);
				queue.getStringList(args[1]);
				queue.set(args[1], queueList);
				saveQueueFile();
				writeLogFile("Queued command for Player:" + args[1] + " Command:/" + commandToRun);
			}
		} else {
			return false;
		}
		return false;

	}
	public void setupLogFile() {
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
				this.logger
						.severe("[WinDooMVoteShope] Could not create 'Transaction Log.txt'!");
				e.printStackTrace();
				return;
			}
		}
	}

	public void writeLogFile(String stringLog) {
		String stringToWrite = new String(); // Formatted string to write
		Date timeStamp = new Date(); // TimeStamp of event (timeStamp = now)
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd"); // Date
																		// formatter
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss:SSS"); // Time
																			// formatter
		stringToWrite = "[" + sdfDate.format(timeStamp) + "] ["
				+ sdfTime.format(timeStamp) + "] - ";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					logFile, true));
			writer.write(stringToWrite + stringLog);
			writer.newLine();
			writer.close();

			return;

		} catch (IOException e) {
			this.logger
					.severe("[WinDooMVoteShop] Could not write to 'Transaction Log.txt'!");
			e.printStackTrace();
		}
		return; // If we get here, it didn't work
	}

	public void setupQueueFile() {
		if (queueFile == null) {
			queueFile = new File(getDataFolder(), "Queue List.yml");
		}
		queue = YamlConfiguration.loadConfiguration(queueFile);
	}

	public FileConfiguration getQueueFile() {
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
			getLogger().severe(
					"[WinDooMVoteShop] Could not save to 'Queue List.yml'!");
			e.printStackTrace();
		}
	}

	public String intoCommand(String[] args, int preCommand) {
		String command = new String();
		for (int i = preCommand; i < args.length; i++) {
			command += args[i] + " ";
		}
		return command;
	}
}
