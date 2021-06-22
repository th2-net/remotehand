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

package com.exactpro.remotehand.windows;

import com.csvreader.CsvReader;
import com.exactpro.remotehand.Action;
import com.exactpro.remotehand.utils.RhUtils;
import com.exactpro.remotehand.ScriptCompileException;
import com.exactpro.remotehand.ScriptCompiler;
import com.exactpro.remotehand.sessions.SessionContext;
import com.exactpro.remotehand.web.WebScriptCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.exactpro.remotehand.web.WebScriptCompiler.EXECUTE;

/**
 *  TODO Class need refactoring. Need improving parent class...
 * 
 */
public class WindowsScriptCompiler extends ScriptCompiler {

	private static final Logger logger = LoggerFactory.getLogger(WindowsScriptCompiler.class);

	public static final String WIN_ACTION = "action";
	public static final String WIN_LOCATOR = "locator";
	public static final String WIN_ID = "id";
	public static final String WIN_MATCHER = "matcher";
	
	private boolean isEmpty (String[] empty) {
		for (String s : empty) {
			if (s != null && !s.isEmpty())
				return false;
		}
		return true;
	}
	
	@Override
	//FIXME: this method contains much copypaste from web script compile. Need refactoring
	public List<Action> build(String script, Map<String, String> inputParams, SessionContext context) throws ScriptCompileException {
		WindowsSessionContext webSessionContext = (WindowsSessionContext) context;
		String sessionId = webSessionContext.getSessionId();
		RhUtils.logInfo(logger, sessionId, "Compiling script...");
		
		script = script.replace(WebScriptCompiler.SCRIPT_LINE_SEPARATOR, System.getProperty(LINE_SEPARATOR));

		CsvReader reader = new CsvReader(new ByteArrayInputStream(script.getBytes()), Charset.defaultCharset());
		reader.setDelimiter(WebScriptCompiler.DELIMITER);
		reader.setTextQualifier(WebScriptCompiler.TEXT_QUALIFIER);

		List<Action> result = new ArrayList<Action>();
		String[] header = null;
		int lineNumber = 0;
		try
		{
			while (reader.readRecord())
			{
				lineNumber++;
				String[] values = reader.getValues();
				if (isEmpty(values)) {
					continue;
				}

				if (values[0].startsWith(WebScriptCompiler.COMMENT_INDICATOR))
					continue;

				if (values[0].startsWith(WebScriptCompiler.HEADER_DELIMITER))
					header = parseValues(values);
				else
				{
					if (header == null)
						throw new ScriptCompileException("Header is not defined for action");

					if (inputParams != null) {
						for (int i = 0; i < values.length; i++) {
							if (values[i].startsWith("%") && values[i].endsWith("%")) {
								String key = values[i].substring(1, values[i].length() - 1);
								values[i] = inputParams.get(key);
							}
						}
					}

					final WindowsAction action = generateAction(header, values, lineNumber, webSessionContext);

					RhUtils.logInfo(logger, sessionId, String.format("%s: %s",
							action.getClass().getSimpleName(), action.getParams()));

					result.add(action);
				}
			}
			reader.close();
		}
		catch (Exception ex1)
		{
			logger.error("Line " + lineNumber + " error", ex1);
			throw new ScriptCompileException("Line <" + lineNumber + ">: " + ex1.getMessage());
		}

		return result;
		
	}


	//FIXME: copied from WEB
	private String[] parseValues(String[] rowValues)
	{
		String[] result = new String[rowValues.length];

		for (int inx = 0; inx < rowValues.length; inx++)
		{
			String rowValue = rowValues[inx];
			if (rowValue.startsWith(WebScriptCompiler.HEADER_DELIMITER))
				rowValue = rowValue.substring(WebScriptCompiler.HEADER_DELIMITER.length());
			result[inx] = rowValue;
		}

		return result;
	}

	//FIXME: copied from WEB
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
		return foundAt == -1 || foundAt >= values.length || RhUtils.YES.contains(values[foundAt].toLowerCase());
	}

	private WindowsAction generateAction(String[] header, String[] values, int lineNumber, WindowsSessionContext context)
			throws ScriptCompileException {
		WindowsAction winAction;
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
			String value = (inx < values.length ? values[inx] : null);
			params.put(name, value);
		}

		winAction = WindowsActionsMapping.getByName(params.get(WIN_ACTION));
		params.remove(WIN_ACTION);

		winAction.init(context, params, lineNumber, params.remove(WIN_ID));
		return winAction;
	}
}
