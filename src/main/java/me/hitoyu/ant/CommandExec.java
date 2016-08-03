package me.hitoyu.ant;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import me.hitoyu.ant.Ant;

public class CommandExec implements CommandExecutor {
	Ant ant = (Ant) Bukkit.getPluginManager().getPlugin("ANT");

	@SuppressWarnings({ "deprecation", "static-access" })
	@Override
	public boolean onCommand(CommandSender player, Command cmd, String label, String[] args) {
		ant.hc.debugCall("player " + player.getName() + " ran command " + label + " " + Arrays.toString(args).replace(",", ""), null, true);

		if (label.equalsIgnoreCase("setlang")) {
			// If no permission
			if (!(player.hasPermission("ant.setlang"))) {
				return false;
			}

			if(args.length < 2 && !(player instanceof Player)) {
				player.sendMessage("You can only change the language of another player using this command from console.");
				player.sendMessage("Change someone elses language by using /setlang [player] [language]");
				return true;
			}
	
			if(args.length == 0) {
				player.sendMessage(ChatColor.RED + "Language required!");
				return true;
			}

			if (args.length == 1) {
				Api.Language lang = Api.getLanguage(args[0]);
				ant.hc.debugCall("Target lang", lang, true);
				
				if (lang == null || ant.setLang((Player) player, lang) == false) {
					player.sendMessage(ChatColor.RED + "Could not change language. Either the language doesn't exist or your language is already set to it.");
					return true;
				}
				
				player.sendMessage(ChatColor.GREEN + "Your language was set to " + lang.getPhonetic());
				return true;
			}

			if (!(player.hasPermission("ant.setlang.others"))) {
				player.sendMessage(ChatColor.RED + ant.noPermission);
				return false;
			}
			
			String playerName = "";
			Player target = Bukkit.getPlayer(ant.hc.returnName(args[0]));
			Api.Language lang = Api.getLanguage(args[1]);
			
			ant.hc.debugCall("Name of target", target.getName(), true);
			ant.hc.debugCall("Target lang", lang.getName(), true);

			if(lang == null || ant.setLang(target, args[1]) == false) {
				player.sendMessage(ChatColor.RED
						+ "Could not change language. Either the language doesn't exist or their language is already set to it.");
				return true;
			}
			
			player.sendMessage(ChatColor.YELLOW + playerName + ChatColor.GREEN + "'s language was set to "
					+ lang.getPhonetic() + " (" + lang.getName() + ")");
			
			target.sendMessage(ChatColor.GREEN + "Your language was changed to " + lang.getPhonetic() + " (" + lang.getName() + ") by " + ChatColor.YELLOW + player.getName());
			return true;
		}

		if (label.equalsIgnoreCase("getlang")) {
			if (!(player.hasPermission("ant.getlang"))) {
				player.sendMessage(ChatColor.RED + ant.noPermission);
				return false;
			}
			if (args.length == 0) {
				player.sendMessage(ChatColor.RED + "Playername required!");
				return false;
			}

			if(args[0].equalsIgnoreCase("console") || args[0].equalsIgnoreCase("server") || args[0].equalsIgnoreCase("host")) {
				player.sendMessage(ChatColor.YELLOW + args[0] + ChatColor.GREEN + "'s language is set to " + Api.returnConsoleLanguage().getPhonetic() + " (" + Api.returnConsoleLanguage().getName() + ")");
				return true;
			}
			
			String antId = ant.hc.returnName(args[0]);
			ant.hc.debugCall("Name of sender", player.getName(), true);
			ant.hc.debugCall("Name/UUID of target", antId, true);

			Api.Language lang = Api.getLanguageOf(args[0]);
			if (lang == null) {
				player.sendMessage(ChatColor.RED + "Make sure " + ChatColor.YELLOW + args[0] + ChatColor.RED
						+ " has played here before.");
				if (ant.hc.usingUUIDs() == true) {
					player.sendMessage(ChatColor.RED
							+ "Since we're using Minecraft 1.7.0 or higher you'll have to make sure they're online, otherwise we can't get their UUID.");
				}
				return false;
			}
			
			String playerName = "";
			if (ant.hc.usingUUIDs() == true) {
				playerName = Bukkit.getPlayer(UUID.fromString(antId)).getName();
			} else {
				playerName = Bukkit.getPlayer(antId).getName();
			}
			player.sendMessage(ChatColor.YELLOW + playerName + ChatColor.GREEN + "'s language is set to " + lang.getPhonetic() + " (" + lang.getName() + ")");
			return true;
			
		}

		if (label.equalsIgnoreCase("languages")) {
			if (!(player.hasPermission("ant.languages"))) {
				player.sendMessage(ChatColor.RED + ant.noPermission);
				return false;
			}
			int pageNum = 1;
			int loop = 0;
			int langsPerPage = 8;
			int availablePages = (int) Math.floor((ant.availableLanguages() / langsPerPage)) + 1;
			if (!(args.length == 0)) {
				try {
					pageNum = Integer.parseInt(args[0]);
				} catch (Exception ignoreMe) {
					pageNum = 0;
				}

				if (pageNum > availablePages) {
					pageNum = availablePages;
				} else if (pageNum < 1) {
					pageNum = 1;
				}
			}
			player.sendMessage(ChatColor.GOLD + "Languages: Page " + ChatColor.RED + pageNum + ChatColor.GOLD + " of " + ChatColor.RED + availablePages + " (" + ant.availableLanguages() + " total languages)");
			for (Api.Language lang : Api.Language.values()) {
				if (loop > (((pageNum - 1) * langsPerPage) - 1) && loop < ((pageNum) * langsPerPage)) {
					player.sendMessage("\u00A7aLanguage: \u00A7b" + lang.getName() + "\u00A7a | ISO2: \u00A7b" + lang.getISO2() + "\u00A7a | ISO3: \u00A7b" + lang.getISO3() + "\u00A7a | Phonetic: \u00A7b" + lang.getPhonetic());
				}
				loop++;
			}
			return true;
		}

		if (label.equalsIgnoreCase("ant")) {
			if (args.length < 1) {
				if(!player.hasPermission("ant.main")) {
					player.sendMessage(ChatColor.RED + "You do not have permission to do this.");
					return true;
				}
				player.sendMessage(ChatColor.GOLD + "ANT (Another Translator) Help:");
				int options = 0;
				if (player.hasPermission("ant.reload")) {
					options++;
					player.sendMessage(ChatColor.RED + "/ant reload " + ChatColor.GREEN + " | Reloads the plugin");
				}

				if (player.hasPermission("ant.version")) {
					options++;
					player.sendMessage(ChatColor.RED + "/ant version" + ChatColor.GREEN 
							+ " | Display the version of this plugin.");
				}
				
				if (player.hasPermission("ant.setdebug")) {
					options++;
					player.sendMessage(ChatColor.RED + "/ant debug [0, 1, 2]" + ChatColor.GREEN
							+ " | Sets debug level. USE WITH CAUTION");
				}

				if (player.hasPermission("ant.setlang")) {
					options++;
					player.sendMessage(
							ChatColor.RED + "/setlang [language]" + ChatColor.GREEN + " | Change your language");
				}

				if (player.hasPermission("ant.setlang.others")) {
					options++;
					player.sendMessage(ChatColor.RED + "/setlang [player] [language]" + ChatColor.GREEN
							+ " | Change another player's language");
				}

				if (player.hasPermission("ant.getlang")) {
					options++;
					player.sendMessage(ChatColor.RED + "/getlang [player]" + ChatColor.GREEN
							+ " | See the language of another player");
				}

				if (player.hasPermission("ant.languages")) {
					options++;
					player.sendMessage(ChatColor.RED + "/languages" 
					+ ChatColor.GREEN + " | View the available languages");
				}
				
				if (options == 0) {
					player.sendMessage(ChatColor.RED + "You have no permissions with this plugin.");
				}
				return false;
			}

			if(player.hasPermission("ant.version")) {
				if(args[0].equalsIgnoreCase("version")) {
					String version = ant.getConfig().getString("version");
					player.sendMessage("ANT is running v" + version);
				}
			}
			
			if (player.hasPermission("ant.reload")) {
				if (args[0].equalsIgnoreCase("reload")) {
					ant.pluginLoad();
					return true;
				}
			}

			if (player.hasPermission("ant.setdebug")) {
				if (args[0].equalsIgnoreCase("debug")) {
					if (args.length < 2) {
						player.sendMessage(ChatColor.RED + "You need to provide a debug level. 0, 1, or 2.");
						return false;
					}

					String num = args[1];

					try {
						int level = Integer.parseInt(num);
						changeDebugLevel(level);
						String prefix = ChatColor.GREEN + "Debug level changed to ";
						String mode = "";
						if (level == 0) {
							mode = "disabled\u00A7a.";
						} else if (level == 1) {
							mode = "normal\u00A7a.";
						} else {
							mode = "advanced\u00A7a.";
						}
						player.sendMessage(prefix + ChatColor.YELLOW + mode);
						return true;
					} catch (Exception ignore) {
						player.sendMessage(ChatColor.RED + "You need to enter a number.");
						return false;
					}

				}
			}
		}
		return false;
	}
	
	@SuppressWarnings("static-access")
	private void changeDebugLevel(int level) {
		if (level > 2) {
			level = 2;
		}
		if (level < 0) {
			level = 0;
		}
		
		ant.getConfig().set("debug", level);
		ant.saveConfig();
		ant.reloadConfig();
		ant.readAndSetDebug();

		if (Ant.isDebugEnabled() == true) {
			if (Ant.isAdvancedDebugEnabled() == true) {
				ant.log.warning("You have advanced debug mode enabled. This may produce a lot of console spam.");
			} else {
				ant.log.warning("You have debug mode enabled. This may produce some minor console spam.");
			}
			ant.log.warning("Debug mode is used to help find and fix bugs, if you aren't doing either of those I recommend turning debug off. Disable it by setting 'debug' in the config to 0.");
		}
	}
}