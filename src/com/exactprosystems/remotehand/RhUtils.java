////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2017, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.remotehand;

import com.exactprosystems.remotehand.web.WebScriptCompiler;
import org.apache.log4j.Logger;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriverException;

import java.util.Map;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.contains;

/**
 * @author anna.bykova.
 */
public class RhUtils
{
	public static final String SESSION_FOR_FILE_MODE = "Main";
	
	public static boolean getBooleanOrDefault(Map<String, String> params, String name, boolean defaultValue)
	{
		String value = params.get(name);
		if (value == null || value.isEmpty())
			return defaultValue;
		else 
			return WebScriptCompiler.YES.contains(value.toLowerCase());
	}
	
	public static void logError(Logger logger, String sessionId, String msg)
	{
		logger.error(simpleMsg(sessionId, msg));
	}

	public static void logError(Logger logger, String sessionId, String msg, Throwable t)
	{
		logger.error(simpleMsg(sessionId, msg), t);
	}
	
	public static void logInfo(Logger logger, String sessionId, String msg)
	{
		if (logger.isInfoEnabled())
			logger.info(simpleMsg(sessionId, msg));
	}
	
	private static String simpleMsg(String sessionId, String msg)
	{
		return format("<%s> %s", sessionId, msg);
	}

	public static boolean isBrowserNotReachable(WebDriverException e)
	{
		String msg = e.getMessage();
		return ((e instanceof NoSuchWindowException) && contains(msg, "target window already closed"))
				|| contains(msg, "not reachable");
	}
}
