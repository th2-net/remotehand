////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
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

import com.exactprosystems.remotehand.web.webelements.WebLocator;
import com.exactprosystems.remotehand.web.webelements.WebLocatorsMapping;
import org.apache.log4j.Logger;

public class WebScriptCompiler extends ScriptCompiler
{
	private static final Logger logger = Logger.getLogger(WebScriptCompiler.class);

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
			
	private static WebElementsDictionary dictionary;
	
	private boolean findLocalDict = true;
	
//	public void setDictionary(WebElementsDictionary dictionary)
//	{
//		if (dictionary == null)
//			findLocalDict = false;
//		this.dictionary = dictionary;
//	}

	private WebDriverManager webDriverManager = null;

	public WebScriptCompiler (WebDriverManager webDriverManager) {
		this.webDriverManager = webDriverManager;
	}

	public List<Action> build(String script) throws ScriptCompileException
	{
		logger.info("Compiling script...");

		if (script.contains("#type") || script.contains("#desc"))
		{
			dictionary = new WebElementsDictionary(script, false);
			logger.info("Web dictionary applied");
			return new ArrayList<Action>();
		}

		script = script.replace("&#13", System.getProperty(LINE_SEPARATOR));

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
						final WebAction action = generateAction(header, values, lineNumber);

						WebScriptChecker checker = new WebScriptChecker();
						checker.checkParams(action, action.getWebLocator(), action.getParams());
						checker.checkParams(action.getWebLocator(), action.getParams());

						logger.info(action.toString());

						result.add(action);
					}
					else
						logger.info(String.format("Action at line %d will be skipped.", lineNumber));
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

	private WebAction generateAction(String[] header, String[] values, int lineNumber) throws ScriptCompileException
	{
		WebAction webAction;
		WebLocator webLocator = null;
		Map<String, String> params = new HashMap<String, String>();
		
		if (header.length > values.length)
			logger.warn("Line <" + lineNumber + ">: " + header.length + " columns in header, " + values.length + " columns in values. Considering missing values empty by default");
		
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
			updateParamsByDictionary(params, params.get(WEB_ID));
		}

		webAction = WebActionsMapping.getInstance().getByName(params.get(WEB_ACTION));
		if (params.get(WEB_LOCATOR) != null)
			webLocator = WebLocatorsMapping.getInstance().getByName(params.get(WEB_LOCATOR));
		params.remove(WEB_ACTION);
		params.remove(WEB_LOCATOR);

		webAction.init(this.webDriverManager.getWebDriver(), webLocator, params);
		return webAction;
	}
	
	private void updateParamsByDictionary(Map<String, String> params, String id) throws ScriptCompileException
	{
		if (dictionary == null)
		{
			if (!findLocalDict)
				throw new ScriptCompileException("Web dictionary has not been sent. Script cannot be processed.");
			
			File dict = new File(DEFAULT_DICT_NAME);
			if (dict.exists())
				dictionary = new WebElementsDictionary(DEFAULT_DICT_NAME, true);
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
}
