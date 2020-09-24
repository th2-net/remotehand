/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.utils;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.exception.ExceptionUtils.getThrowableList;

public class ExceptionUtils
{
	public static final String EOL = "\r\n";


	public static String getDetailedMessage(Throwable e)
	{
		return joinStackTraceMessages(e);
	}


	private static String getRuntimeExceptionMessage(Throwable t)
	{
		String message = t.getClass().getName();

		StackTraceElement[] stackTrace = t.getStackTrace();
		if ((stackTrace != null) && (stackTrace.length != 0))
			message += " at " + stackTrace[0];

		if (t.getMessage() != null)
			message += ": " + t.getMessage();

		return message;
	}

	private static String getMessage(Throwable t)
	{
		if (t instanceof RuntimeException)
			return getRuntimeExceptionMessage(t);
		else
			return (t.getMessage() != null) ? t.getMessage() : "";
	}
	
	private static String joinStackTraceMessages(Throwable cause)
	{
		if (cause == null)
			return "";

		@SuppressWarnings("unchecked") List<Throwable> throwables = getThrowableList(cause);
		if (throwables.size() == 1)
			return getMessage(cause);

		StringBuilder msgBuilder = new StringBuilder();
		for (Throwable t : throwables)
		{
			String msg = getMessage(t);
			if (isEmpty(msg))
				continue;

			if (msgBuilder.indexOf(msg) != -1)
				continue;

			if (msgBuilder.length() > 0)
				msgBuilder.append("Cause: ");
			msgBuilder.append(msg).append(EOL);
		}

		return msgBuilder.toString();
	}
}
