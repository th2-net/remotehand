/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand;

import com.exactprosystems.clearth.connectivity.data.rhdata.RhScriptResult;
import com.exactprosystems.remotehand.http.ErrorRespondent;
import com.exactprosystems.remotehand.sessions.SessionContext;
import com.exactprosystems.remotehand.utils.ExceptionUtils;
import com.exactprosystems.remotehand.windows.WindowsAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class ActionsLauncher
{
	private static final Logger logger = LoggerFactory.getLogger(ActionsLauncher.class);
	
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
		logger.info(format("<%s> Script execution starting...", sessionId));
		long startTimeMs = System.currentTimeMillis();
		
		for (Action action : scriptActions)
		{
			try
			{
				action.beforeExecute();
				final String actionResult = action.execute();
				if (actionResult != null)
					processActionResult(result, action, actionResult);

				if (parent != null && parent.isClosing())
					return null;
			}
			catch (Exception e)
			{
				RhUtils.logError(logger, sessionId, "Error while executing actions", e);
				return ErrorRespondent.getRespondent().error(e, buildErrorMessage(action, e));
			}
		}
		
		long duration = System.currentTimeMillis() - startTimeMs;
		logger.info(format("<%s> Script execution time: %d sec.", sessionId,
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


	private String buildErrorMessage(Action action, Throwable e) {
		StringBuilder builder = new StringBuilder();
		builder.append("An error occurred while executing action '").append(action.getActionName()).append("'");
		if (action instanceof WindowsAction)
			builder.append(" with parameters ").append(((WindowsAction) action).getParams());
		builder.append(ExceptionUtils.EOL);
		builder.append("Cause: ").append(ExceptionUtils.getDetailedMessage(e));

		return builder.toString();
	}
}