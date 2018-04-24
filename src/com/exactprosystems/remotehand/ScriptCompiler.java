/******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand;

import com.csvreader.CsvReader;
import com.exactprosystems.remotehand.http.SessionContext;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alexey.karpukhin on 2/1/16.
 */
public abstract class ScriptCompiler {

	public final String LINE_SEPARATOR = "line.separator";

	public abstract List<Action> build(String scriptFile, Map<String, String> inputParams, SessionContext context) throws ScriptCompileException;

	public List<Action> build(File scriptFile, File inputParams, SessionContext context) throws IOException, ScriptCompileException
	{
		BufferedReader reader = new BufferedReader(new FileReader(scriptFile));
		StringBuffer sb = new StringBuffer();
		String separator = System.getProperty(LINE_SEPARATOR);
		try
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line).append(separator);
			}
		}
		finally
		{
			if (reader != null)
				reader.close();
		}

		String script = sb.toString();

		if (inputParams == null)
			return build(script, null, context);
		if (!inputParams.exists())
			return build(script, null, context);
		Map<String, String> params = readParamsFromFile(inputParams);
		return build(script, params, context);
	}

	public Map<String, String> readParamsFromFile(File inputParams) throws IOException {
		Map<String, String> params = new HashMap<>();
		CsvReader csvReader = null;
		try {
			csvReader = new CsvReader(new FileReader(inputParams));
			csvReader.setDelimiter(',');
			csvReader.setTextQualifier('"');
			while (csvReader.readRecord()) {
				String[] param = csvReader.getValues();
				params.put(param[0], param[1]);
			}
			return params;
		} finally {
			if (csvReader != null)
				csvReader.close();
		}
	}

}
