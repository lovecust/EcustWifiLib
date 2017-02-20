package com.ecust.wifi;

import android.text.TextUtils;
import android.util.Base64;

import com.fisher.utils.RegexUtil;
import com.fisher.utils.URLUtil;
import com.fisher.utils.constants.FileConstant;
import com.fisher.utils.ConsoleUtil;
import com.fisher.utils.FileUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * `Created` by Fisher at 17:22 on 2017-02-19.
 */
public class WifiLibrary {

	private static final String DEFAULT_NAS_IP = "";

	/**
	 * Get device Internet connection status.
	 *
	 * @return Online or Offline.
	 */
	public static boolean internetStatus() throws IOException {
		HTTPResponse res = WifiLibrary.request(EcustWifiConstant.URL_ECUST_WIFI_CHECK);
		return EcustWifiConstant.STATUS_NOT_ONLINE.equals(res.response);
	}

	/**
	 * Do pre fetch for direction.
	 * <p>
	 * This filed rarely changes.
	 * <p>
	 * Older Time Got: "http://www.baidu.com/&arubalp=221e86ec-1a18-4a48-8d6e-c5f4ef09d0"
	 * 2017-02-19 Got: "http://login.ecust.edu.cn/&arubalp=7cbd8c4e-1a0c-4ee1-b0aa-a625aeb1ee"
	 */
	public static String getRedirectedURL() {
		try {
			HTTPResponse res = WifiLibrary.request(EcustWifiConstant.URL_REDIRECTION);
			ConsoleUtil.log(res.toString());
			String url = RegexUtil.find("content='(.+?)'", res.response);
			if (null != url) {url = url.substring(url.indexOf("url=") + 4);}
			return url;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Follow the redirected url.
	 *
	 * @return Another Redirection Url.
	 */
	public static String followRedirectURL(String redirectedUrl) throws IOException {
		HTTPResponse res = WifiLibrary.request(redirectedUrl);
		if (res.code != HttpURLConnection.HTTP_OK &&
				(res.code == HttpURLConnection.HTTP_MOVED_TEMP || res.code == HttpURLConnection.HTTP_MOVED_PERM || res.code == HttpURLConnection.HTTP_SEE_OTHER)) {
			String direction = res.getConnection().getHeaderField("Location");
			String[] urls = direction.split("/index_(\\d+).html\\?");
			if (2 != urls.length) {return null;}
			String acID = RegexUtil.find("/index_(\\d+).html", direction);
			return urls[0] + "/ac_detect.php?ac_id=" + acID + "&" + urls[1];
		}
		return null;
	}

	/**
	 * Get AC_ID from HTML.
	 *
	 * @param redirectedUrl Redirected url.
	 * @return ACID.
	 * @throws IOException
	 */
	public static String getACID(String redirectedUrl) throws IOException {
		HTTPResponse res = WifiLibrary.requestAndFollow(redirectedUrl);
		if (HttpURLConnection.HTTP_OK == res.code) {
			return RegexUtil.find("<input.{0,30}name=\"ac_id\".{0,30}value=\"(\\d{0,2})\">", res.response);
		} else {
			return null;
		}
	}

	/**
	 * Get post string for logging in.
	 *
	 * @return Post string.
	 * @throws UnsupportedEncodingException
	 */
	private static String getPostString(String username, String password, int acID, String ip, String mac) throws UnsupportedEncodingException {
		if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(ip) || TextUtils.isEmpty(mac)) {
			return null;
		}
		HashMap<String, String> map = new HashMap<>();
		map.put("action", "login");
		if (username.endsWith(EcustWifiConstant.UID_POSTFIX)) {
			map.put("username", username);
		} else {
			map.put("username", username + EcustWifiConstant.UID_POSTFIX);
		}
		map.put("password", EcustWifiConstant.PWD_PREFIX + new String(Base64.encode(password.getBytes(FileConstant.CHARSET_UTF_8), Base64.DEFAULT), FileConstant.CHARSET_UTF_8));
		ConsoleUtil.log(map.get("password"));
		map.put("ac_id", String.valueOf(acID));
		map.put("user_ip", ip);
		map.put("nas_ip", DEFAULT_NAS_IP);
		map.put("user_mac", mac);
		map.put("ajax", String.valueOf(1));
		return URLUtil.getPostDataString(map);
	}

	/**
	 * @param username User name.
	 * @param password Password.
	 * @param acID     ACID.
	 * @return
	 * @throws IOException
	 */
	public static boolean doLogin(String username, String password, int acID, String ip, String mac) throws IOException {
		String data = getPostString(username, password, acID, ip, mac);
		HTTPResponse response = WifiLibrary.request(EcustWifiConstant.URL_ECUST_WIFI_LOGIN, data);
		ConsoleUtil.log(response.toString());
		return response.response.contains(EcustWifiConstant.LOGIN_SUCCEEDED);
	}

	/**
	 * Send a HTTP get request.
	 *
	 * @param url Resource URL.
	 * @return HTTP Response.
	 * @throws IOException
	 */
	public static HTTPResponse request(String url) throws IOException {
		return request(url, null, false);
	}

	/**
	 * Send a HTTP get request and follow redirect.
	 *
	 * @param url Resource URL.
	 * @return HTTP Response.
	 * @throws IOException
	 */
	public static HTTPResponse requestAndFollow(String url) throws IOException {
		return request(url, null, true);
	}

	/**
	 * Set up a simple post request.
	 *
	 * @param url    URL.
	 * @param params HTTP Params
	 * @return HTTP response.
	 * @throws IOException
	 */
	public static HTTPResponse request(String url, String params) throws IOException {
		return request(url, params, false);
	}

	/**
	 * Send a HTTP request.
	 *
	 * @param url      Resource URL.
	 * @param params   Request body.
	 * @param redirect Weather to follow redirect.
	 * @return HTTP Response.
	 * @throws IOException
	 */
	private static HTTPResponse request(String url, String params, boolean redirect) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setInstanceFollowRedirects(redirect);
		conn.setConnectTimeout(15000);
		conn.setReadTimeout(10000);
		conn.setConnectTimeout(15000);
		// set headers
		if (null != params && !"".equals(params)) {
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(params);
			writer.flush();
			writer.close();
			os.close();
		}
		ConsoleUtil.log("HTTPUtil.login(server, params)-> now requesting: " + url);
		int code = conn.getResponseCode();
		InputStream in = conn.getInputStream();
		String response = FileUtil.getString(in);
		ConsoleUtil.log("HTTPUtil.login(server, params)-> now response: " + response);
		return new HTTPResponse(code, response).setConnection(conn);
	}
}
