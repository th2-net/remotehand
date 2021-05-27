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

package com.exactpro.remotehand.web;

import com.exactpro.remotehand.*;
import com.exactpro.remotehand.http.HttpLogonHandler;
import com.exactpro.remotehand.sessions.LogonHandler;
import com.exactpro.remotehand.sessions.SessionContext;
import org.apache.commons.cli.CommandLine;

import java.util.Map;

public class WebRemoteHandManager implements IRemoteHandManager
{
	protected WebDriverManager webDriverManager;
	protected static WebConfiguration configuration;

	public WebRemoteHandManager(DriverPoolProvider<WebDriverWrapper> driverPoolProvider)
	{
		webDriverManager = new WebDriverManager(driverPoolProvider);
	}

	@Override
	public ScriptCompiler createScriptCompiler() {
		return new WebScriptCompiler();
	}

	@Override
	public Configuration createConfiguration(CommandLine commandLine) {
		WebConfiguration.init(commandLine);
		return WebConfiguration.getInstance();
	}

	@Override
	public Configuration createConfiguration(CommandLine commandLine, Map<String, String> options) {
		WebConfiguration.init(commandLine, options);
		return WebConfiguration.getInstance();
	}

	@Override
	public ActionsLauncher createActionsLauncher(ScriptProcessorThread thread) {
		return new WebActionsLauncher(thread);
	}

	@Override
	public SessionContext createSessionContext(String sessionId) throws RhConfigurationException
	{
		WebSessionContext webSessionContext = new WebSessionContext(sessionId);
		webDriverManager.createWebDriver(webSessionContext);
		return webSessionContext;
	}

	@Override
	public void close(SessionContext sessionContext) 
	{
		if (sessionContext == null)
			return;
		WebSessionContext webSessionContext = (WebSessionContext) sessionContext;
		WebDriverWrapper webDriver = webSessionContext.getWebDriverWrapper();
		if (webDriver != null)
			webDriverManager.closeWebDriver(webDriver, webSessionContext.getSessionId());
		WebUtils.deleteDownloadDirectory(webSessionContext.getDownloadDir());
	}

	@Override
	public LogonHandler createLogonHandler() {
		return new HttpLogonHandler(this);
	}

	public WebDriverManager getWebDriverManager()
	{
		return webDriverManager;
	}

	@Override
	public RemoteManagerType getManagerType()
	{
		return RemoteManagerType.WEB;
	}
}
