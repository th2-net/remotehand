/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand;

import com.exactprosystems.remotehand.http.LoginHandler;
import com.exactprosystems.remotehand.http.SessionContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

/**
 * Created by alexey.karpukhin on 2/1/16.
 */
public interface IRemoteHandManager {

	ScriptCompiler createScriptCompiler ();

	Configuration createConfiguration(CommandLine commandLine);

	ActionsLauncher createActionsLauncher(ScriptProcessorThread thread);
	
	SessionContext createSessionContext(String sessionId) throws RhConfigurationException;

	LoginHandler createLoginHandler();

	Option[] getAdditionalOptions();

	void close(SessionContext sessionContext);

}
