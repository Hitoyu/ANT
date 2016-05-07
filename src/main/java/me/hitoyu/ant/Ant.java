package me.hitoyu.ant;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.hitoyu.hitocore.hitocore;

public class Ant extends JavaPlugin implements Listener {
	static Map<String, Api.Language> langs = new ConcurrentHashMap<String, Api.Language>();
	private static boolean connected = false;
	private static boolean uuid = false;
	private int debug = 0;
	static Api.Language consoleLang;
	static Api.Language defaultLang;
	private static int languages = 0;
	String noPermission = "You don't have permission for this.";
	private static int conCheckTask;
	String plugin = "ant";
	static String plugins = "ant";
	
	static String pluginVer = null;
	
	// Easier logging mechanism
	Logger log = null;
	static Logger logs = null;
	
	// HitoCore integration part one
	hitocore hc = null;
	static hitocore hcs = null;
	
	@Override
	public void onEnable() {
		pluginLoad();
		// Allows event handlers to work
		Bukkit.getPluginManager().registerEvents(this, this);
		pluginVer = this.getDescription().getVersion();
		log.info("ANT (Another Translator) enabled");
	}

	@Override
	public void onDisable() {
		saveData();
		log.info("ANT (Another Translator) disabled");
	}

	void pluginLoad() { // Also handles reloading
		// Variable reset
		langs = null;
		connected = false;
		uuid = false;
		debug = 0;
		consoleLang = null;
		defaultLang = null;
		languages = 0;
		conCheckTask = 0;
		pluginVer = null;
		log = null;
		logs = null;
		hc = null;
		hcs = null;
		
		// Load procedure
		log = this.getLogger();
		logs = log;
		
		// HitoCore integration part two
		hc = (hitocore) Bukkit.getPluginManager().getPlugin("HitoCore");
		if(!(hc == null)) {
			hcs = hc;
			logs.info("Hooked into " + hcs);
		}
		// Creates a config file if there is none
		saveDefaultConfig();

		// Reloads config file, incase there is one and changes were made
		reloadConfig();

		readAndSetDebug();
		// Command registering
		this.getCommand("setlang").setExecutor(new CommandExec());
		this.getCommand("languages").setExecutor(new CommandExec());
		this.getCommand("getlang").setExecutor(new CommandExec());
		this.getCommand("ant").setExecutor(new CommandExec());
		this.getCommand("say").setExecutor(new Command_Say());

		// Counting available languages
		for (@SuppressWarnings("unused") Api.Language lang : Api.Language.values()) {
			languages++;
		}

		// Debug warning
		if (isDebugEnabled() == true) {
			if (isAdvancedDebugEnabled() == true) {
				log.warning("You have advanced debug mode enabled. This may produce a lot of console spam.");
			} else {
				log.warning("You have debug mode enabled. This may produce some minor console spam.");
			}
			log.warning("Debug mode is used to help find and fix bugs, if you aren't doing either of those I recommend turning debug off. Disable it by setting 'debug' in the config to 0.");
			log.warning("Example debug: ");
			hcs.debugCall("Level 1", null, false);
			hcs.debugCall("Level 2", null, true);
		}

		setStorage(); // Will the plugin use UUIDs or Usernames?
		loadData(); // Load data from ANT_data.dat

		log.info("Loading config");
		consoleLang = Api.getLanguage(getConfig().getString("console-lang"));
		defaultLang = Api.getLanguage(getConfig().getString("default-lang"));
		
		if (consoleLang == null) {
			getConfig().set("console-lang", "English");
			consoleLang = Api.Language.ENGLISH;
		}

		if (defaultLang == null) {
			getConfig().set("default-lang", "English");
			defaultLang = Api.Language.ENGLISH;
		}

		hcs.debugCall("using UUIDs", uuid, false);
		hcs.debugCall("consoleLang", consoleLang, false);
		hcs.debugCall("defaultLang", defaultLang, false);

		// Check first connection. The rest will be done when players are
		// online, every five minutes.
		if (setConnected(connectionCheck()) == true) {
			log.info("Plugin can connect to translation API.");
		} else {
			log.info("Plugin cannot connect to translation API.");
		}

	}

