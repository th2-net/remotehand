/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.sessions;

import static com.exactprosystems.remotehand.RhUtils.logError;
import static com.exactprosystems.remotehand.RhUtils.logInfo;
import static java.lang.String.format;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.clearth.connectivity.data.rhdata.JsonSerializer;
import com.exactprosystems.clearth.connectivity.data.rhdata.RhResponseCode;
import com.exactprosystems.clearth.connectivity.data.rhdata.RhScriptResult;
import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.IRemoteHandManager;
import com.exactprosystems.remotehand.RhConfigurationException;
import com.exactprosystems.remotehand.ScriptProcessorThread;
import com.exactprosystems.remotehand.requests.ExecutionRequest;
import com.exactprosystems.remotehand.requests.ExecutionStatusRequest;
import com.exactprosystems.remotehand.requests.FileUploadRequest;
import com.exactprosystems.remotehand.requests.LogoutRequest;
import com.exactprosystems.remotehand.requests.RhRequest;

/**
 * Handles requests related to particular session
 */
public abstract class SessionHandler
{
	private static final Logger logger = LoggerFactory.getLogger(SessionHandler.class);
	
	//Result codes are the same as for HTTP as it is main interaction protocol for RemoteHand
	public static final int CODE_SUCCESS = 200,
			CODE_ERROR = 500,
			CODE_BAD = 400,
			CODE_NOTFOUND = 404;
	
	private final String id;

	private ScriptProcessorThread scriptProcessor = null;
	private IRemoteHandManager manager;
	private JsonSerializer serializer = new JsonSerializer();
	
	public SessionHandler(String id, IRemoteHandManager manager)
	{
		this.id = id;
		SessionWatcher.getWatcher().addSession(this);
		this.manager = manager;
		logger.info("Created session <" + id + ">");
	}
	
	
	protected abstract void closeConnection() throws IllegalArgumentException;
	
	
	public void handle(RhRequest request, SessionExchange exchange) throws IOException
	{
		SessionWatcher.getWatcher().updateSession(this);
		if (request instanceof ExecutionRequest)
			handleExecution((ExecutionRequest)request, exchange);
		else if (request instanceof FileUploadRequest)
			handleFileUpload((FileUploadRequest)request, exchange);
		else if (request instanceof ExecutionStatusRequest)
			handleStatus((ExecutionStatusRequest)request, exchange);
		else if (request instanceof LogoutRequest)
			handleLogout((LogoutRequest)request, exchange);
	}
	
	
	protected void handleFileUpload(FileUploadRequest request, SessionExchange exchange) throws IOException
	{
		File f = new File(request.getFileName());
		if (!f.isAbsolute())
			f = new File(Configuration.getInstance().getFileStorage(), f.getName());
		logger.info("Session <" + id + ">. Received request to upload file '" + f.toString() + "'");
		
		if (f.exists())
			logger.info("File: " + f.toString() + " exists. Rewriting...");
		else
		{
			try
			{
				if (f.getParentFile() != null && !f.getParentFile().exists())
					f.getParentFile().mkdirs();
				f.createNewFile();
			}
			catch (Exception e)
			{
				logger.error("Could not create directory or file.", e);
				return;
			}
		}
		
		try
		{
			byte[] contents = request.getContents();
			FileUtils.writeByteArrayToFile(f, contents, false);
			sendSuccessMessage(exchange, "OK");
		}
		catch (Exception e)
		{
			logger.error("Could not write file.", e);
		}
	}
	
	protected void handleExecution(ExecutionRequest request, SessionExchange exchange) throws IOException
	{
		String body = request.getScript();
		logInfo(logger, id, format("Received text:%n%s", body));
		
		try
		{
			launchScript(body);
			sendSuccessMessage(exchange, "OK");
		}
		catch (RhConfigurationException e)
		{
			logError(logger, id, "An error occurred:", e);
			sendErrorMessage(exchange, "Internal error. " + e.getMessage());
		}
	}
	
	protected void handleStatus(ExecutionStatusRequest request, SessionExchange exchange) throws IOException
	{
		logger.info("Session <" + id + ">. Request for result");
		if (scriptProcessor == null)
		{
			sendIncorrectRequestMessage(exchange, "No results. No scripts have been passed.");
			return;
		}

		if (scriptProcessor.isBusy())
		{
			sendBusyMessage(exchange);
			return;
		}

		sendSuccessMessage(exchange, serializer.serialize(scriptProcessor.getResult()));
	}
	
	protected void handleLogout(LogoutRequest request, SessionExchange exchange) throws IOException
	{
		logger.info("Session <" + id + ">. Request for session close");
		close();
		sendSuccessMessage(exchange, "OK");
	}
	
	
	protected void sendMessage(SessionExchange exchange, int code, String message) throws IOException
	{
		exchange.sendResponse(code, message);
	}
	
	protected void sendSuccessMessage(SessionExchange exchange, String message) throws IOException
	{
		sendMessage(exchange, CODE_SUCCESS, message);
	}
	
	protected void sendErrorMessage(SessionExchange exchange, String message) throws IOException
	{
		sendMessage(exchange, CODE_ERROR, message);
	}
	
	protected void sendBusyMessage(SessionExchange exchange) throws IOException
	{
		RhScriptResult result = new RhScriptResult();
		result.setCode(RhResponseCode.TOOL_BUSY.getCode());
		result.setErrorMessage("Process is busy");
		sendSuccessMessage(exchange, serializer.serialize(result));
	}

	protected void sendIncorrectRequestMessage(SessionExchange exchange, String message) throws IOException
	{
		RhScriptResult result = new RhScriptResult();
		result.setCode(RhResponseCode.INCORRECT_REQUEST.getCode());
		result.setErrorMessage(message);
		sendSuccessMessage(exchange, serializer.serialize(result));
	}

	private void launchScript(String script) throws RhConfigurationException
	{
		if (scriptProcessor == null)
		{
			scriptProcessor = new ScriptProcessorThread(id, manager);
			final Thread webThread = new Thread(scriptProcessor);
			webThread.start();
		}

		scriptProcessor.setScript(script);
	}

	public void close()
	{
		if (scriptProcessor != null)
			scriptProcessor.close();

		try
		{
			closeConnection();
		}
		catch (IllegalArgumentException ex)
		{
			logger.warn("Error while closing session <" + id + ">. Nothing to close.");
		}
	}

	public String getId()
	{
		return id;
	}
}