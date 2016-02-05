////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web;

import com.exactprosystems.remotehand.*;

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
		return new WebScriptCompiler(webDriverManager);
	}

	@Override
	public Configuration createConfiguration() {
		return new WebConfiguration();
	}

	@Override
	public ActionsLauncher createActionsLauncher(ScriptProcessorThread thread) {
		return new ActionsLauncher(thread);
	}

	@Override
	public void close() {
		webDriverManager.close();
	}
}
