////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2017, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.http;

import java.io.*;
import java.util.List;

import com.exactprosystems.clearth.connectivity.data.rhdata.JsonSerializer;
import com.exactprosystems.clearth.connectivity.data.rhdata.RhResponseCode;
import com.exactprosystems.clearth.connectivity.data.rhdata.RhScriptResult;
import com.exactprosystems.remotehand.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import static com.exactprosystems.remotehand.RhUtils.logError;
import static com.exactprosystems.remotehand.RhUtils.logInfo;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

public class SessionHandler implements HttpHandler
{
	private static final Logger logger = Logger.getLogger(SessionHandler.class);

	private final String id;

	private ScriptProcessorThread scriptProcessor = null;
	private IRemoteHandManager manager;
	private JsonSerializer serializer = new JsonSerializer();

	SessionHandler(String id, IRemoteHandManager manager)
	{
		this.id = id;
		SessionWatcher.getWatcher().addSession(this);
		this.manager = manager;
		logger.info("Created session <" + id + ">");
	}

	@Override
	public void handle(HttpExchange exchanger) throws IOException
	{
		SessionWatcher.getWatcher().updateSession(this);
		final String method = exchanger.getRequestMethod();
		if (method.equalsIgnoreCase("POST"))
		{
			List<String> contTypeList = exchanger.getRequestHeaders().get("Content-type");
			List<String> transFNs = exchanger.getRequestHeaders().get("Transfer-filename");
			if (contTypeList != null && !contTypeList.isEmpty() && "application/octet-stream".equals(contTypeList.get(0))
					 && transFNs != null && !transFNs.isEmpty()) {

				String outFileName = transFNs.get(0);
				File f = new File(outFileName);

				if (!f.isAbsolute()) {
					f = new File(Configuration.getInstance().getFileStorage(), outFileName);
				}

				if (f.exists()) {
					logger.info("File: " + f.toString() + " exists. Rewriting...");
				} else {

					try {

						if (f.getParentFile() != null && !f.getParentFile().exists()) {
							f.getParentFile().mkdirs();
						}
						f.createNewFile();
					} catch (Exception e) {
						logger.error("Can't create directory or file.", e);
						return;
					}

				}

				InputStream requestBody = null;
				FileOutputStream fw = null;


				try {
					requestBody = exchanger.getRequestBody();
					fw = new FileOutputStream(f, false);
					byte[] buffer = new byte[512];
					int readBytes;
					while ((readBytes = requestBody.read(buffer)) != -1) {
						fw.write(buffer, 0, readBytes);
					}
					sendMessage(exchanger, "OK");
					logger.info("Session <" + id + ">. Received request for file:" + f.toString());
				} catch (Exception e) {
					logger.error("Can't write file.", e);
				} finally {
					if (requestBody != null) {
						requestBody.close();
					}
					if (fw != null) {
						fw.close();
					}
				}


			} else {

				final String body = IOUtils.toString(exchanger.getRequestBody(), UTF_8);
				exchanger.getRequestBody().close();

				logInfo(logger, id, format("Received text:%n%s", body));
				
				try
				{
					launchScript(body);
					sendMessage(exchanger, "OK");
				}
				catch (RhConfigurationException e)
				{
					logError(logger, id, "An error occurred:", e);
					sendMessage(exchanger, 500, "Internal error. " + e.getMessage());
				}
			}
		}
		else if (method.equalsIgnoreCase("GET"))
		{
			if (scriptProcessor == null)
			{
				sendIncorrectRequestMessage(exchanger, "No results. No scripts have been passed.");
				return;
			}

			if (scriptProcessor.isBusy())
			{
				sendBusyMessage(exchanger);
				return;
			}

			logger.info("Session <" + id + ">. Request for result");

			sendMessage(exchanger, serializer.serialize(scriptProcessor.getResult()));
		}
		else if (method.equals("DELETE"))
		{
			logger.info("Session <" + id + ">. Request for session close");

			this.close();

			sendMessage(exchanger, "OK");
		}
	}

	private void sendMessage(HttpExchange exchanger, String message) throws IOException
	{
		sendMessage(exchanger, 200, message);
	}
	
	private void sendMessage(HttpExchange exchanger, int code, String message) throws IOException
	{
		exchanger.sendResponseHeaders(code, message.length());
		OutputStream os = exchanger.getResponseBody();
		os.write(message.getBytes());
		os.close();
	}

	private void sendBusyMessage(HttpExchange exchanger) throws IOException
	{
		RhScriptResult result = new RhScriptResult();
		result.setCode(RhResponseCode.TOOL_BUSY.getCode());
		result.setErrorMessage("Process is busy");
		sendMessage(exchanger, serializer.serialize(result));
	}

	private void sendIncorrectRequestMessage(HttpExchange exchanger, String message) throws IOException
	{
		RhScriptResult result = new RhScriptResult();
		result.setCode(RhResponseCode.INCORRECT_REQUEST.getCode());
		result.setErrorMessage(message);
		sendMessage(exchanger, serializer.serialize(result));
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
			HTTPServer.getServer().removeContext(id);
		}
		catch (IllegalArgumentException ex)
		{
			logger.warn("Error while to closing session <" + id + ">. Nothing to close.");
		}
	}

	public String getId()
	{
		return id;
	}
}
