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
import com.exactprosystems.remotehand.http.SessionHandler;
import org.apache.log4j.Logger;

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

	private ActionsLauncher launcher = null;
	private IRemoteHandManager rhmanager = null;
//	private WebElementsDictionary webDictionary = null;

	public ScriptProcessorThread(SessionHandler session, IRemoteHandManager rhmanager)
	{
		this.parentSession = session;
		this.rhmanager = rhmanager;
		this.launcher = rhmanager.createActionsLauncher(this);
	}

//	public void setWebDictionary(String webDictionary)
//	{
//		this.webDictionary = new WebElementsDictionary(webDictionary, false);
//	}

	@Override
	public void run()
	{
		logger.info("Processor thread for session " + parentSession.getId() + " is executed");

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
		logger.info("Processor thread was terminated");
	}

	private String processScript()
	{
		String result = null;
		try
		{
			ScriptCompiler compiler = this.rhmanager.createScriptCompiler();
			final List<Action> actions = compiler.build(script);

			result = launcher.runActions(actions);

		}
		catch (ScriptCompileException ex1)
		{
			logger.error("Compile error: " + ex1.getMessage());
			return ErrorRespondent.getRespondent().error(ex1);
		}
		catch (ScriptExecuteException ex2)
		{
			logger.error("Execute error: " + ex2.getMessage());
			return ErrorRespondent.getRespondent().error(ex2);
		}

		return result;
	}

	private void closeThread()
	{
		this.rhmanager.close();
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
