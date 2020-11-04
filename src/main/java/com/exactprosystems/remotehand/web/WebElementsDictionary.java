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

package com.exactprosystems.remotehand.web;

import com.csvreader.CsvReader;

import static com.exactprosystems.remotehand.web.WebScriptCompiler.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebElementsDictionary
{
	private static final Logger logger = LoggerFactory.getLogger(WebElementsDictionary.class);
	
	private static final String ID      = "#webId";
	private static final String LOCATOR = "#locator";
	private static final String MATCHER = "#matcher";
	private static final String TYPE    = "#type";
	private static final String DESC    = "#desc";

	public final String LINE_SEPARATOR = "line.separator";
	
	private Map<String, WebElementProperties> storage = new HashMap<String, WebElementProperties>();
	
	public WebElementsDictionary(String dictionary, boolean isFile)
	{
		CsvReader reader;
		if (!isFile)
		{
			dictionary = dictionary.replace("&#13", System.getProperty(LINE_SEPARATOR));
			reader = new CsvReader(new ByteArrayInputStream(dictionary.getBytes()), Charset.defaultCharset());
			processDictionary(reader);
		}
		else
		{
			try
			{
				reader = new CsvReader(dictionary);
				processDictionary(reader);
			}
			catch (FileNotFoundException e)
			{
				logger.error("Error during web dictionary opening. File " + dictionary + " is not found.");
			}
		}
	}
	
	private void processDictionary(CsvReader reader)
	{
		int rowNumber = 0;
		try
		{
			rowNumber++;
			reader.readHeaders();

			rowNumber++;
			while (reader.readRecord())
			{
				if(reader.get(0).startsWith(COMMENT_INDICATOR))
					continue;

				String elementId = reader.get(ID);
				String locator = reader.get(LOCATOR);
				String matcher = reader.get(MATCHER);
				String type = reader.get(TYPE);
				String desc = reader.get(DESC);

				List<String> emptyParams = new ArrayList<String>();
				if (elementId.isEmpty())
					emptyParams.add(ID);
				if (locator.isEmpty())
					emptyParams.add(LOCATOR);
				if (matcher.isEmpty())
					emptyParams.add(MATCHER);
				if (!emptyParams.isEmpty())
				{
					logger.error(String.format("Error during web element dictionary reading. The following required parameters are empty: %s. Row %d",
							emptyParams.toString(), rowNumber));
					continue;
				}

				WebElementProperties properties = new WebElementProperties();
				properties.locator = locator;
				properties.matcher = matcher;
				properties.type = type;
				properties.desc = desc;

				storage.put(elementId, properties);
				rowNumber++;
			}
		}
		catch (IOException e)
		{
			logger.error("Error during web elements dictionary reading. Row: " + rowNumber);
		}
		finally
		{
			if (reader != null)
				reader.close();
		}
	}
	
	public WebElementProperties getProperties(String elementId)
	{
		return storage.get(elementId);
	}
}
