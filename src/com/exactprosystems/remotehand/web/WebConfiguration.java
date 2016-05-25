////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web;

import com.exactprosystems.remotehand.Configuration;
import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;

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
	
	public static final String PARAM_BROWSER = "Browser";
	public static final String	PARAM_IEDRIVERPATH = "IEDriverPath";
	public static final String	PARAM_CHROMEDRIVERPATH = "ChromeDriverPath";
	public static final String PARAM_HTTPPROXY = "HttpProxy";
	public static final String PARAM_SSLPROXY = "SslProxy";
	public static final String PARAM_FTPPROXY = "FtpProxy";
	public static final String PARAM_SOCKSPROXY = "SocksProxy";
	public static final String PARAM_NOPROXY = "NoProxy";
	public static final String PARAM_PROFILE = "Profile";

	private volatile Browser browserToUse;
	private volatile String ieDriverFileName, chromeDriverFileName, httpProxySetting, sslProxySetting, 
		ftpProxySetting, socksProxySetting, noProxySetting, profilePath;

	protected WebConfiguration(CommandLine commandLine) {
		super(commandLine);

		browserToUse = Browser.valueByLabel(properties.getProperty(PARAM_BROWSER));
		if (browserToUse == Browser.INVALID)
		{
			logger.warn(String.format("Property '%s' is not set or has invalid value. Using default value = '%s'", PARAM_BROWSER, DEF_BROWSER.getLabel()));
			browserToUse = DEF_BROWSER;
		}
		
		profilePath = properties.getProperty(PARAM_PROFILE);
		if ((profilePath == null) || (profilePath.isEmpty()))
			logger.warn(String.format(PROPERTY_NOT_SET, PARAM_PROFILE, "temporary profile"));
		else
			logger.info(PARAM_PROFILE+"="+profilePath);
		
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
	
	public String getProfilePath()
	{
		return profilePath;
	}
	
}
