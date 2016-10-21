////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.http;

import java.io.*;
import java.util.List;

import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.IRemoteHandManager;
import com.exactprosystems.remotehand.ScriptProcessorThread;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.entity.mime.Header;
import org.apache.http.entity.mime.MultipartEntity;
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
					int readBytes = -1;
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

				BufferedReader in = new BufferedReader(new InputStreamReader(exchanger.getRequestBody()));

				String line;
				StringBuilder buff = new StringBuilder();
				while ((line = in.readLine()) != null)
					buff.append(line);
				in.close();

				final String body = buff.toString();

				//			MultipartEntity

				sendMessage(exchanger, "OK");

				logger.info("Session <" + id + ">. Received text:");
				logger.info(body);

				launchScript(body);
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
