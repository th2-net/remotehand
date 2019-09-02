/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration
{
	private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
	protected static volatile Configuration instance = null;
	protected volatile Properties properties;

	public final String PROPERTY_NOT_SET = "Property '%s' is not set. Using default value = '%s'";

	public final String CONFIG_FILE_NAME = "config.ini";
	public final int DEF_PORT = 8008;
	public final char DEF_DELIMITER = ',';
	public final char DEF_TEXT_QUALIFIER = '"';
	public final int DEF_SESSION_EXPIRE = 10; // 10 minutes
	public final int DEF_DRIVER_POOL_SIZE = 1;
	public final String DEF_FILE_STORAGE = "generated/",
			DEF_HOST = "localhost";

	public final String PARAM_HOST = "Host",
			PARAM_PORT = "Port",
			PARAM_DELIMITER = "Delimiter",
			PARAM_TEXT_QUALIFIER = "TextQualifier",
			PARAM_SESSIONEXPIRE = "SessionExpire",
			PARAM_FILE_STORAGE = "DefaultFileStorage",
			PARAM_DRIVER_POOL_SIZE = "WebDriverPoolSize";

	private volatile String host;
	private volatile int port;
	private volatile char scriptDelimiter;
	private volatile char scriptTextQualifier;
	private volatile int sessionExpire;
	private volatile int driverPoolSize;
	private volatile File fileStorage;
	
	protected Configuration(CommandLine commandLine)
	{
		if (instance != null) {
			throw new RuntimeException("Configuration is already exist. Use getInstance method");
		}
		instance = this;

		properties = new Properties(getDefaultProperties());

		String configFileName = commandLine.getOptionValue(Starter.CONFIG_PARAM, CONFIG_FILE_NAME);
		logger.info(String.format("Using configuration file '%s'", configFileName));
		
		FileInputStream fs = null;
		try
		{
			fs = new FileInputStream(new File(configFileName));
			properties.load(fs);
		}
		catch (IOException e)
		{
			logger.warn(String.format("File '%s' is not found or has wrong format. Using default configuration.", configFileName));
		}
		finally
		{
			try
			{
				if (fs != null)
					fs.close();
			}
			catch (IOException e)
			{
				logger.warn(String.format("An error occurred while closing file '%s'.", configFileName), e);
			}
		}
		
		
		host = properties.getProperty(PARAM_HOST);
		if (StringUtils.isBlank(host))
		{
			host = DEF_HOST;
			logger.info(String.format("Property '%s' is empty. Using default value = <%s>", PARAM_HOST, host));
		}
		
		try
		{
			port = Integer.parseInt(properties.getProperty(PARAM_PORT));
		}
		catch (Exception ex)
		{
			logger.warn(String.format("Error while reading property '%s'. Using default value = <%s>", PARAM_PORT, DEF_PORT));
			port = DEF_PORT;
		}

		final String delim = getProperty(PARAM_DELIMITER);
		if (!delim.isEmpty())
			scriptDelimiter = delim.charAt(0);
		else
		{
			logger.warn(String.format(PROPERTY_NOT_SET, PARAM_DELIMITER, String.valueOf(DEF_DELIMITER)));
			scriptDelimiter = DEF_DELIMITER;
		}
		
		String qualifier = getProperty(PARAM_TEXT_QUALIFIER);
		if (qualifier != null && !qualifier.isEmpty())
			scriptTextQualifier = qualifier.charAt(0);
		else 
		{
			logger.warn(String.format(PROPERTY_NOT_SET, PARAM_TEXT_QUALIFIER, DEF_TEXT_QUALIFIER));
			scriptTextQualifier = DEF_TEXT_QUALIFIER;
		}

		try
		{
			sessionExpire = Integer.parseInt(properties.getProperty(PARAM_SESSIONEXPIRE));
		}
		catch (Exception ex)
		{
			logger.warn(String.format("Error while reading property '%s'. Using default value = <%s>", sessionExpire, DEF_SESSION_EXPIRE));
			sessionExpire = DEF_SESSION_EXPIRE;
		}

		try
		{
			String fileStorageString = properties.getProperty(PARAM_FILE_STORAGE);
			if (fileStorageString == null || fileStorageString.trim().isEmpty()) {
				logger.info(String.format("Property '%s' is empty. Using default value = <%s>", PARAM_FILE_STORAGE, DEF_FILE_STORAGE));
				fileStorageString = DEF_FILE_STORAGE;
			}

			fileStorage = new File(fileStorageString);
		}
		catch (Exception ex)
		{
			logger.warn(String.format("Error while reading property '%s'. Using default value = <%s>", PARAM_FILE_STORAGE, DEF_FILE_STORAGE));
			fileStorage = new File(DEF_FILE_STORAGE);
		}

		try
		{
			driverPoolSize = Integer.parseInt(properties.getProperty(PARAM_DRIVER_POOL_SIZE));
		}
		catch (Exception ex)
		{
			logger.warn("Error while reading property '{}'. Using default value = <{}>", PARAM_DRIVER_POOL_SIZE,
			            DEF_DRIVER_POOL_SIZE);
			driverPoolSize = DEF_DRIVER_POOL_SIZE;
		}
	}
	
	protected Properties getDefaultProperties()
	{
		Properties defProperties = new Properties();
		defProperties.setProperty(PARAM_PORT, String.valueOf(DEF_PORT));
		defProperties.setProperty(PARAM_DELIMITER, String.valueOf(DEF_DELIMITER));
		defProperties.setProperty(PARAM_TEXT_QUALIFIER, String.valueOf(DEF_TEXT_QUALIFIER));
		defProperties.setProperty(PARAM_SESSIONEXPIRE, String.valueOf(DEF_SESSION_EXPIRE));
		return defProperties;
	}

	public static Configuration getInstance()
	{
		return instance;
	}


	public String getProperty(String key)
	{
		return properties.getProperty(key);
	}

	public char getDelimiter()
	{
		return scriptDelimiter;
	}
	
	public char getTextQualifier()
	{
		return scriptTextQualifier;
	}
	
	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}

	public int getSessionExpire()
	{
		return sessionExpire;
	}

	public File getFileStorage() {
		return fileStorage;
	}

	public int getDriverPoolSize()
	{
		return driverPoolSize;
	}
}