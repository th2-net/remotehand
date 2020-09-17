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

import com.exactprosystems.remotehand.ActionsLauncher;
import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.IDriverManager;
import com.exactprosystems.remotehand.IRemoteHandManager;
import com.exactprosystems.remotehand.RhConfigurationException;
import com.exactprosystems.remotehand.ScriptCompiler;
import com.exactprosystems.remotehand.ScriptProcessorThread;
import com.exactprosystems.remotehand.http.HttpLogonHandler;
import com.exactprosystems.remotehand.sessions.LogonHandler;
import com.exactprosystems.remotehand.sessions.SessionContext;
import org.apache.commons.cli.CommandLine;

import java.net.MalformedURLException;
import java.net.URL;

public class WindowsRemoteHandManager implements IRemoteHandManager {
	@Override
	public ScriptCompiler createScriptCompiler() {
		return new WindowsScriptCompiler();
	}

	@Override
	public Configuration createConfiguration(CommandLine commandLine) {
		return new WindowsConfiguration(commandLine);
	}

	@Override
	public ActionsLauncher createActionsLauncher(ScriptProcessorThread thread) {
		return new WindowsActionsLauncher(thread);
	}

	@Override
	public SessionContext createSessionContext(String sessionId) throws RhConfigurationException {
		WindowsSessionContext windowsSessionContext = new WindowsSessionContext(sessionId);
		WindowsConfiguration instance = (WindowsConfiguration) Configuration.getInstance();
		try {
			String urlPath = instance.getWinAppUrlPath();
			if (!urlPath.startsWith("/")) {
				urlPath = "/" + urlPath;
			}
			if (!urlPath.endsWith("/")) {
				urlPath = urlPath + "/";
			}
			windowsSessionContext.setWinApiDriverURL(new URL(String.format("http://%s:%s%s",
					instance.getWinAppHost(), instance.getWinAppPort(), urlPath)));
			windowsSessionContext.setCurrentDriver(new WindowsDriverWrapper(windowsSessionContext.getWinApiDriverURL()));
		} catch (MalformedURLException e) {
			throw new RhConfigurationException("Cannot create URL", e);
		}
		return windowsSessionContext;
	}

	@Override
	public LogonHandler createLogonHandler() {
		return new HttpLogonHandler(this);
	}

	@Override
	public void close(SessionContext sessionContext) {
		WindowsSessionContext windowsSessionContext = (WindowsSessionContext) sessionContext;
		if (windowsSessionContext.getCurrentDriver() != null) {
			windowsSessionContext.getCurrentDriver().close();
		}
	}

	@Override
	public IDriverManager getWebDriverManager() {
		return new WindowsDriverManager();
	}
}
