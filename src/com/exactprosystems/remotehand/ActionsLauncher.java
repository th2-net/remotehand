////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand;

import java.util.List;
import java.util.Map;

import com.exactprosystems.remotehand.web.WebScriptAction;
import com.exactprosystems.remotehand.web.WebAction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.exactprosystems.remotehand.web.webelements.WebLocator;

public class ActionsLauncher
{

	ScriptProcessorThread parent = null;

	public ActionsLauncher(ScriptProcessorThread parentThread)
	{
		this.parent = parentThread;
	}

	public String runActions(List<ScriptAction> scriptActions) throws ScriptExecuteException
	{
		StringBuilder result = null;

		for (ScriptAction scriptAction : scriptActions)
		{
			final Action action = scriptAction.getAction();

			final String actionResult = action.execute(scriptAction);
			if (actionResult != null)
			{
				if (result==null)
					result = new StringBuilder();
				result.append(actionResult+"\r\n");
			}

			if (parent != null && parent.isClosing())
			{
				return null;
			}
		}

		return result!=null ? result.toString() : null;
	}
}
