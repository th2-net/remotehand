/******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.web.actions.GetFormFields;

/**
 * Created by alexey.karpukhin on 2/1/16.
 */
public class WebConfiguration extends Configuration{

	private static final Logger logger = LoggerFactory.getLogger(WebConfiguration.class);

	public static final Browser DEF_BROWSER = Browser.FIREFOX;
	public static final String DEF_IEDRIVER_PATH = "IEDriverServer.exe";
	public static final String DEF_CHROMEDRIVER_PATH = "chromedriver.exe";
	public static final String DEF_FIREFOXDRIVER_PATH = "geckodriver.exe";
	public static final String DEF_PROXY = "";
	public static final String DEF_LOCATOR = "xpath";
	public static final String FORM_PARSER_CONFIG_FILE = "formParser.properties";
	
	public static final String PARAM_BROWSER = "Browser";
	public static final String PARAM_IEDRIVERPATH = "IEDriverPath";
	public static final String PARAM_CHROMEDRIVERPATH = "ChromeDriverPath";
	public static final String PARAM_FIREFOXDRIVERPATH = "FirefoxDriverPath";
	public static final String PARAM_HTTPPROXY = "HttpProxy";
	public static final String PARAM_SSLPROXY = "SslProxy";
	public static final String PARAM_FTPPROXY = "FtpProxy";
	public static final String PARAM_SOCKSPROXY = "SocksProxy";
	public static final String PARAM_NOPROXY = "NoProxy";
	public static final String PARAM_PROFILE = "Profile";
	public static final String PARAM_BINARY = "Binary";
	public static final String PARAM_DEFAULT_LOCATOR = "DefaultLocator";
	
	public static final String BROWSER_LOGGING_LEVEL = "BrowserLoggingLevel";
	public static final String CLIENT_LOGGING_LEVEL = "ClientLoggingLevel";
	public static final String DRIVER_LOGGING_LEVEL = "DriverLoggingLevel";
	public static final String PERFORMANCE_LOGGING_LEVEL = "PerformanceLoggingLevel";
	
	public static final String SCREENSHOTS_DIR_NAME = "screenshots";
	public static final String DRIVER_LOGS_DIR_NAME = "driverLogs";
	
	private static final Level DEF_LOG_LEVEL = Level.OFF;

	private volatile Browser browserToUse;
	private final String ieDriverFileName, chromeDriverFileName, firefoxDriverFileName, httpProxySetting, sslProxySetting, 
		ftpProxySetting, socksProxySetting, noProxySetting, profilePath, binary, defaultLocator;
	
	private final Level browserLoggingLevel;
	private final Level clientLoggingLevel;
	private final Level driverLoggingLevel;
	private final Level performanceLoggingLevel;
	
	private final Properties formParserProperties;
	private boolean isProxySettingsSet;
	private volatile File downloadsDir;

	protected WebConfiguration(CommandLine commandLine) {
		super(commandLine);

		browserToUse = Browser.valueByLabel(properties.getProperty(PARAM_BROWSER));
		if (browserToUse == Browser.INVALID)
		{
			logger.warn(format("Property '%s' is not set or has invalid value. Using default value = '%s'", PARAM_BROWSER, DEF_BROWSER.getLabel()));
			browserToUse = DEF_BROWSER;
		}
		
		profilePath = loadProperty(properties, PARAM_PROFILE, "temporary profile");
		
		ieDriverFileName = loadProperty(properties, PARAM_IEDRIVERPATH, DEF_IEDRIVER_PATH);
		chromeDriverFileName = loadProperty(properties, PARAM_CHROMEDRIVERPATH, DEF_CHROMEDRIVER_PATH);
		firefoxDriverFileName = loadProperty(properties, PARAM_FIREFOXDRIVERPATH, DEF_FIREFOXDRIVER_PATH);
		binary = loadProperty(properties, PARAM_BINARY, "");
		defaultLocator = loadProperty(properties, PARAM_DEFAULT_LOCATOR, DEF_LOCATOR);
		
		browserLoggingLevel = loadLogLevel(properties, BROWSER_LOGGING_LEVEL);
		clientLoggingLevel = loadLogLevel(properties, CLIENT_LOGGING_LEVEL);
		driverLoggingLevel = loadLogLevel(properties, DRIVER_LOGGING_LEVEL);
		performanceLoggingLevel = loadLogLevel(properties, PERFORMANCE_LOGGING_LEVEL);

		httpProxySetting = loadProxySetting(properties, PARAM_HTTPPROXY);
		sslProxySetting = loadProxySetting(properties, PARAM_SSLPROXY);
		ftpProxySetting = loadProxySetting(properties, PARAM_FTPPROXY);
		socksProxySetting = loadProxySetting(properties, PARAM_SOCKSPROXY);
		noProxySetting = loadProxySetting(properties, PARAM_NOPROXY);
		
		formParserProperties = loadFormParserConfig(FORM_PARSER_CONFIG_FILE);
		
		this.downloadsDir = new File(loadProperty(properties, "DownloadsDir", "downloads/"));
		
	}

