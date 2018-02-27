////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2017, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web;

import com.exactprosystems.remotehand.*;
import com.exactprosystems.remotehand.http.LoginHandler;
import com.exactprosystems.remotehand.http.SessionContext;
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
		webSessionContext.setDonwloadDir(downloadDir);
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
		File donwloadDir = webSessionContext.getDonwloadDir();
		File[] tmp;
		if (donwloadDir != null && (tmp = donwloadDir.listFiles()) != null && tmp.length == 0) {
			donwloadDir.delete();
		}
	}

	@Override
	public LoginHandler createLoginHandler() {
		return new WebLoginHandler(this);
	}
}
