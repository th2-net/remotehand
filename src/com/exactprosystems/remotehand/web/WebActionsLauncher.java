////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.remotehand.web;

import com.exactprosystems.clearth.connectivity.data.rhdata.RhScriptResult;
import com.exactprosystems.remotehand.Action;
import com.exactprosystems.remotehand.ActionsLauncher;
import com.exactprosystems.remotehand.ScriptProcessorThread;

/**
 * @author anna.bykova.
 */
public class WebActionsLauncher extends ActionsLauncher
{
	public WebActionsLauncher(ScriptProcessorThread parentThread)
	{
		super(parentThread);
	}
	
	@Override
	protected void processActionResult(RhScriptResult scriptResult, Action action, String actionResult)
	{
		WebAction webAction = (WebAction) action;
		switch (webAction.getOutputType())
		{
			case SCREENSHOT:
				scriptResult.addScreenshotId(actionResult);
				break;
			case ENCODED_DATA:
				scriptResult.addToEncodedOutput(actionResult);
				break;
			default:
				scriptResult.addToTextOutput(actionResult);
		}
	}
}
