package me.hitoyu.ant;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.SayCommand;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
public class Command_Say implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender.hasPermission("bukkit.command.say"))) {
			Ant Ant = (Ant) Bukkit.getPluginManager().getPlugin("ANT");
			sender.sendMessage(Ant.noPermission.replace("&", "\u00A7"));
			return true;
		}

		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Too few parameters. " + ChatColor.GREEN + "/say " + ChatColor.RED + "<message>");
			return true;
		}

		final SayCommand SayCommand = new SayCommand();
		SayCommand.execute(sender, label, args);

		StringBuilder bmessage = new StringBuilder();
		if (args.length > 0) {
			bmessage.append(args[0]);
			for (int i = 1; i < args.length; i++) {
				bmessage.append(" ").append(args[i]);
			}
		}
		
		Boolean canTranslate = false;
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			Api.Language lang =  Api.getLanguageOf(Ant.hcs.returnName(p.getName()));
			if(!(lang == Ant.consoleLang)) {
				canTranslate = true;
			}
		}
		

		Api.Language senderLang;
		if (sender instanceof Player) {
			senderLang = Api.getLanguageOf(sender.getName());
		} else {
			senderLang = Api.returnConsoleLanguage();
		}
		
		if(Ant.hcs.getNumberOnline() == 0 || canTranslate == false || !(sender.hasPermission("ant.use"))) {
			return true;
		}
		
		String sender1 = sender.getName();

		String prefix = "" + ChatColor.BOLD + ChatColor.AQUA + sender1 + " [From " + senderLang.getName() + "] " + ChatColor.RESET + ChatColor.GREEN;
		Ant.hcs.debugCall("sender name", sender1, true);
		Ant.hcs.debugCall("sender lang", senderLang, true);
		Ant.hcs.debugCall("message", bmessage.toString(), true);
		Ant.hcs.debugCall("prefix", prefix, true);
		Api.broadcastTranslation(prefix, bmessage.toString(), sender1, senderLang);
		return true;
	}
}