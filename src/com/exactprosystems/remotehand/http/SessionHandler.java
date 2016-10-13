////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.exactprosystems.remotehand.IRemoteHandManager;
import com.exactprosystems.remotehand.ScriptProcessorThread;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.log4j.Logger;

public class SessionHandler implements HttpHandler
{
	private static final Logger logger = Logger.getLogger(SessionHandler.class);

	private final String id;

	private ScriptProcessorThread scriptProcessor = null;
	private IRemoteHandManager manager;

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
			BufferedReader in = new BufferedReader(new InputStreamReader(exchanger.getRequestBody()));

			String line;
			StringBuilder buff = new StringBuilder();
			while ((line = in.readLine()) != null)
				buff.append(line);
			in.close();

			final String body = buff.toString();

			sendMessage(exchanger, "OK");

			logger.info("Session <" + id + ">. Received text:");
			logger.info(body);

			launchScript(body);
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

			if (scriptProcessor.getResult() != null)
				sendMessage(exchanger, scriptProcessor.getResult());
			else
				sendMessage(exchanger, "");
		}
		else if (method.equals("DELETE"))
		{
			if (scriptProcessor == null)
			{
				sendIncorrectRequestMessage(exchanger, "No results. No scripts have been passed.");
				return;
			}
			logger.info("Session <" + id + ">. Request for session close");

			this.close();

			sendMessage(exchanger, "OK");
		}
	}

	private void sendMessage(HttpExchange exchanger, String message) throws IOException
	{
		exchanger.sendResponseHeaders(200, message.length());
		OutputStream os = exchanger.getResponseBody();
		os.write(message.getBytes());
		os.close();
	}

	private void sendBusyMessage(HttpExchange exchanger) throws IOException
	{
		final String response = ErrorRespondent.getRespondent().error(new ThreadBusyException("Process is busy"));
		sendMessage(exchanger, response);
	}

	private void sendIncorrectRequestMessage(HttpExchange exchanger, String message) throws IOException
	{
		final String response = ErrorRespondent.getRespondent().error(new IncorrectRequestException(message));
		sendMessage(exchanger, response);
	}

	private void launchScript(String script)
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
