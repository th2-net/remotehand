package com.exactprosystems.remotehand;

import java.util.List;

import com.exactprosystems.remotehand.http.ErrorRespondent;
import com.exactprosystems.remotehand.http.SessionHandler;

public class ScriptProcessorThread implements Runnable
{
	private Logger logger = Logger.getLogger();

	private SessionHandler parentSession = null;

	private boolean switchOn = true, 
			busy = true, 
			close = false;

	private String script = null, 
			lastResult = null;

	private ActionsLauncher launcher = new ActionsLauncher(this);

	public ScriptProcessorThread(SessionHandler session)
	{
		this.parentSession = session;
	}

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
			ScriptCompiler compiler = new ScriptCompiler();
			final List<ScriptAction> actions = compiler.build(script);

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
		launcher.close();
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