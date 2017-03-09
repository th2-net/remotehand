////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2017, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web;

import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.web.actions.GetFormFields;
import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by alexey.karpukhin on 2/1/16.
 */
public class WebConfiguration extends Configuration{

	private static final Logger logger = Logger.getLogger(WebConfiguration.class);

	public static final Browser DEF_BROWSER = Browser.FIREFOX;
	public static final String DEF_IEDRIVER_PATH = "IEDriverServer.exe";
	public static final String DEF_CHROMEDRIVER_PATH = "chromedriver.exe";
	public static final String DEF_PROXY = "";
	public static final String FORM_PARSER_CONFIG_FILE = "formParser.properties";
	
	public static final String PARAM_BROWSER = "Browser";
	public static final String	PARAM_IEDRIVERPATH = "IEDriverPath";
	public static final String	PARAM_CHROMEDRIVERPATH = "ChromeDriverPath";
	public static final String PARAM_HTTPPROXY = "HttpProxy";
	public static final String PARAM_SSLPROXY = "SslProxy";
	public static final String PARAM_FTPPROXY = "FtpProxy";
	public static final String PARAM_SOCKSPROXY = "SocksProxy";
	public static final String PARAM_NOPROXY = "NoProxy";
	public static final String PARAM_PROFILE = "Profile";
	public static final String BINARY_PARAM = "Binary";
	
	public static final String SCREENSHOTS_DIR_NAME = "screenshots";

	private volatile Browser browserToUse;
	private final String ieDriverFileName, chromeDriverFileName, httpProxySetting, sslProxySetting, 
		ftpProxySetting, socksProxySetting, noProxySetting, profilePath, binary;
	
	private final Properties formParserProperties;
	private boolean isProxySettingsSet;

	protected WebConfiguration(CommandLine commandLine) {
		super(commandLine);

		browserToUse = Browser.valueByLabel(properties.getProperty(PARAM_BROWSER));
		if (browserToUse == Browser.INVALID)
		{
			logger.warn(String.format("Property '%s' is not set or has invalid value. Using default value = '%s'", PARAM_BROWSER, DEF_BROWSER.getLabel()));
			browserToUse = DEF_BROWSER;
		}
		
		profilePath = loadProperty(properties, PARAM_PROFILE, "temporary profile");
		
		ieDriverFileName = loadProperty(properties, PARAM_IEDRIVERPATH, DEF_IEDRIVER_PATH);
		chromeDriverFileName = loadProperty(properties, PARAM_CHROMEDRIVERPATH, DEF_CHROMEDRIVER_PATH);
		binary = loadProperty(properties, BINARY_PARAM, "");

		httpProxySetting = loadProxySetting(properties, PARAM_HTTPPROXY);
		sslProxySetting = loadProxySetting(properties, PARAM_SSLPROXY);
		ftpProxySetting = loadProxySetting(properties, PARAM_FTPPROXY);
		socksProxySetting = loadProxySetting(properties, PARAM_SOCKSPROXY);
		noProxySetting = loadProxySetting(properties, PARAM_NOPROXY);
		
		formParserProperties = loadFormParserConfig(FORM_PARSER_CONFIG_FILE);
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

	public String getBinary() {
		return binary;
	}

	private String loadProperty(Properties properties, String name, String defaultValue)
	{
		String property = properties.getProperty(name, "");
		if (property.isEmpty())
		{
			logger.warn(String.format(PROPERTY_NOT_SET, name, defaultValue));
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
			logger.warn(String.format("Property '%s' is not set.", propertyName));
		else 
		{
			logger.info(propertyName + " = " + setting);
			if (!isProxySettingsSet)
				isProxySettingsSet = true;
		}
		return setting;
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
			logger.warn(String.format("Unable to load config '%s' for the action '%s'.", fileName, 
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
					logger.error(e);
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

	public Properties getFormParserProperties()
	{
		return formParserProperties;
	}
}
