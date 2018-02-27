////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2018, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.remotehand.web.logging;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.Logs;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;

import static com.exactprosystems.remotehand.web.WebConfiguration.DRIVER_LOGS_DIR_NAME;
import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.StringUtils.removeStart;

/**
 * @author anna.bykova.
 * 26 February 2018
 */
public class DriverLoggerThread extends Thread
{
	private static final Logger logger = Logger.getLogger(DriverLoggerThread.class);
	
	private final String sessionId;
	private final WebDriver driver;

	
	public DriverLoggerThread(String sessionId, WebDriver driver)
	{
		super(format("%s-logger", sessionId));
		this.sessionId = sessionId;
		this.driver = driver;
	}

	
	@Override
	public void run()
	{
		Path path = Paths.get(DRIVER_LOGS_DIR_NAME, removeStart(sessionId, "/") + ".log");
		try
		{
			createDirectories(Paths.get(DRIVER_LOGS_DIR_NAME));
			try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(path.toFile(), true))))
			{
				while (!isInterrupted())
				{
					try
					{
						SECONDS.sleep(30);
					}
					catch (InterruptedException e)
					{
						interrupt();
					}

					receiveAndWrite(writer);
				}
			}
		}
		catch (IOException e)
		{
			logger.error(format("I/O error while writing driver logs to file '%s'.", path), e);
		}
		catch (Exception e)
		{
			logger.error(format("Unknown error while writing driver logs to file '%s'.", path), e);
		}
	}
	
	
	private void receiveAndWrite(PrintWriter writer)
	{
		Logs logs = driver.manage().logs();
		for (String type : logs.getAvailableLogTypes())
		{
			for (LogEntry logEntry : logs.get(type))
			{
				writer.printf("[%S] %s%n", type, logEntry);
			}
		}
	}
}
