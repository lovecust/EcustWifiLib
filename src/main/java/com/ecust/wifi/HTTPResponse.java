package com.ecust.wifi;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * Created by fisher at 2:51 PM on 2/19/17.
 * <p>
 * HTTP Simple Response.
 */

public class HTTPResponse {
	public int code;
	public String response;
	public HttpURLConnection connection;

	public HttpURLConnection getConnection() {
		return connection;
	}

	public HTTPResponse setConnection(HttpURLConnection connection) {
		this.connection = connection;
		return this;
	}

	public HTTPResponse(int code, String response) {
		this.code = code;
		this.response = response;
	}

	@Override
	public String toString() {
		JSONObject json = new JSONObject();
		try {
			json.put("code", code);
			json.put("response", response);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
}
