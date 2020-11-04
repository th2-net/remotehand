/*
 * Copyright 2020-2020 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exactprosystems.remotehand.windows;

import com.exactprosystems.remotehand.*;
import com.exactprosystems.remotehand.http.HttpLogonHandler;
import com.exactprosystems.remotehand.sessions.LogonHandler;
import com.exactprosystems.remotehand.sessions.SessionContext;

import io.appium.java_client.windows.WindowsDriver;
import org.apache.commons.cli.CommandLine;

import java.util.Map;

public class WindowsRemoteHandManager implements IRemoteHandManager {
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
	public Configuration createConfiguration(CommandLine commandLine, Map<String, String> options) {
		WindowsConfiguration.init(commandLine, options);
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
		WindowsDriverWrapper driverWrapper = (WindowsDriverWrapper)driverPoolProvider.createDriverWrapper(windowsSessionContext);
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
		return RemoteManagerType.WINDOWS;
	}
}
