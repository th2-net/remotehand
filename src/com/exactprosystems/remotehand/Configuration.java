////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration
{
	private static final Logger logger = Logger.getLogger(Configuration.class);
	public static volatile Configuration instance;
	private volatile Properties properties;

	public static final String PROPERTY_NOT_SET = "Property '%s' is not set. Using default value = '%s'";

	public static final String CONFIG_FILE_NAME = "config.ini";
	public static final int DEF_SRV_PORT = 8000;
	public static final char DEF_DELIMITER = ',';
	public static final int DEF_SESSION_EXPIRE = 60; // 1 hour
	public static final Browser DEF_BROWSER = Browser.FIREFOX;
	public static final String DEF_IEDRIVER_PATH = "IEDriverServer.exe", 
			DEF_CHROMEDRIVER_PATH = "chromedriver.exe", 
			DEF_PROXY = "";

	public static final String PARAM_PORT = "Port", 
			PARAM_DELIMITER = "Delimiter", 
			PARAM_SESSIONEXPIRE = "SessionExpire", 
			PARAM_BROWSER = "Browser", 
			PARAM_IEDRIVERPATH = "IEDriverPath",
			PARAM_CHROMEDRIVERPATH = "ChromeDriverPath", 
			PARAM_HTTPPROXY = "HttpProxy", 
			PARAM_SSLPROXY = "SslProxy", 
			PARAM_FTPPROXY = "FtpProxy", 
			PARAM_SOCKSPROXY = "SocksProxy", 
			PARAM_NOPROXY = "NoProxy";

	private volatile int httpServerPort;
	private volatile char scriptDelimiter;
	private volatile int sessionExpire;
	private volatile Browser browserToUse;
	private volatile String ieDriverFileName, chromeDriverFileName, httpProxySetting, sslProxySetting, ftpProxySetting, socksProxySetting, noProxySetting;

	private Configuration()
	{
		Properties defProperties = new Properties();

		defProperties.setProperty(PARAM_PORT, String.valueOf(DEF_SRV_PORT));
		defProperties.setProperty(PARAM_DELIMITER, String.valueOf(DEF_DELIMITER));
		defProperties.setProperty(PARAM_SESSIONEXPIRE, String.valueOf(DEF_SESSION_EXPIRE));
		defProperties.setProperty(PARAM_BROWSER, DEF_BROWSER.getLabel());
		defProperties.setProperty(PARAM_IEDRIVERPATH, DEF_IEDRIVER_PATH);
		defProperties.setProperty(PARAM_HTTPPROXY, DEF_PROXY);
		defProperties.setProperty(PARAM_SSLPROXY, DEF_PROXY);
		defProperties.setProperty(PARAM_FTPPROXY, DEF_PROXY);
		defProperties.setProperty(PARAM_SOCKSPROXY, DEF_PROXY);
		defProperties.setProperty(PARAM_NOPROXY, DEF_PROXY);

		properties = new Properties(defProperties);

		try
		{
			FileInputStream fs = new FileInputStream(new File(CONFIG_FILE_NAME));
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

		browserToUse = Browser.valueByLabel(properties.getProperty(PARAM_BROWSER));
		if (browserToUse == Browser.INVALID)
		{
			logger.warn(String.format("Property '%s' is not set or has invalid value. Using default value = '%s'", PARAM_BROWSER, DEF_BROWSER.getLabel()));
			browserToUse = DEF_BROWSER;
		}

		ieDriverFileName = properties.getProperty(PARAM_IEDRIVERPATH);
		if ((ieDriverFileName == null) || (ieDriverFileName.isEmpty()))
		{
			logger.warn(String.format(PROPERTY_NOT_SET, PARAM_IEDRIVERPATH, DEF_IEDRIVER_PATH));
			ieDriverFileName = DEF_IEDRIVER_PATH;
		}

		chromeDriverFileName = properties.getProperty(PARAM_CHROMEDRIVERPATH);
		if ((chromeDriverFileName == null) || (chromeDriverFileName.isEmpty()))
		{
			logger.warn(String.format(PROPERTY_NOT_SET, PARAM_CHROMEDRIVERPATH, DEF_CHROMEDRIVER_PATH));
			chromeDriverFileName = DEF_CHROMEDRIVER_PATH;
		}

		httpProxySetting = properties.getProperty(PARAM_HTTPPROXY);
		if ((httpProxySetting == null) || (httpProxySetting.isEmpty()))
		{
			logger.warn(String.format(PROPERTY_NOT_SET, PARAM_HTTPPROXY, DEF_PROXY));
			httpProxySetting = DEF_PROXY;
		}

		sslProxySetting = properties.getProperty(PARAM_SSLPROXY);
		if ((sslProxySetting == null) || (sslProxySetting.isEmpty()))
		{
			logger.warn(String.format(PROPERTY_NOT_SET, PARAM_SSLPROXY, DEF_PROXY));
			sslProxySetting = DEF_PROXY;
		}

		ftpProxySetting = properties.getProperty(PARAM_FTPPROXY);
		if ((ftpProxySetting == null) || (ftpProxySetting.isEmpty()))
		{
			logger.warn(String.format(PROPERTY_NOT_SET, PARAM_FTPPROXY, DEF_PROXY));
			ftpProxySetting = DEF_PROXY;
		}

		socksProxySetting = properties.getProperty(PARAM_SOCKSPROXY);
		if ((socksProxySetting == null) || (socksProxySetting.isEmpty()))
		{
			logger.warn(String.format(PROPERTY_NOT_SET, PARAM_SOCKSPROXY, DEF_PROXY));
			socksProxySetting = DEF_PROXY;
		}

		noProxySetting = properties.getProperty(PARAM_NOPROXY);
		if ((noProxySetting == null) || (noProxySetting.isEmpty()))
		{
			logger.warn(String.format(PROPERTY_NOT_SET, PARAM_NOPROXY, DEF_PROXY));
			noProxySetting = DEF_PROXY;
		}
	}

	public static Configuration getInstance()
	{
		Configuration localInstance = instance;
		if (localInstance == null)
		{
			synchronized (Configuration.class)
			{
				localInstance = instance;
				if (localInstance == null)
					instance = localInstance = new Configuration();
			}
		}
		return localInstance;
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

	public Browser getBrowserToUse()
	{
		return browserToUse;
	}

	public String getIeDriverFileName()
	{
		return ieDriverFileName;
	}

	public String getChromeDriverFileName()
	{
		return chromeDriverFileName;
	}

	public String getHttpProxySetting()
	{
		return httpProxySetting;
	}

	public String getSslProxySetting()
	{
		return sslProxySetting;
	}

	public String getFtpProxySetting()
	{
		return ftpProxySetting;
	}

	public String getSocksProxySetting()
	{
		return socksProxySetting;
	}

	public String getNoProxySetting()
	{
		return noProxySetting;
	}
}
