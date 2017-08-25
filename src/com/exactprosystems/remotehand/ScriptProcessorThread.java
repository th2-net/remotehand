////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2017, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand;

import com.exactprosystems.clearth.connectivity.data.rhdata.RhScriptResult;
import com.exactprosystems.remotehand.http.ErrorRespondent;
import com.exactprosystems.remotehand.http.SessionContext;
import org.apache.log4j.Logger;

import java.util.List;

public class ScriptProcessorThread implements Runnable
{
	private static final Logger logger = Logger.getLogger(ScriptProcessorThread.class);

	private boolean switchOn = true, 
			busy = true, 
			close = false;

	private String script;
	private RhScriptResult lastResult;

	private final String sessionId;
	private final ActionsLauncher launcher;
	private final IRemoteHandManager rhmanager;
	private final ScriptCompiler scriptCompiler;
	private final SessionContext sessionContext;

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
			final List<Action> actions = scriptCompiler.build(script, sessionContext);
			return launcher.runActions(actions, sessionContext);
		}
		catch (Exception ex1)
		{
			RhUtils.logError(logger, sessionId, ex1.getMessage(), ex1);
			return ErrorRespondent.getRespondent().error(ex1);
		}
	}

	private void closeThread()
	{
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
