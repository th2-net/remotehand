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
		WebAction webAction = null;
		WebLocator webLocator = null;
		Map<String, String> params = new HashMap<String, String>();

		for (int inx = 0; inx < header.length; inx++)
		{
			final String head = header[inx], headLow = head.toLowerCase(), value = (inx < values.length ? values[inx] : null);

			if (value == null)
			{
				final String mess = "Line <" + lineNumber + ">: " + header.length + " columns in header, " + values.length + " columns in values. Considering missing values empty by default";
				logger.warn(mess);
			}

			if (headLow.equals(WEB_ACTION))
				webAction = WebActionsMapping.getInstance().getByName(value);
			else if (headLow.equals(WEB_LOCATOR))
				webLocator = WebLocatorsMapping.getInstance().getByName(value);
			else
				params.put(head, value);
		}

		return new ScriptAction(webAction, webLocator, params);
	}
}