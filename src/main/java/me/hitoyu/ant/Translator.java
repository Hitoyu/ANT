package me.hitoyu.ant;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Translator {
	private static String readURL(String url) {
		String output = null;
		try {
			URL getTrans = new URL(url);
			URLConnection conn = getTrans.openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:9.0.1) Gecko/20100101 Firefox/9.0.1");
			conn.setRequestProperty("Accept-Charset", "UTF-8");
			conn.setRequestProperty("charset", "UTF-8");
			conn.setReadTimeout(5000);
			BufferedReader readIn = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));
			String msgPostInput;
			while ((msgPostInput = readIn.readLine()) != null) {
				output = output + msgPostInput;
			}
			readIn.close();
		} catch (Exception ex) {
			return null;
		}
		return output;
	}

	static Map<Api.Language, String> getTranslation(String message, Api.Language senderLang, Map<Integer, Api.Language> targetLang, int translationsNeeded) {
		// Translation index: (Language) of (String)
		Map<Api.Language, String> returns = new ConcurrentHashMap<Api.Language, String>();
		
		String msgPre = "";
		try {
			msgPre = URLEncoder.encode(message, "UTF-8"); // Encodes the message
															// in HTTP format
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		String url;
		String urlPt1 = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=" + senderLang.getISO2() + "&tl=";
		String urlPt3 = "&dt=t&q=" + msgPre + "&ie=UTF-8&oe=UTF-8";

		Ant.hcs.debugCall("message", message, false);
		Ant.hcs.debugCall("senderLang", senderLang, false);

		for (int x = 0; x < translationsNeeded; x++) {
			Ant.hcs.debugCall("loop, lang at index " + x, targetLang.get(x), false);
			if (!(targetLang.get(x) == Api.Language.NONE)) {
				url = urlPt1 + targetLang.get(x).getISO2() + urlPt3;
				String result = readURL(url);

				// Debug info
				Ant.hcs.debugCall("msgPre", msgPre, true);
				Ant.hcs.debugCall("result", result, true);
				Ant.hcs.debugCall("url", url, true);
				
				if(result == null) {
					continue;
				}
				
				String[] res = result.split(",,\"");

				res = res[0].split("\\[\\[\\[");
				try {
					res = res[1].split("\\],\\[");
				} catch (ArrayIndexOutOfBoundsException ignore) {
					Ant.hcs.debugCall("There was no output for this language", targetLang.get(x), true);
				}

				String[] resTemp = res;
				String[] resTemp2 = res;
				for (int y = 0; y < res.length; y++) {
					String temp = res[y];
					resTemp = temp.split("\",\"");
					resTemp[0] = resTemp[0].replace("\"", "");
					resTemp2[y] = resTemp[0];

					// Debug info
					Ant.hcs.debugCall("Filter 1", y, true);
				}

				res = resTemp2;
				String output = "";
				for (int y = 0; y < res.length; y++) {
					output = output + res[y];
					// Debug info
					Ant.hcs.debugCall("Filter 2", y, true);
					Ant.hcs.debugCall("Filter 2 res", output, true);
				}
				if(!output.replace(" ", "").equalsIgnoreCase(message) || !output.replace(" ", "").equalsIgnoreCase(msgPre)) {
					returns.put(targetLang.get(x), output);
					Ant.hcs.debugCall("Output for " + targetLang.get(x).getName(), output, true);	
				}
			}
		}
		return returns;
	}
}