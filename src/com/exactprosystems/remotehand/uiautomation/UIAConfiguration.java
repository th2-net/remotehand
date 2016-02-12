////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.remotehand.uiautomation;

import com.exactprosystems.remotehand.Configuration;
import org.apache.commons.cli.CommandLine;

import java.io.File;

/**
 * Created by alexey.karpukhin on 2/11/16.
 */
public class UIAConfiguration extends Configuration {

	private volatile File scriptDir;

	protected UIAConfiguration(CommandLine commandLine) {
		super(commandLine);
		String scriptDirName = commandLine.getOptionValue("scriptDir");
		this.scriptDir = (scriptDirName != null) ? new File (scriptDirName): null;
	}

	public File getScriptDir() {
		return scriptDir;
	}

	public void setScriptDir(File scriptDir) {
		this.scriptDir = scriptDir;
	}

}
