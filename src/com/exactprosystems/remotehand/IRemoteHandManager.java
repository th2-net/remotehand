////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
	
	SessionContext createSessionContext(String sessionId);

	LoginHandler createLoginHandler();

	Option[] getAdditionalOptions();

	void close(SessionContext sessionContext);

}