	static void readAndSetDebug() {
		Ant Ant = (Ant)Bukkit.getPluginManager().getPlugin("ANT");

		Ant.debug = Ant.getConfig().getInt("debug");
		if (Ant.debug > 2) {
			Ant.debug = 2;
		} else if (Ant.debug < 0) {
			Ant.debug = 0;
		}
	}

	private static void startConnectionChecking() {
		logs.info("Connection checking started");

		// Connection checking and null checking
		conCheckTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Bukkit.getServer().getPluginManager().getPlugin("ANT"), new Runnable() {
			public void run() {
				if (setConnected(connectionCheck()) == true) {
					logs.info("Connected to the translation API.");
				} else {
					logs.info("Cannot connect to the translation API. Trying again in five minutes.");
				}

				// Null checking
				for (Player target : Bukkit.getOnlinePlayers()) {
					String targetName = hcs.returnName(target.getName());
					if (!langs.containsKey(targetName) || langs.get(targetName) == null) {
						nullCheck(target);
					}
				}
			}
		}, 20L, 6000L); // five minutes, with a start delay of one second.
	}

	private static boolean setConnected(boolean connected) {
		if (Ant.connected == connected) {
			return connected;
		}
		Ant.connected = connected;
		Ant ant = (Ant)Bukkit.getPluginManager().getPlugin("ANT");
		ant.hc.debugCall("are we connected?", connected, false);
		return connected;
	}

	private static boolean connectionCheck() {		
		String res = Api.getTranslation("unas gatos", Api.Language.SPANISH, Api.Language.ENGLISH);
		hcs.debugCall("Value of result", res, true);
		if(res == null) {
			return false;
		}
		
		String callback = "some cats";
		if ((res).equalsIgnoreCase(callback)) {
			return true;
		}
		return false;
	}

	private void setStorage() {
		if (hc.usingUUIDs() == true) {
			Ant.uuid = true;
			log.info("Using UUIDs (1.7.0 and newer)");
		} else {
			Ant.uuid = false;
			log.info("Using usernames (1.6.4 or older)");
		}
		hc.debugCall("Minecraft version", hc.getServerVersion(true), false);
	}

	private static boolean saveData() {
		Ant Ant = (Ant) Bukkit.getPluginManager().getPlugin("ANT");
		File ANT_data = new File(Ant.getDataFolder(), "ANT_data.dat");
		try {
			if (ANT_data.exists()) {
				ANT_data.delete();
			}
			ANT_data.createNewFile();
			SLAPI.save(langs, ANT_data.getPath());
			hcs.debugCall("Data save successful", null, false);
			return true;
		} catch (Exception ex) {
			hcs.debugCall("Data save failed", "See below stacktrace", false);
			ex.printStackTrace();
			return false;
		}
	}

	private static boolean loadData() {
		Ant Ant = (Ant) Bukkit.getPluginManager().getPlugin("ANT");
		File ANT_data = new File(Ant.getDataFolder(), "ANT_data.dat");
		try {
			if (ANT_data.exists()) {
				langs = SLAPI.load(ANT_data.getPath());
			} else {
				ANT_data.createNewFile();
			}
			hcs.debugCall("Data load successful", null, false);
			return true;
		} catch (Exception ex) {
			hcs.debugCall("Data load failed", "See below stacktrace", false);
			ex.printStackTrace();
			return false;
		}
	}

	static String getLocale(Player p) {
		Object ep = null;
		try {
			ep = getMethod("getHandle", p.getClass()).invoke(p, (Object[]) null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (ep == null) {
			return null;
		}

		Field f = null;
		try {
			f = ep.getClass().getDeclaredField("locale");
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (f == null) {
			return null;
		}

		f.setAccessible(true);
		String language = null;
		try {
			language = (String) f.get(ep);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		if (language == null) {
			return null;
		}

		return language;
	}

	private static Method getMethod(String name, Class<?> clazz) {
		for (Method m : clazz.getDeclaredMethods()) {
			if (m.getName().equals(name))
				return m;
		}
		return null;
	}

	// setLang methods

	static boolean setLang(Player player, String lang) {
		return setLang(player.getName(), lang);
	}
	
	static boolean setLang(Player player, Api.Language lang) {
		return setLang(player.getName(), lang);
	}

	static boolean setLang(String playerName, String lang) {
		return setLang(playerName, Api.getLanguage(lang));
	}
	
	static boolean setLang(String playerName, Api.Language lang) {
		String name = hcs.returnName(playerName);
		hcs.debugCall("player name|uuid", name, true);
		hcs.debugCall("player language", lang, true);
		
		if(lang == null || name == null) {
			return false;
		}
		
		if (langs.containsKey(name)) {
			if ((Api.Language)langs.get(name) == lang) {
				return false;
			}
		}
		
		langs.put(name, lang);
		saveData();
		
		return true;
	}

	private static void nullCheck(Player player) {
		if (Api.getLanguageOf(player) == null) {
			String endRes = Api.getLanguage(getLocale(player).split("_")[0]).getName();

			hcs.debugCall("player locale", getLocale(player), true);
			hcs.debugCall("player language from locale", endRes, true);

			if (endRes == null) {
				endRes = Api.returnDefaultLanguage().getName();
			}

			Api.Language playerLang = Api.getLanguage(endRes);

			setLang(player.getName(), playerLang);
			player.sendMessage("\u00A7aYour language was changed from N/A to " + playerLang.getPhonetic() + " (" + playerLang.getName() +  ")");
			hcs.debugCall("Fixed null language of " + player.getName(), null, false);
		}
	}

	// Event handlers of sorts
	// Translation triggers and such
	@EventHandler
	void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		hc.debugCall("Player joined", player.getName(), true);
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				int online = hc.getNumberOnline();
				if(online == 0) {
					return;
				}
				
				nullCheck(player);
				
				if (online == 1) {
					startConnectionChecking();
				}
			}
		}, 20L);
	}

	@EventHandler
	void onPlayerQuit(PlayerQuitEvent event) {
		hc.debugCall("Player disconnected", event.getPlayer().getName(), true);

		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				if (hc.getNumberOnline() == 0) {
					saveData();
					Bukkit.getScheduler().cancelTask(conCheckTask);
					log.info("Connection checking stopped as no players are online.");
				}
			}
		}, 20L);
	}

	@EventHandler
	void onPlayerChat(AsyncPlayerChatEvent event) {		
		final String playerName = event.getPlayer().getName();
		final String message = event.getMessage();
		final Api.Language senderLang = Api.getLanguageOf(playerName);
		
		if(senderLang == consoleLang && hc.getNumberOnline() == 1) {
			return;
		}

		hc.debugCall("Chat event from " + playerName, message, true);
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				String prefix = "" + ChatColor.BOLD + ChatColor.AQUA + playerName + " [From " + senderLang.getName() + "] " + ChatColor.RESET + ChatColor.GREEN;
				Api.broadcastTranslation(prefix,message,playerName,senderLang);
			}
		}, 5L);
	}

	static boolean isConnected() {
		return Ant.connected;
	}

	static boolean isDebugEnabled() {
		Ant Ant = (Ant) Bukkit.getPluginManager().getPlugin("ANT");
		if (Ant.debug > 0) {
			return true;
		}
		return false;
	}

	static boolean isUsingUUIDs() {
		if (Ant.uuid == true) {
			return true;
		}
		return false;
	}

	static boolean isAdvancedDebugEnabled() {
		Ant Ant = (Ant) Bukkit.getPluginManager().getPlugin("ANT");
		if (Ant.debug > 1) {
			return true;
		}
		return false;
	}

	static int availableLanguages() {
		return Ant.languages;
	}
}