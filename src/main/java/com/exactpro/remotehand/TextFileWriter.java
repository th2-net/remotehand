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

package com.exactpro.remotehand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TextFileWriter
{
	private static final Logger logger = LoggerFactory.getLogger(TextFileWriter.class);
	
	private static TextFileWriter writer = new TextFileWriter();

	private String content;

	public static TextFileWriter getInstance()
	{
		return writer;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public void writeFile(File file)
	{
		if (content == null || content.isEmpty())
		{
			logger.warn("File not created. Nothing to write.");
			return;
		}

		BufferedWriter writer = null;
		try
		{
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(content);
		}
		catch (IOException e)
		{
			logger.error("File " + file.getName() + " does not exist.", e);
		}
		finally
		{
			content = null;

			logger.info("Result file '" + file.getName() + "' has been created");

			try
			{
				if (writer != null)
					writer.close();
			}
			catch (IOException e)
			{
				logger.error("Error while closing file " + file.getName(), e);
			}
		}
	}
}
