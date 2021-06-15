/*
 * Copyright 2020-2021 Exactpro (Exactpro Systems Limited)
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

package com.exactpro.remotehand;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.contains;

public class RhUtils {

	private static final Logger logger = LoggerFactory.getLogger(RhUtils.class);
	public static final String SESSION_FOR_FILE_MODE = "Main";
	
	
	public static final List<String> YES = Arrays.asList("y", "yes", "t", "true", "1", "+");
	public static final List<String> NO = Arrays.asList("n", "no", "f", "false", "0", "-");

	public static boolean getBooleanOrDefault(Map<String, String> params, String name, boolean defaultValue)
	{
		String value = params.get(name);
		if (value == null || value.isEmpty())
			return defaultValue;
		else 
			return YES.contains(value.toLowerCase());
	}

	public static int getIntegerOrDefault(Map<String, String> params, String name, int defaultValue) throws ScriptExecuteException {
		String value = params.get(name);
		if (value == null || value.isEmpty()) {
			return defaultValue;
		} else {
			try {
				return Integer.parseInt(value);
			} catch (Exception e) {
				throw new ScriptExecuteException("Incorrect parameter: " + name + ". Number is required");
			}
		}
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

	public static List<Filter> buildFilters(String filters) throws ScriptExecuteException {
		if (StringUtils.isEmpty(filters))
			return Collections.emptyList();

		String[] splitFilters = filters.split(";");
		List<Filter> result = new ArrayList<>(splitFilters.length);
		for (String splitFilter : splitFilters) {
			String[] kvPair = splitFilter.split("=", -1);
			if (kvPair.length == 3) {
				int index;
				try {
					index = Integer.parseInt(kvPair[2]);
				} catch (NumberFormatException e) {
					throw new ScriptExecuteException("Cannot extract index: " + kvPair[2]);
				}
				if (index < 0) {
					throw new ScriptExecuteException("Index cannot be less than 0: " + index);
				}
				result.add(new Filter(kvPair[0], index, kvPair[1]));
			} else if (kvPair.length == 2) {
				result.add(new Filter(kvPair[0], kvPair[1]));
			} else {
				logger.warn("Cannot process filters: {}", splitFilter);
			}
		}

		return result;
	}
	
	public static class Filter {
		
		public final String name;
		public final Integer index;
		public final String value;

		public Filter(String name, Integer index, String value) {
			this.name = name;
			this.index = index;
			this.value = value;
		}

		public Filter(String name, String value) {
			this.name = name;
			this.index = null;
			this.value = value;
		}
	}
}
