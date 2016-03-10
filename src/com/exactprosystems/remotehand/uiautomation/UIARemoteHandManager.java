////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.remotehand.uiautomation;

import com.exactprosystems.remotehand.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 * Created by alexey.karpukhin on 2/12/16.
 */
public class UIARemoteHandManager implements IRemoteHandManager{

	@Override
	public ScriptCompiler createScriptCompiler() {
		return new UIAScriptCompiler();
	}

	@Override
	public Configuration createConfiguration(CommandLine commandLine) {
		return new UIAConfiguration(commandLine);
	}

	@Override
	public ActionsLauncher createActionsLauncher(ScriptProcessorThread thread) {
		return new ActionsLauncher(thread);
	}

	@Override
	@SuppressWarnings("static-access")
	public Option[] getAdditionalOptions() {
		return new Option[] {
			OptionBuilder.withArgName("dir").hasArg().withDescription("Specify script directory.").create("scriptDir")} ;
	}


	@Override
	public void close() {
	}
}