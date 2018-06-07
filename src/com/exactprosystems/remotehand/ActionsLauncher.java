/******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand;

import com.exactprosystems.clearth.connectivity.data.rhdata.RhScriptResult;
import com.exactprosystems.remotehand.http.SessionContext;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ActionsLauncher
{
	private static final Logger logger = Logger.getLogger(ActionsLauncher.class);

	ScriptProcessorThread parent = null;

	public ActionsLauncher(ScriptProcessorThread parentThread)
	{
		this.parent = parentThread;
	}

	public RhScriptResult runActions(List<Action> scriptActions, SessionContext context) throws ScriptExecuteException, RhConfigurationException
	{
		beforeActions(context);
		
		RhScriptResult result = new RhScriptResult();
		
		String sessionId = getSessionId();
		logger.info(String.format("<%s> Script execution starting...", sessionId));
		long startTimeMs = System.currentTimeMillis();
		
		for (Action action : scriptActions)
		{
			action.beforeExecute();
			final String actionResult = action.execute();
			if (actionResult != null)
				processActionResult(result, action, actionResult);

			if (parent != null && parent.isClosing())
			{
				return null;
			}
		}
		
		long duration = System.currentTimeMillis() - startTimeMs;
		logger.info(String.format("<%s> Script execution time: %d sec.", sessionId, 
				TimeUnit.MILLISECONDS.toSeconds(duration)));

		return result;
	}
	
	protected void processActionResult(RhScriptResult scriptResult, Action action, String actionResult)
	{
		scriptResult.addToTextOutput(actionResult);
	}
	
	protected String getSessionId()
	{
		return parent != null ? parent.getSessionId() : RhUtils.SESSION_FOR_FILE_MODE;
	}
	
	protected void beforeActions(SessionContext context) throws ScriptExecuteException, RhConfigurationException { }
}
