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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActionsLogger
{
	private static final Logger thisLogger = Logger.getLogger(ActionsLogger.class);
	private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmm");
	public static final String MAIN_LOG_DIR = "logs/";
	public static final String LOG_FILE_EXT = ".log";
	public static final String MODIFY_MSG_PATTERN = "</.*> ";
	public static final String PACKAGE = "com.exactprosystems.remotehand.web.actions";
	private static final PatternLayout LOG_MSG_PATTERN = new PatternLayout("%5m%n");
	private static Logger logger = Logger.getLogger(PACKAGE);
	private static String previousLogDirName = "";
	private static String previousAppenderName = "";

	static
	{
		logger.setLevel(Level.INFO);
	}

	public void init(String sessionId, String actionName)
	{
		Appender fileAppender = getFileAppender(sessionId, actionName);
		logger.addAppender(fileAppender);
	}

	private static String deleteFirstSlash(String string)
	{
		if (string.charAt(0) == '/')
			return string.substring(1);
		return string;
	}

	private Appender getFileAppender(String sessionId, String actionName)
	{
		String currentAppenderName = actionName + sessionId;
		if (logger.getAppender(currentAppenderName) == null)
		{
			try
			{
				RollingFileAppender fileAppender = new MyAppender(LOG_MSG_PATTERN, getLogDirName(sessionId)
						+ actionName + LOG_FILE_EXT, true);
				fileAppender.setName(currentAppenderName);
				fileAppender.setAppend(true);
				fileAppender.activateOptions();
				thisLogger.error("File appender " + currentAppenderName + " for session: " + sessionId + " has been created");

				if (!StringUtils.isEmpty(previousAppenderName))
				{
					logger.removeAppender(previousAppenderName);
					thisLogger.error("File appender " + currentAppenderName + " for session: " + sessionId + " has been removed");
				}
				previousAppenderName = currentAppenderName;
				return fileAppender;
			} catch (IOException e)
			{
				thisLogger.error("Can't create file appender for session: " + sessionId, e);
				return null;
			}
		} else
			return logger.getAppender(currentAppenderName);
	}

	private class MyAppender extends RollingFileAppender
	{
		public MyAppender(Layout layout, String filename, boolean append) throws IOException
		{
			super(layout, filename, append);
		}

		@Override
		protected void subAppend(LoggingEvent event)
		{
			String newMessage = event.getRenderedMessage().replaceFirst(MODIFY_MSG_PATTERN, "");
			LoggingEvent newEvent = new LoggingEvent(event.getFQNOfLoggerClass(), event.getLogger(), event.getTimeStamp(), event.getLevel(),
					newMessage, event.getThreadName(), event.getThrowableInformation(), event.getNDC(), event.getLocationInformation(),
					event.getProperties());
			super.subAppend(newEvent);
		}
	}

	private String getLogDirName(String sessionId)
	{
		String logDirName;
		if (previousLogDirName.contains(sessionId))
			return previousLogDirName;
		else
		{
			logDirName = MAIN_LOG_DIR + SIMPLE_DATE_FORMAT.format(new Date()) + "_" + deleteFirstSlash(sessionId) + "/";
			previousLogDirName = logDirName;
			return logDirName;
		}
	}
}
