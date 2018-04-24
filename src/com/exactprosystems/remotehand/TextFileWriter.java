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

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TextFileWriter
{
	private static final Logger logger = Logger.getLogger(TextFileWriter.class);
	
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
