/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ScriptProcessorThread implements Runnable
{
	private static final Logger logger = LoggerFactory.getLogger(ScriptProcessorThread.class);

	private volatile boolean switchOn = true, 
			busy = true, 
			close = false;

	private volatile String script;
	private volatile RhScriptResult lastResult;

	private final String sessionId;
	private final ActionsLauncher launcher;
	private final IRemoteHandManager rhmanager;
	private final ScriptCompiler scriptCompiler;
	private final SessionContext sessionContext;
	private static ActionsLogger actionsLogger = new ActionsLogger();
	private static final String CTH_ACTION_NAME = "ActionName";

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
				closeThread();

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

			final List<Action> actions = scriptCompiler.build(script, null, sessionContext);
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
			String temp[] = script.split(":");
			actionName = temp[0].split("=")[1];
			script = script.substring(script.indexOf(":") + 1);
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
		busy = true;
		this.script = script;
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
