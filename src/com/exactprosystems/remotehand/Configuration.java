////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand;

import com.exactprosystems.remotehand.web.Browser;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration
{
	private static final Logger logger = Logger.getLogger(Configuration.class);
	public static volatile Configuration instance;
	protected volatile Properties properties;

	public final String PROPERTY_NOT_SET = "Property '%s' is not set. Using default value = '%s'";

	public final String CONFIG_FILE_NAME = "config.ini";
	public final int DEF_SRV_PORT = 8000;
	public final char DEF_DELIMITER = ',';
	public final int DEF_SESSION_EXPIRE = 60; // 1 hour

	public final String PARAM_PORT = "Port",
			PARAM_DELIMITER = "Delimiter", 
			PARAM_SESSIONEXPIRE = "SessionExpire";

	private volatile int httpServerPort;
	private volatile char scriptDelimiter;
	private volatile int sessionExpire;

	protected Configuration()
	{
		if (instance != null) {
			throw new RuntimeException("Configuration is already exist. Use getInstance method");
		}
		instance = this;

		Properties defProperties = new Properties();

		defProperties.setProperty(PARAM_PORT, String.valueOf(DEF_SRV_PORT));
		defProperties.setProperty(PARAM_DELIMITER, String.valueOf(DEF_DELIMITER));
		defProperties.setProperty(PARAM_SESSIONEXPIRE, String.valueOf(DEF_SESSION_EXPIRE));

		properties = new Properties(defProperties);

		try
		{
			FileInputStream fs = new FileInputStream(new File(getConfigFileName()));
			properties.load(fs);
			fs.close();
		}
		catch (IOException e)
		{
			logger.warn(String.format("File '%s' is not found or has wrong format. Using default cofiguration.", CONFIG_FILE_NAME));
		}

		try
		{
			httpServerPort = Integer.parseInt(properties.getProperty(PARAM_PORT));
		}
		catch (Exception ex)
		{
			logger.warn(String.format("Error while reading property '%s'. Using default value = <%s>", PARAM_PORT, DEF_SRV_PORT));
			httpServerPort = DEF_SRV_PORT;
		}

		final String delim = getProperty(PARAM_DELIMITER);
		if (!delim.isEmpty())
			scriptDelimiter = delim.charAt(0);
		else
		{
			logger.warn(String.format(PROPERTY_NOT_SET, PARAM_DELIMITER, String.valueOf(DEF_DELIMITER)));
			scriptDelimiter = DEF_DELIMITER;
		}

		try
		{
			sessionExpire = Integer.parseInt(properties.getProperty(PARAM_SESSIONEXPIRE));
		}
		catch (Exception ex)
		{
			logger.warn(String.format("Error while reading property '%s'. Using default value = <%s>", sessionExpire, DEF_SESSION_EXPIRE));
			httpServerPort = DEF_SRV_PORT;
		}


	}

	public static Configuration getInstance()
	{
		return instance;
	}


	protected String getConfigFileName() {
		return CONFIG_FILE_NAME;
	}

	public String getProperty(String key)
	{
		return properties.getProperty(key);
	}

	public char getDelimiter()
	{
		return scriptDelimiter;
	}

	public int getHttpServerPort()
	{
		return httpServerPort;
	}

	public int getSessionExpire()
	{
		return sessionExpire;
	}

}
