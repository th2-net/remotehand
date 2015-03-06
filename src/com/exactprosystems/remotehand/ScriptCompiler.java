package com.exactprosystems.remotehand;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csvreader.CsvReader;
import com.exactprosystems.remotehand.webelements.WebLocator;
import com.exactprosystems.remotehand.webelements.WebLocatorsMapping;

public class ScriptCompiler
{
	private final static Logger logger = Logger.getLogger();

	// csv
	private static final char DELIMITER = Configuration.getInstance().getDelimiter();
	private static final String HEADER_DELIMITER = "#", 
			COMMENT_INDICATOR = "//";

	// script action elements
	public static final String WEB_ACTION = "action";
	public static final String WEB_LOCATOR = "locator";
	public static final String WEB_ID = "webId";
	
	private static WebElementsDictionary dictionary;

	public List<ScriptAction> build(File scriptFile) throws IOException, ScriptCompileException
	{
		BufferedReader reader = new BufferedReader(new FileReader(scriptFile));
		StringBuffer sb = new StringBuffer();
		try
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line).append(System.getProperty("line.separator"));
			}
		}
		finally
		{
			if (reader != null)
				reader.close();
		}

		String script = sb.toString();

		return build(script);
	}

	public List<ScriptAction> build(String script) throws ScriptCompileException
	{
		logger.info("Compiling script...");

		script = script.replace("&#13", System.getProperty("line.separator"));

		CsvReader reader = new CsvReader(new ByteArrayInputStream(script.getBytes()), Charset.defaultCharset());
		reader.setDelimiter(DELIMITER);

		List<ScriptAction> result = new ArrayList<ScriptAction>();
		String[] header = null;
		int lineNumber = 1;
		try
		{
			while (reader.readRecord())
			{
				String[] values = reader.getValues();
				
				if (values[0].startsWith(COMMENT_INDICATOR))
					continue;
				if (values[0].startsWith(HEADER_DELIMITER))
					header = parseValues(values);
				else
				{
					if (header == null)
						throw new ScriptCompileException("Header is not defined for action");

					final ScriptAction action = generateAction(header, values, lineNumber);

					ScriptChecker checker = new ScriptChecker();
					checker.checkParams(action.getWebAction(), action.getWebLocator(), action.getParams());
					checker.checkParams(action.getWebLocator(), action.getParams());

					logger.info(action.toString());

					result.add(action);
				}
				lineNumber++;
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

	private ScriptAction generateAction(String[] header, String[] values, int lineNumber) throws ScriptCompileException
	{
		WebAction webAction;
		WebLocator webLocator = null;
		Map<String, String> params = new HashMap<String, String>();
		
		if (header.length > values.length)
			logger.warn("Line <" + lineNumber + ">: " + header.length + " columns in header, " + values.length + " columns in values. Considering missing values empty by default");
		
		for (int inx = 0; inx < header.length; inx++)
		{
			final String head = header[inx], headLow = head.toLowerCase(), value = (inx < values.length ? values[inx] : null);
			params.put(head, value);
		}
		
		if (params.containsKey(WEB_ID))
		{
			if (params.containsKey(WEB_LOCATOR) || params.containsKey("matcher"))
				throw new ScriptCompileException("Web action '" + params.get(WEB_ACTION) + "' have incompatible params: 'webId' and 'locator' / 'matcher'. Use only 'webId' or 'locator' and 'matcher'.");
			updateParamsByDictionary(params, params.get(WEB_ID));
		}

		webAction = WebActionsMapping.getInstance().getByName(params.get(WEB_ACTION));
		if (params.get(WEB_LOCATOR) != null)
			webLocator = WebLocatorsMapping.getInstance().getByName(params.get(WEB_LOCATOR));
		params.remove(WEB_ACTION);
		params.remove(WEB_LOCATOR);

		return new ScriptAction(webAction, webLocator, params);
	}
	
	private void updateParamsByDictionary(Map<String, String> params, String id) throws ScriptCompileException
	{
		if (dictionary == null)
			dictionary = new WebElementsDictionary("webdictionary.csv");
		WebElementProperties properties = dictionary.getProperties(id);
		
		if (properties == null)
			throw new ScriptCompileException("Unable to find web element properties by ID: " + id);
		params.put("locator", properties.locator);
		params.put("matcher", properties.matcher);
		params.remove(WEB_ID);
	}
}