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

import com.exactprosystems.remotehand.*;
import com.exactprosystems.remotehand.http.HttpLogonHandler;
import com.exactprosystems.remotehand.sessions.LogonHandler;
import com.exactprosystems.remotehand.sessions.SessionContext;
import com.exactprosystems.remotehand.web.WebConfiguration;

import io.appium.java_client.windows.WindowsDriver;
import org.apache.commons.cli.CommandLine;

public class WindowsRemoteHandManager implements IRemoteHandManager {
	private final RemoteManagerType managerType = RemoteManagerType.WINDOWS;
	private final DriverPoolProvider<? extends DriverWrapper<WindowsDriver<?>>> driverPoolProvider;


	public WindowsRemoteHandManager(DriverPoolProvider<? extends DriverWrapper<WindowsDriver<?>>> driverPoolProvider)
	{
		this.driverPoolProvider = driverPoolProvider;
	}


	@Override
	public ScriptCompiler createScriptCompiler() {
		return new WindowsScriptCompiler();
	}

	@Override
	public Configuration createConfiguration(CommandLine commandLine) {
		WindowsConfiguration.init(commandLine);
		return WindowsConfiguration.getInstance();
	}

	@Override
	public ActionsLauncher createActionsLauncher(ScriptProcessorThread thread) {
		return new WindowsActionsLauncher(thread);
	}

	@Override
	public SessionContext createSessionContext(String sessionId) throws RhConfigurationException
	{
		WindowsSessionContext windowsSessionContext = new WindowsSessionContext(sessionId);
		WindowsDriverWrapper driverWrapper = (WindowsDriverWrapper)driverPoolProvider.getDriverWrapper(windowsSessionContext);
		windowsSessionContext.setWinApiDriverURL(driverWrapper.getDriverUrl());
		windowsSessionContext.setCurrentDriver(driverWrapper);
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
			driverPoolProvider.closeDriver(sessionContext.getSessionId(), null);
		}
	}

	@Override
	public IDriverManager getWebDriverManager() {
		return new WindowsDriverManager();
	}

	@Override
	public RemoteManagerType getManagerType()
	{
		return managerType;
	}
}
