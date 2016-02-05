////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand;

/**
 * Created by alexey.karpukhin on 2/1/16.
 */
public interface IRemoteHandManager {

	ScriptCompiler createScriptCompiler ();

	Configuration createConfiguration();

	ActionsLauncher createActionsLauncher(ScriptProcessorThread thread);

	void close();

}
