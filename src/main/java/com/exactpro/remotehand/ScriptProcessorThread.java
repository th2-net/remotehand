/*
 * Copyright 2020-2020 Exactpro (Exactpro Systems Limited)
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
import com.exactpro.remotehand.utils.RhUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

public class ScriptProcessorThread implements Runnable
{
	private static final Logger logger = LoggerFactory.getLogger(ScriptProcessorThread.class);

	private volatile boolean switchOn = true, 
			busy = true, 
			close = false;

	private volatile String script, shutdownScript;
	private volatile RhScriptResult lastResult;

	private final String sessionId;
	private final ActionsLauncher launcher;
	private final IRemoteHandManager rhmanager;
	private final ScriptCompiler scriptCompiler;
	private final SessionContext sessionContext;
	private static ActionsLogger actionsLogger = new ActionsLogger();
	private static final String CTH_ACTION_NAME = "ActionName";
	private static final String SHUTDOWN_SCRIPT_START = "// SHUTDOWN SCRIPT";

	public ScriptProcessorThread(String sessionId, IRemoteHandManager rhmanager) throws RhConfigurationException
	{
		this.sessionId = sessionId;
		this.rhmanager = rhmanager;
		this.scriptCompiler = rhmanager.createScriptCompiler();
		this.launcher = rhmanager.createActionsLauncher(this);
		this.sessionContext = rhmanager.createSessionContext(sessionId);
	}

	@Override
	public void run()
	{
		RhUtils.logInfo(logger, sessionId, "Processor thread is executed.");
		while (switchOn)
		{
			if (script != null)
			{
				lastResult = processScript();
				script = null;
				busy = false;
			}

			if (close)
			{
				if (shutdownScript != null)
				{
					RhUtils.logInfo(logger, sessionId, "Processing shutdown script before closing thread...");
					close = false;
					script = shutdownScript;
					busy = true;
					lastResult = processScript();
					busy = false;
					RhUtils.logInfo(logger, sessionId, "Shutdown script has been executed.");
					close = true;
				}
				closeThread();
			}

			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				// it's ok, do nothing
			}
		}
		RhUtils.logInfo(logger, sessionId, "Processor thread was terminated.");
	}

	private RhScriptResult processScript() 
	{
		try
		{
			String actionName = extractActionName();
			if (actionName != null)
				actionsLogger.init(sessionId, actionName);

			List<Action> actions = scriptCompiler.build(script, null, sessionContext);
			return launcher.runActions(actions, sessionContext);
		}
		catch (Exception ex1)
		{
			RhUtils.logError(logger, sessionId, ex1.getMessage(), ex1);
			return ErrorRespondent.getRespondent().error(ex1);
		}
	}

	private String extractActionName()
	{
		String actionName = null;
		if (script.startsWith(CTH_ACTION_NAME))
		{
			String[] temp = script.split(":");
			actionName = temp[0].split("=")[1];
			script = script.substring(script.indexOf(':') + 1);
		}
		return actionName;
	}

	private void closeThread()
	{
		RhUtils.logInfo(logger, sessionId, "Closing thread");
		this.rhmanager.close(sessionContext);
		switchOn = false;
	}

	public RhScriptResult getResult()
	{
		return lastResult;
	}

	public boolean isBusy()
	{
		return busy;
	}

	public void setScript(String script)
	{
		if (isShutdownScript(script))
		{
			RhUtils.logInfo(logger, sessionId, "Last received script will be used as shutdown script.");
			shutdownScript = script.replaceFirst(Pattern.quote(SHUTDOWN_SCRIPT_START), "");
		}
		else
		{
			busy = true;
			this.script = script;
		}
	}

	private boolean isShutdownScript(String script)
	{
		return script.startsWith(SHUTDOWN_SCRIPT_START);
	}

	public void close()
	{
		close = true;
	}

	public boolean isClosing()
	{
		return close;
	}

	public String getSessionId()
	{
		return sessionId;
	}
}
