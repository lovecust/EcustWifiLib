package com.ecust.wifi;

/**
 * Created by fisher at 2:57 PM on 2/19/17.
 * <p>
 * Wifi Connection interface.
 */

public abstract class IWifiConnector {

	/**
	 * Ready to check Internet status.
	 */
	public void onCheckInternetStatus() {

	}

	/**
	 * Device is online.
	 */
	public void onDeviceIsOnline() {

	}

	/**
	 * Device is offline.
	 */
	public void onDeviceIsOffline() {

	}

	/**
	 * Failed to get redirected url.
	 */
	public void onFailedToGetRedirectedUrl() {

	}

	/**
	 * Failed to follow redirection url.
	 */
	public void onFailedToFollowRedirection() {

	}

	/**
	 * Failed to get ac_id.
	 */
	public void onFailedToGetACID() {

	}

	/**
	 * On Device logged in.
	 */
	public void onLoggedIn(Long usingTime, String acID) {
	}

	/**
	 * Failed to log in.
	 */
	public void onFailedToLogIn() {

	}

	/**
	 * On request error.
	 */
	public void onRequestError() {

	}

	public void onClientError() {

	}
}
