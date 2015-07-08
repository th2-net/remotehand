////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger
{
	public static final String LOG_PATH = "log.txt";
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static Logger logger;

	public static Logger getLogger()
	{
		Logger localInstance = logger;
		if (localInstance == null)
		{
			synchronized (Logger.class)
			{
				localInstance = logger;
				if (localInstance == null)
					logger = localInstance = new Logger();
			}
		}
		return localInstance;
	}

	private static String getCallerClassName()
	{
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
		for (int i = 1; i < stElements.length; i++)
		{
			StackTraceElement ste = stElements[i];
			if (!ste.getClassName().equals(Logger.class.getName()) && ste.getClassName().indexOf("java.lang.Thread") != 0)
				return ste.getClassName();
		}
		return null;
	}

	public void info(String message)
	{
		final String logLine = buildString(getInfoStringParts(), message);

		addToConsole(logLine);
		addLineToFile(logLine);
	}

	public void error(String message)
	{
		final String logLine = buildString(getErrorStringParts(), message);

		addToConsole(logLine);
		addLineToFile(logLine);
	}

	public void error(Exception exception)
	{
		String logLine = buildString(getErrorStringParts(), exception.getMessage());

		addToConsole(logLine);
		addLineToFile(logLine);

		for (StackTraceElement ste : exception.getStackTrace())
		{
			logLine = buildString(getErrorStringParts(), ste.toString());

			addToConsole(logLine);
			addLineToFile(logLine);
		}
	}

	public void warn(String message)
	{
		final String logLine = buildString(getWarningStringParts(), message);

		addToConsole(logLine);
		addLineToFile(logLine);
	}

	private String[] getInfoStringParts()
	{
		final String result[] = { getCurrentDateTime(), getCallerClassName() };
		return result;
	}

	private String[] getErrorStringParts()
	{
		final String result[] = { getCurrentDateTime(), getCallerClassName(), "ERROR" };
		return result;
	}

	private String[] getWarningStringParts()
	{
		final String result[] = { getCurrentDateTime(), getCallerClassName(), "WARN" };
		return result;
	}

	private String getCurrentDateTime()
	{
		final SimpleDateFormat sdfDate = new SimpleDateFormat(DATE_FORMAT);
		String strDate = sdfDate.format(new Date());
		return strDate;
	}

	private String buildString(String[] preInfo, String message)
	{
		StringBuilder result = new StringBuilder();

		for (int inx = 0; inx < preInfo.length; inx++)
		{
			result.append('[');
			result.append(preInfo[inx]);
			result.append(']');
		}
		result.append(" : ");
		result.append(message);
		result.append("\r\n");

		return result.toString();
	}

	private synchronized void addLineToFile(String line)
	{
		byte[] buffer = line.getBytes();
		RandomAccessFile raf = null;
		FileChannel rwChannel = null;
		try
		{
			raf = new RandomAccessFile(LOG_PATH, "rw");
			rwChannel = raf.getChannel();
			rwChannel.position(rwChannel.size());

			ByteBuffer wrBuf = rwChannel.map(FileChannel.MapMode.READ_WRITE, rwChannel.size(), buffer.length);

			wrBuf.put(buffer);
		}
		catch (IOException ex)
		{
			System.out.println("Error while writing log. Line: '" + line + "'");
		}
		finally
		{
			try
			{
				if (rwChannel != null)
					rwChannel.close();
			}
			catch (IOException e)
			{
			}

			try
			{
				if (raf != null)
					raf.close();
			}
			catch (IOException e)
			{
			}
		}

	}

	private void addToConsole(String line)
	{
		System.out.print(line);
	}
}
