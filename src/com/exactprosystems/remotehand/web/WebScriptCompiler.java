////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2017, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import com.csvreader.CsvReader;
import com.exactprosystems.remotehand.*;

import com.exactprosystems.remotehand.http.SessionContext;
import com.exactprosystems.remotehand.web.webelements.WebLocator;
import com.exactprosystems.remotehand.web.webelements.WebLocatorsMapping;
import org.apache.log4j.Logger;

public class WebScriptCompiler extends ScriptCompiler
{
	private static final Logger logger = Logger.getLogger(WebScriptCompiler.class);

	private static final String SCRIPT_LINE_SEPARATOR = "&#13";
	
	// csv
	private static final char DELIMITER = Configuration.getInstance().getDelimiter();
	private static final char TEXT_QUALIFIER = Configuration.getInstance().getTextQualifier();
	private static final String HEADER_DELIMITER = "#";
	public static final String COMMENT_INDICATOR = "//";

	// script action elements
	public static final String WEB_ACTION = "action";
	public static final String WEB_LOCATOR = "locator";
	public static final String WEB_MATCHER = "matcher";
	public static final String WEB_ID = "webid";
	public static final String EXECUTE = "execute";
	
	public static final String DEFAULT_DICT_NAME = "webdictionary.csv"; 
	
	public static final List<String> YES = Arrays.asList("y", "yes", "t", "true", "1", "+");
	public static final List<String> NO = Arrays.asList("n", "no", "f", "false", "0", "-");

	@Override
	public List<Action> build(String script, SessionContext context) throws ScriptCompileException
	{
		WebSessionContext webSessionContext = (WebSessionContext) context;
		String sessionId = webSessionContext.getSessionId();
		RhUtils.logInfo(logger, sessionId, "Compiling script...");

		if (isDictionaryUsed(script))
		{
			webSessionContext.setDictionary(new WebElementsDictionary(script, false));
			RhUtils.logInfo(logger, sessionId, "Web dictionary applied");
			return new ArrayList<Action>();
		}

		script = script.replace(SCRIPT_LINE_SEPARATOR, System.getProperty(LINE_SEPARATOR));

		CsvReader reader = new CsvReader(new ByteArrayInputStream(script.getBytes()), Charset.defaultCharset());
		reader.setDelimiter(DELIMITER);
		reader.setTextQualifier(TEXT_QUALIFIER);

		List<Action> result = new ArrayList<Action>();
		String[] header = null;
		int lineNumber = 0;
		try
		{
			while (reader.readRecord())
			{
				lineNumber++;
				String[] values = reader.getValues();
				
				if (values[0].startsWith(COMMENT_INDICATOR))
					continue;

				if (values[0].startsWith(HEADER_DELIMITER))
					header = parseValues(values);
				else
				{
					if (header == null)
						throw new ScriptCompileException("Header is not defined for action");

					if (isExecutable(header, values))
					{
						final WebAction action = generateAction(header, values, lineNumber, webSessionContext);

						WebScriptChecker checker = new WebScriptChecker();
						checker.checkParams(action, action.getWebLocator(), action.getParams());
						checker.checkParams(action.getWebLocator(), action.getParams());

						RhUtils.logInfo(logger, sessionId, String.format("%s: %s",
								action.getClass().getSimpleName(), action.getParams()));

						result.add(action);
					}
					else
						RhUtils.logInfo(logger, sessionId, String.format("Action at line %d will be skipped.", lineNumber));
				}
			}
			reader.close();
		}
		catch (IOException ex1)
		{
			throw new ScriptCompileException("Line <" + lineNumber + ">: " + ex1.getMessage());
		}
		catch (ScriptCompileException ex2)
		{
			throw new ScriptCompileException("Line <" + lineNumber + ">: " + ex2.getMessage());
		}

		return result;
	}

	private String[] parseValues(String[] rowValues)
	{
		String[] result = new String[rowValues.length];

		for (int inx = 0; inx < rowValues.length; inx++)
		{
			String rowValue = rowValues[inx];
			if (rowValue.startsWith(HEADER_DELIMITER))
				rowValue = rowValue.substring(HEADER_DELIMITER.length());
			result[inx] = rowValue;
		}

		return result;
	}

	private WebAction generateAction(String[] header, String[] values, int lineNumber, WebSessionContext context) throws ScriptCompileException
	{
		WebAction webAction;
		WebLocator webLocator = null;
		Map<String, String> params = new HashMap<String, String>();
		
		if (header.length > values.length)
		{
			logger.warn(String.format("<%s> Line <%d>: %d columns in header, %d columns in values. " +
							"Considering missing values empty by default", 
					context.getSessionId(), lineNumber, header.length, values.length));
		}
		
		for (int inx = 0; inx < header.length; inx++)
		{
			String name = header[inx].toLowerCase();
			if (EXECUTE.equals(name))
				continue;
			String value = (inx < values.length ? values[inx] : null);
			params.put(name, value);
		}
		
		if (params.containsKey(WEB_ID))
		{
			if (params.containsKey(WEB_LOCATOR) || params.containsKey(WEB_MATCHER))
				throw new ScriptCompileException(String.format("Web action '%s' has incompatible parameters: '%s' and '%s' + '%s'", 
						params.get(WEB_ACTION), WEB_ID, WEB_LOCATOR, WEB_MATCHER));
			updateParamsByDictionary(params, params.get(WEB_ID), context);
		}

		webAction = WebActionsMapping.getInstance().getByName(params.get(WEB_ACTION));
		if (params.get(WEB_LOCATOR) != null)
			webLocator = WebLocatorsMapping.getInstance().getByName(params.get(WEB_LOCATOR));
		params.remove(WEB_ACTION);
		params.remove(WEB_LOCATOR);

		webAction.init(context, webLocator, params);
		return webAction;
	}
	
	private void updateParamsByDictionary(Map<String, String> params, String id, WebSessionContext context) throws ScriptCompileException
	{
		WebElementsDictionary dictionary = context.getDictionary();
		if (dictionary == null)
		{
			File dict = new File(DEFAULT_DICT_NAME);
			if (dict.exists())
			{
				dictionary = new WebElementsDictionary(DEFAULT_DICT_NAME, true);
				context.setDictionary(dictionary);
			}
			else
				throw new ScriptCompileException("Web dictionary " + DEFAULT_DICT_NAME + " is not found. Script cannot be processed.");
		}

		WebElementProperties properties = dictionary.getProperties(id);
		
		if (properties == null)
			throw new ScriptCompileException(String.format("Unable to find web element properties by %s: %s", WEB_ID, id));
		params.put(WEB_LOCATOR, properties.locator);
		params.put(WEB_MATCHER, properties.matcher);
		params.remove(WEB_ID);
	}
	
	private boolean isExecutable(String[] header, String[] values)
	{
		int foundAt = -1;
		for (int i = 0; i < header.length; i++)
		{
			if (EXECUTE.equalsIgnoreCase(header[i]))
			{
				foundAt = i;
				break;
			}
		}
		return foundAt == -1 || foundAt >= values.length || YES.contains(values[foundAt].toLowerCase());
	}
	
	private boolean isDictionaryUsed(String script)
	{
		String[] lines = script.split(SCRIPT_LINE_SEPARATOR);
		for (String line : lines)
		{
			if (line.startsWith(HEADER_DELIMITER) && (line.contains("#type") || line.contains("#desc")))
				return true;
		}
		return false;
	}
}