	@Override
	protected Properties getDefaultProperties()
	{
		Properties defProperties = super.getDefaultProperties();
		defProperties.setProperty(PARAM_BROWSER, DEF_BROWSER.getLabel());
		defProperties.setProperty(PARAM_IEDRIVERPATH, DEF_IEDRIVER_PATH);
		defProperties.setProperty(PARAM_HTTPPROXY, DEF_PROXY);
		defProperties.setProperty(PARAM_SSLPROXY, DEF_PROXY);
		defProperties.setProperty(PARAM_FTPPROXY, DEF_PROXY);
		defProperties.setProperty(PARAM_SOCKSPROXY, DEF_PROXY);
		defProperties.setProperty(PARAM_NOPROXY, DEF_PROXY);
		return defProperties;
	}

	private String loadProperty(Properties properties, String name, String defaultValue)
	{
		String property = properties.getProperty(name, "");
		if (property.isEmpty())
		{
			logger.warn(format(PROPERTY_NOT_SET, name, defaultValue));
			property = defaultValue;
		}
		else 
			logger.info(name + " = " + property);
		return property;
	}
	
	private String loadProxySetting(Properties properties, String propertyName)
	{
		String setting = properties.getProperty(propertyName, "");
		if (setting.isEmpty())
			logger.warn(format("Property '%s' is not set.", propertyName));
		else 
		{
			logger.info(propertyName + " = " + setting);
			if (!isProxySettingsSet)
				isProxySettingsSet = true;
		}
		return setting;
	}
	
	private Level loadLogLevel(Properties properties, String propertyName)
	{
		Level level = null;
		if (properties.containsKey(propertyName))
		{
			String levelTxt = properties.getProperty(propertyName);
			try
			{
				level = Level.parse(levelTxt);
			}
			catch (IllegalArgumentException e)
			{
				logger.warn(format("Invalid %s '%s'. Default level %s will be used.", 
						propertyName, levelTxt, DEF_LOG_LEVEL));
			}
		}
		return (level != null) ? level : DEF_LOG_LEVEL;
	}
	
	protected Properties loadFormParserConfig(String fileName)
	{
		Properties properties = new Properties();
		FileInputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(new File(fileName));
			properties.load(inputStream);
		}
		catch (Exception e)
		{
			logger.warn(format("Unable to load config '%s' for the action '%s'.", fileName, 
					GetFormFields.class.getSimpleName()), e);
			return null;
		}
		finally
		{
			if (inputStream != null)
				try
				{
					inputStream.close();
				}
				catch (IOException e)
				{
					logger.error("Exception during close connection to file: {}.", fileName, e);
				}
		}
		return properties;
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
	
	public String getFirefoxDriverFileName()
	{
		return firefoxDriverFileName;
	}

	public boolean isProxySettingsSet()
	{
		return isProxySettingsSet;
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
	
	public String getProfilePath()
	{
		return profilePath;
	}
	
	public String getBinary()
	{
		return binary;
	}
	
	public String getDefaultLocator()
	{
		return defaultLocator;
	}

	public Properties getFormParserProperties()
	{
		return formParserProperties;
	}

	public File getDownloadsDir()
	{
		return downloadsDir;
	}

	public Level getBrowserLoggingLevel()
	{
		return browserLoggingLevel;
	}

	public Level getClientLoggingLevel()
	{
		return clientLoggingLevel;
	}

	public Level getDriverLoggingLevel()
	{
		return driverLoggingLevel;
	}

	public Level getPerformanceLoggingLevel()
	{
		return performanceLoggingLevel;
	}
	
	public boolean isDriverLoggingEnabled()
	{
		return browserLoggingLevel != Level.OFF
				|| clientLoggingLevel != Level.OFF
				|| driverLoggingLevel != Level.OFF
				|| performanceLoggingLevel != Level.OFF;
	}
}
