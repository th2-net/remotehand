////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand;

import com.exactprosystems.remotehand.http.ErrorRespondent;
import com.exactprosystems.remotehand.http.SessionContext;
import com.exactprosystems.remotehand.http.SessionHandler;
import com.exactprosystems.remotehand.web.WebUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriverException;

import java.util.List;

public class ScriptProcessorThread implements Runnable
{
	private static final Logger logger = Logger.getLogger(ScriptProcessorThread.class);

	private SessionHandler parentSession = null;

	private boolean switchOn = true, 
			busy = true, 
			close = false;

	private String script = null, 
			lastResult = null;

	private final ActionsLauncher launcher;
	private final IRemoteHandManager rhmanager;
	private final ScriptCompiler scriptCompiler;
	private final SessionContext sessionContext;

	public ScriptProcessorThread(SessionHandler session, IRemoteHandManager rhmanager)
	{
		this.parentSession = session;
		this.rhmanager = rhmanager;
		this.scriptCompiler = rhmanager.createScriptCompiler();
		this.launcher = rhmanager.createActionsLauncher(this);
		this.sessionContext = rhmanager.createSessionContext(session.getId());
	}

	@Override
	public void run()
	{
		WebUtils.logInfo(logger, parentSession.getId(), "Processor thread is executed.");
		while (switchOn)
		{
			if (script != null)
			{
				lastResult = processScript();
				script = null;

				busy = false;
			}

			if (close || parentSession == null)
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
		WebUtils.logInfo(logger, parentSession.getId(), "Processor thread was terminated.");
	}

	private String processScript()
	{
		String result;
		try
		{
			final List<Action> actions = scriptCompiler.build(script, sessionContext);
			result = launcher.runActions(actions);
		}
		catch (ScriptCompileException ex1)
		{
			WebUtils.logError(logger, parentSession.getId(), "Compile error: " + ex1.getMessage());
			return ErrorRespondent.getRespondent().error(ex1);
		}
		catch (ScriptExecuteException ex2)
		{
			WebUtils.logError(logger, parentSession.getId(), "Execute error: " + ex2.getMessage());
			return ErrorRespondent.getRespondent().error(ex2);
		}
		catch (WebDriverException ex3)
		{
			WebUtils.logError(logger, parentSession.getId(), "Execute error: " + ex3.getMessage());
			return ErrorRespondent.getRespondent().error(ex3);
		}

		return result;
	}

	private void closeThread()
	{
		this.rhmanager.close(sessionContext);
		switchOn = false;
	}

	public String getResult()
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
}
