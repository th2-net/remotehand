/*
 * Copyright 2020-2020 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
