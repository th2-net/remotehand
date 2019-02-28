/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web;

import com.exactprosystems.remotehand.*;
import com.exactprosystems.remotehand.http.HttpLogonHandler;
import com.exactprosystems.remotehand.sessions.LogonHandler;
import com.exactprosystems.remotehand.sessions.SessionContext;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.openqa.selenium.WebDriver;

import java.io.File;

/**
 * Created by alexey.karpukhin on 2/2/16.
 */
public class WebRemoteHandManager implements IRemoteHandManager {

	private WebDriverManager webDriverManager;

	public WebRemoteHandManager () {
		webDriverManager = new WebDriverManager();
	}

	@Override
	public ScriptCompiler createScriptCompiler() {
		return new WebScriptCompiler();
	}

	@Override
	public Configuration createConfiguration(CommandLine commandLine) {
		return new WebConfiguration(commandLine);
	}


	@Override
	public ActionsLauncher createActionsLauncher(ScriptProcessorThread thread) {
		return new WebActionsLauncher(thread);
	}

	@Override
	public SessionContext createSessionContext(String sessionId) throws RhConfigurationException
	{
		WebSessionContext webSessionContext = new WebSessionContext(sessionId);
		File downloadDir = WebUtils.createDownloadDirectory();
		webSessionContext.setDownloadDir(downloadDir);
		webSessionContext.setWebDriverManager(webDriverManager);
		webSessionContext.setWebDriver(webDriverManager.createWebDriver(sessionId, downloadDir));
		return webSessionContext;
	}

	@Override
	public Option[] getAdditionalOptions() {
		return new Option[0];
	}

	@Override
	public void close(SessionContext sessionContext) 
	{
		if (sessionContext == null)
			return;
		WebSessionContext webSessionContext = (WebSessionContext) sessionContext;
		WebDriver webDriver = webSessionContext.getWebDriver();
		if (webDriver != null)
			webDriverManager.closeWebDriver(webDriver, webSessionContext.getSessionId());
		File downloadDir = webSessionContext.getDownloadDir();
		File[] tmp;
		if (downloadDir != null && (tmp = downloadDir.listFiles()) != null && tmp.length == 0) {
			downloadDir.delete();
		}
	}

	@Override
	public LogonHandler createLogonHandler() {
		return new HttpLogonHandler(this);
	}
}
