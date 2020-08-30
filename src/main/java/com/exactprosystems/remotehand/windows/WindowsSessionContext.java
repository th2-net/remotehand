/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.windows;

import com.exactprosystems.remotehand.sessions.SessionContext;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class WindowsSessionContext extends SessionContext {
	
	private WindowsDriverWrapper currentDriver;
	private URL winApiDriverURL;
	private final Map<String, Object> mvelVars;

	public WindowsSessionContext(String sessionId) {
		super(sessionId);
		this.mvelVars = new HashMap<>();
	}

	public WindowsDriverWrapper getCurrentDriver() {
		return currentDriver;
	}

	public void setCurrentDriver(WindowsDriverWrapper currentDriver) {
		this.currentDriver = currentDriver;
	}

	public URL getWinApiDriverURL() {
		return winApiDriverURL;
	}

	public void setWinApiDriverURL(URL winApiDriverURL) {
		this.winApiDriverURL = winApiDriverURL;
	}

	public Map<String, Object> getMvelVars() {
		return mvelVars;
	}
}
