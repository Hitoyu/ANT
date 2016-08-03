package me.hitoyu.ant;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Api {
	static String getTranslation(String message, Api.Language fromLanguage, Api.Language toLanguage) {	
		Ant.hcs.debugCall("fromLang",fromLanguage.getName(),true);
		Ant.hcs.debugCall("toLang",toLanguage.getName(),true);
		
		if(toLanguage == null || fromLanguage == null) {
			return null;
		}

		Map<Integer, Api.Language> toTrans = new HashMap<Integer,Api.Language>();
		toTrans.put(0, toLanguage);
		Map<Api.Language, String> result = Translator.getTranslation(message, fromLanguage, toTrans, 1);

		String output = (String)result.get(toLanguage);
		return output;
	}
	/**
	 * Translates a message from one language to another.  
	 * 
	 * 
	 * @param message The message to be translated.
	 * @param fromLanguageSTR Name of language that the message is in.
	 * @param toLanguageSTR Name of the language you want the message translated to.
	 * @return Translated message.
	 */
	public static String getTranslation(String message, String fromLanguageSTR, String toLanguageSTR) {
		if(Ant.isConnected() == false) {
			return null;
		}
		Api.Language fromLanguage = getLanguage(fromLanguageSTR);
		Api.Language toLanguage = getLanguage(toLanguageSTR);
		
		String output = getTranslation(message, fromLanguage, toLanguage);
		return output;
	}

	// input, from what language
	public static void broadcastTranslation(final String prefix, final String message, final String senderName, final Api.Language senderLang) {
		if (Ant.isConnected() == false || Ant.hcs.getNumberOnline() == 0) {
			Ant.hcs.debugCall("Conditions false", "Connected: " + Ant.isConnected() + " | online: " + Ant.hcs.getNumberOnline(), true);
			return;
		}
		
		Ant.hcs.debugCall("senderName", senderName, false);
		Ant.hcs.debugCall("senderLang", senderLang, false);
		
		Map<Integer, Api.Language> languages = new ConcurrentHashMap<Integer, Api.Language>();
		Map<Api.Language, String> playerT = new ConcurrentHashMap<Api.Language, String>();
		int x = 0;
		int debugCounter = 0;
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			Api.Language playerLang = getLanguageOf(player);
			Ant.hcs.debugCall("language of " + player.getName(), playerLang, true);
			Ant.hcs.debugCall("language of sender", senderLang, true);
			if(!(playerLang.getName().equalsIgnoreCase(senderLang.getName())) && playerLang != Api.Language.NONE) {
				if(!languages.containsKey(playerLang)) {
					languages.put(x, playerLang);
					Ant.hcs.debugCall("Language added to index at " + x, playerLang, true);
					x++;
				}
			}
			if(!(playerLang == senderLang)
					|| !(playerLang == Api.Language.NONE) 
					|| !((playerLang == senderLang && (playerLang == Api.Language.NONE)))) {
				playerT.put(playerLang, player.getName());
				
			}
			debugCounter++;
		}
		
		boolean tcc = false;
		if (!(languages.containsKey(Ant.consoleLang)) && !(Ant.consoleLang == senderLang)) {
			languages.put(x, Ant.consoleLang);
			debugCounter++;
			x++;
			tcc = true;
		}
		final boolean tc = tcc;
		
		Ant.hcs.debugCall("Total languages", x, false);
		Ant.hcs.debugCall("Total checks", debugCounter, false);
		
		if(languages.isEmpty()) {
			return;
		}
		
		final Map<Api.Language, String> trans = Translator.getTranslation(message, senderLang, languages, x);
		if(trans.isEmpty()) {
			return;
		}
		
		final Map<Api.Language,String> players = playerT;
		
		new Thread(new Runnable() {
			public void run() {
				for(Entry<Api.Language, String> entry : players.entrySet()) {
					@SuppressWarnings("deprecation") Player player = Bukkit.getPlayer(entry.getValue());
					if(!(player.getName().equalsIgnoreCase(senderName))) {
						Api.Language lang = entry.getKey();
						String output = (String)trans.get(lang);
						if(!output.replace(" ", "").equalsIgnoreCase(message)) {
							player.sendMessage(prefix + output);
						}
					}
				}
				
				if (tc == true) {
					String output = (String)trans.get(Api.returnConsoleLanguage());
					if (!(output.replace(" ", "").equalsIgnoreCase(message.replace(" ", "")))) {
						Ant.logs.info(prefix + output);
					}
				}
			}
		}).start();
		// output to server
	}

	public static Api.Language getLanguageOf(String playerName) {
		if (playerName == "CONSOLE" || playerName == "~CONSOLE~") {
			return Ant.consoleLang;
		}

		String name = Ant.hcs.returnName(playerName);
		if (name == null) {
			return null;
		}
		Ant.hcs.debugCall("player name|uuid", name, true);
		
		if (Ant.langs.containsKey(name) == false) {
			return null;
		}
		Api.Language lang = (Api.Language) Ant.langs.get(name);
		Ant.hcs.debugCall("player language", lang, false);
		return lang;
	}

	public static Api.Language getLanguageOf(Player player) {
		return getLanguageOf(player.getName());
	}

	public static Api.Language returnConsoleLanguage() {
		return Ant.consoleLang;
	}

	public static Api.Language returnDefaultLanguage() {
		return Ant.defaultLang;
	}

	public static Api.Language getLanguage(String input) {
		for (Api.Language lang : Api.Language.values()) {
			String[] name = { "", "", "", "" };
			name[0] = lang.getName();
			name[1] = lang.getISO2();
			name[2] = lang.getISO3();
			name[3] = lang.getPhonetic();
			Ant.hcs.debugCall("Compare language names to " + input, "Language: " + lang.getName() + " | ISO2: " + lang.getISO2() + " | ISO3: " + lang.getISO3() + " | Phonetic: " + lang.getPhonetic(), true);

			for (int x = 0; x < 4; x++) {
				if (name[x].equalsIgnoreCase(input)) {
					Ant.hcs.debugCall("Language found", lang.getName(), false);
					return lang;
				}
			}
		}		
		return null;
	}
	
	public static boolean canHearMe() {
		return true;
	}
	
	public static String getPluginVersion() {
		return Ant.pluginVer;
	}
	
	public enum Language {
	    AUTO("Auto", "auto", "auto", "auto"),
	    NONE("off", "", "disable", "none"),
	    AFRIKAANS("Afrikaans", "af", "aft","Afrikaans"),
	    ALBANIAN("Albanian", "sq", "sqi", "shqiptar"),
	    AMHARIC("Amharic", "am", "anh","\u12A0\u121B\u122D\u129B"),
	    ARABIC("Arabic", "ar", "ara","\u0627\u0644\u0639\u0631\u0628\u064A\u0629"),
	    ARMENIAN("Armenian", "hy", "hye","\u0570c\u0575\u0565\u0580\u0565\u0576"),
	    AZERBAIJANI("Azerbaijani", "az", "aze","Az\u0259rbaycan"),
	    BASQUE("Basque", "eu", "eus", "Euskal"),
	    BELARUSIAN("Belarusian", "be", "bel","\u0431\u0435\u043B\u0430\u0440\u0443\u0441\u043A\u0430\u044F"),
	    BENGALI("Bengali", "bn", "ben","\u09AC\u09BE\u0999\u09CD\u0997\u09BE\u09B2\u09C0"),
	    BOSNIAN("Bosnian", "bs", "bos","Bosanski"),
	    BULGARIAN("Bulgarian", "bg", "bul","\u0431\u044A\u043B\u0433\u0430\u0440\u0441\u043A\u0438"),
	    CATALAN("Catalan", "ca", "cat","català"),
	    CEBUANO("Cebuano", "","DEB", "Cebuano"),
	    CHICHEWA("Chichewa", "ny","nya","Chichewa"),
	    CHINESE_SIMPLIFIED("Chinese-CN","zh-CN","zho-CN","\u4E2D\u56FD"),CHINESE_TRADITIONAL("Chinese-TW","zh-TW","zho-TW","\u4E2D\u570B"),
	    CORSICAN("Corsican","co","cos","corsu"),
	    CROATIAN("Croatian","hr","hrv","hrvatski"),
	    CZECH("Czech","cs","cse","ceština"),
	    DANISH("Danish","da","dan","dansk"),
	    DUTCH("Dutch","nl","nld","Nederlands"),
	    ENGLISH("English","en","eng","English"),
	    ESPERANTO("Esperanto","eo","epo","Esperanto"),
	    ESTONIAN("Estonian","et","est","eesti"),
	    FILIPINO("Filipino","fil","fil","Pilipino"),
	    FINNISH("Finnish","fi","fin","suomi"),
	    FRENCH("French","fr","fre","français"),
	    FRISIAN("Frisian","fy","fry","Frysk"),
	    GALICIAN("Galician","gl","glg","galega"),
	    GEORGIAN("Georgian","ka","kat","\u10E5\u10D0\u10E0\u10D7\u10E3\u10DA\u10D8"),
	    GERMAN("German","de","deu","Deutsch"),
	    GREEK("Greek","el","ell","\u03B5\u03BB\u03BB\u03B7\u03BD\u03B9\u03BA\u03AC"),
	    GUJARATI("Gujarati","gu","guj","\u0A97\u0AC1\u0A9C\u0AB0\u0ABE\u0AA4\u0AC0"),
	    HAITIAN_CREOLE("Haitian Creole","ht","hat","kreyòl ayisyen"),
	    HAUSA("Hausa","ha","hau","Hausa"),
	    HAWAIIAN("Hawaiian","","haw","Hawai'i"),
	    HEBREW("Hebrew","he","heb","\u05E2\u05D1\u05E8\u05D9\u05EA"),
	    HINDI("Hindi","hi","hin","\u0939\u093F\u0928\u094D\u0926\u0940"),
	    HMONG("Hmong","","hmn","hmoob"),
	    HUNGARIAN("Hungarian","hu","hun","magyar"),
	    ICELANDIC("Icelandic","is","isl","Icelandic"),
	    IGBO("Igbo","ig","ibo","Igbo"),
	    INDONESIAN("Indonesian","id","ind","Indonesia"),
	    IRISH("Irish","ga","gle","Gaeilge"),
	    ITALIAN("Italian","it","ita","Italiano"),
	    JAPANESE("Japanese","ja","jpn","\u65E5\u672C\u306E"),
	    JAVANESE("Javanese","jv","jav","Jawa"),
	    KANNADA("Kannada","kn","kan","\u0C95\u0CA8\u0CCD\u0CA8\u0CA1"),
	    KAZAKH("Kazakh","kk","kaz","\u049A\u0430\u0437\u0430\u049B"),
	    KHMER("Khmer","km","khm","\u1781\u17D2\u1798\u17C2\u179A"),
	    KOREAN("Korean","ko","kor","\uD55C\uAD6D\uC758"),
	    KURDISH("Kurdish","ku","kur","\u0643\u0648\u0631\u062F\u06CC\u200E"),
	    KYRGYZ("Kyrgyz","","","\u041A\u044B\u0440\u0433\u044B\u0437\u0447\u0430"),
	    LAO("Lao","la","lao","\u0EA5\u0EB2\u0EA7"),
	    LATIN("Latin","la","lat","Latine"),
	    LATVIAN("Latvian","lv","lav","Latvijas"),
	    LITHUANIAN("Lithuanian","lt","lit","Lietuvos"),
	    LUXEMBOURGISH("Luxembourgish","lb","ltz","L\u00EBtzebuergesch"),
	    MACEDONIAN("Macedonian","mk","mkd","\u043C\u0430\u043A\u0435\u0434\u043E\u043D\u0441\u043A\u0438"),
	    MALAGASY("Malagasy","mg","mlg","Malagasy"),
	    MALAY("Malay","ms","msa","Melayu"),
	    MALAYALAM("Malayalam","ml","mal","\u0D2E\u0D32\u0D2F\u0D3E\u0D33\u0D02"),
	    MALTESE("Maltese","mt","mlt","Malti"),
	    MAORI("Maori","mi","mri","Maori"),
	    MARATHI("Marathi","mr","mar","\u092E\u0930\u093E\u0920\u0940"),
	    MYANMAR("Myanmar","my","mya","\u1019\u103C\u1014\u103A\u1019\u102C"),
	    MONGOLIAN("Mongolian","mn","mon","\u041C\u043E\u043D\u0433\u043E\u043B"),
	    NEPALI("Nepali","ne","nep","\u0928\u0947\u092A\u093E\u0932\u0940"),
	    NORWEGIAN("Norwegian","no","nor","Norsk"),
	    PASHTO("Pashto","ps","pus","\u067E\u069A\u062A\u0648"),
	    PERSIAN("Persian","fa","fas","\u0641\u0627\u0631\u0633\u06CC"),
	    POLISH("Polish","pl","pol","polski"),
	    PORTUGUESE("Portuguese","pt","por","português"),
	    PUNJABI("Punjabi","pa","pan","\u0A2A\u0A70\u0A1C\u0A3E\u0A2C\u0A40 \u0A26"),
	    ROMANIAN("Romanian","ro","ron","român"),
	    RUSSIAN("Russian","ru","rus","\u0440\u0443\u0441\u0441\u043A\u0438\u0439"),
	    SAMOAN("Samoan","sm","smo","Samoa"),
	    SCOTS_GAELIC("Scottish_Gaelic","gd","gla","Gàidhlig"),
	    SERBIAN("Serbian","sr","srp","\u0441\u0440\u043F\u0441\u043A\u0438"),
	    SESOTHO("Sesotho","st","sot","Sesotho"),
	    SHONA("Shona","sn","sna","Shona"),
	    SINDHI("Sindhi","sd","snd","\u0633\u0646\u068C\u064A"),
	    SINHALA("Sinhala","si","sin","\u0DC3\u0DD2\u0D82\u0DC4\u0DBD"),
	    SLOVAK("Slovak","sk","slk","slovenský"),
	    SLOVENIAN("Slovenian","sl","slv","slovenšcina"),
	    SOMALI("Somali","so","som","Somali"),
	    SPANISH("Spanish","es","spa","español"),
	    SUNDANESE("Sundanese","su","sun","\u1B98\u1B9E \u1B9E\u1BA5\u1B94\u1BAA\u1B93"),
	    SWAHILI("Swahili","sw","swa","Kiswahili"),
	    SWEDISH("Swedish","sv","swe","svenska"),
	    TAJIK("Tajik","tg","tgk","\u0422\u043E\u04B7\u0438\u043A\u0438\u0441\u0442\u043E\u043D"),
	    TAMIL("Tamil","ta","tam","\u0BA4\u0BAE\u0BBF\u0BB4\u0BCD"),
	    TELUGU("Telugu","te","tel","\u0C24\u0C46\u0C32\u0C41\u0C17\u0C41"),
	    THAI("Thai","th","tha","\u0E44\u0E17\u0E22"),
	    TURKISH("Turkish","tr","tur","Türk"),
	    UKRAINIAN("Ukrainian","uk","ukr","\u0423\u043A\u0440\u0430\u0457\u043D\u0441\u044C\u043A\u0438\u0439"),
	    URDU("Urdu","ur","urd","\u0627\u0631\u062F\u0648"),
	    UZBEK("Uzbek","uz","uzb","O'zbekiston"),
	    VIETNAMESE("Vietnamese","vi","vie","ti\u1EBFng Vi\u1EC7t"),
	    WELSH("Welsh","cy","cym","Cymraeg"),
	    XHOSA("Xhosa","xh","xho","Xhosa"),
	    YIDDISH("Yiddish","yi","yid","\u05D9\u05D9\u05B4\u05D3\u05D9\u05E9"),
	    YORUBA("Yoruba","yo","yor","Yoruba"),
	    ZULU("Zulu","zu","zul","zulu");
	        
	    private String lang;
	    private String ISO2;
	    private String ISO3;
	    private String phonetic;

	    Language(String lang, String ISO2, String ISO3, String phonetic) {
	        this.lang = lang;
	        this.ISO2 = ISO2;
	        this.ISO3 = ISO3;
	        this.phonetic = phonetic;
	    }

	    public String getName() {
	        return lang;
	    }

	    public String getISO2() {
	        return ISO2;
	    }

	    public String getISO3() {
	        return ISO3;
	    }

	    public String getPhonetic() {
	        return phonetic;
	    }
	}
}
