/*
 * Copyright 2020-2021 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exactpro.remotehand;

import com.exactpro.remotehand.http.ErrorRespondent;
import com.exactpro.remotehand.rhdata.RhScriptResult;
import com.exactpro.remotehand.sessions.SessionContext;
import com.exactpro.remotehand.utils.ExceptionUtils;
import com.exactpro.remotehand.windows.WindowsAction;
import com.exactpro.remotehand.windows.WindowsScriptExecuteException;
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
			catch (Exception e) {
				Exception printableException = e;
				if (e instanceof WindowsScriptExecuteException) {
					WindowsScriptExecuteException windowsScriptExecuteException = (WindowsScriptExecuteException) e;
					printableException = windowsScriptExecuteException.hasDriverException()
							? windowsScriptExecuteException.getDriverException() : e;
				}
				RhUtils.logError(logger, sessionId, "Error while executing actions", printableException);
				return ErrorRespondent.getRespondent().error(e, buildErrorMessage(action, e));
			}
		}
		
		long duration = System.currentTimeMillis() - startTimeMs;
		logger.info(format("<%s> Script execution time: %d sec.", sessionId,
				TimeUnit.MILLISECONDS.toSeconds(duration)));

		return result;
	}

	protected void processActionResult(RhScriptResult scriptResult, Action action, String actionResult) {
		switch (action.getOutputType()) {
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
	
	protected String getSessionId()
	{
		return parent != null ? parent.getSessionId() : RhUtils.SESSION_FOR_FILE_MODE;
	}
	
	protected void beforeActions(SessionContext context) throws ScriptExecuteException, RhConfigurationException { }

	protected String buildErrorMessage(Action action, Throwable e) {
		StringBuilder builder = new StringBuilder();
		builder.append("An error occurred while executing action '").append(action.getActionName()).append("'");
		if (action instanceof WindowsAction)
			builder.append(" with parameters ").append(((WindowsAction) action).getParams());
		builder.append(ExceptionUtils.EOL);
		builder.append("Cause: ").append(ExceptionUtils.getDetailedMessage(e));

		return builder.toString();
	}
}
