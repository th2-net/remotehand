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

package com.exactpro.remotehand.utils;

import com.exactpro.remotehand.ScriptExecuteException;
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
	
	public static final Set<String> YES = new HashSet<>(Arrays.asList("y", "yes", "t", "true", "1", "+"));
	public static final Set<String> NO = new HashSet<>(Arrays.asList("n", "no", "f", "false", "0", "-"));

	public static boolean getBooleanOrDefault(Map<String, String> params, String name, boolean defaultValue) throws ScriptExecuteException {
		return getBoolean(params, name, false, defaultValue);
	}

	public static boolean getBoolean(Map<String, String> params, String name) throws ScriptExecuteException {
		return getBoolean(params, name, true, false);
	}

	private static boolean getBoolean(Map<String, String> params, String key, boolean mandatory, boolean defaultValue) throws ScriptExecuteException {
		String param = params.get(key);
		if (param == null || param.isEmpty()) {
			if (!mandatory) {
				return defaultValue;
			} else {
				throw new ScriptExecuteException("Param should be specified : " + key);	
			}
		}
		String lcParam = param.toLowerCase();
		if (RhUtils.YES.contains(lcParam)) {
			return true;
		} else if (RhUtils.NO.contains(lcParam)) {
			return false;
		} else {
			throw new ScriptExecuteException(String.format("Invalid value for param %s. boolean required (actual: %s",
					key, param));
		}
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

	public static List<TableFilter> buildTableFilters(String filters) throws ScriptExecuteException {
		if (StringUtils.isEmpty(filters))
			return Collections.emptyList();

		String[] splitFilters = filters.split(";");
		List<TableFilter> result = new ArrayList<>(splitFilters.length);
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
				result.add(new TableFilter(kvPair[0], index, kvPair[1]));
			} else if (kvPair.length == 2) {
				result.add(new TableFilter(kvPair[0], kvPair[1]));
			} else {
				logger.warn("Cannot process filters: {}", splitFilter);
			}
		}

		return result;
	}
}
