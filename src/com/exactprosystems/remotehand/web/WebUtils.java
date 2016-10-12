////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.remotehand.web;

import org.apache.log4j.Logger;

import java.util.Map;

import static java.lang.String.format;

/**
 * @author anna.bykova.
 */
public class WebUtils
{
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
	
	public static void logInfo(Logger logger, String sessionId, String msg)
	{
		if (logger.isInfoEnabled())
			logger.info(simpleMsg(sessionId, msg));
	}
	
	private static String simpleMsg(String sessionId, String msg)
	{
		return format("<%s> %s", sessionId, msg);
	}
}