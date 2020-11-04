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

import static java.lang.String.format;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.Configuration;

/**
 * Created by alexey.karpukhin on 2/1/16.
 */
public class WebConfiguration extends Configuration {

	private static final Logger logger = LoggerFactory.getLogger(WebConfiguration.class);
	private static volatile WebConfiguration instance;

	public static final Browser DEF_BROWSER = Browser.FIREFOX;
	public static final String DEF_IEDRIVER_PATH = "IEDriverServer.exe";
	public static final String DEF_EDGEDRIVER_PATH = "MicrosoftWebDriver.exe";
	public static final String DEF_CHROMEDRIVER_PATH = "chromedriver.exe";
	public static final String DEF_FIREFOXDRIVER_PATH = "geckodriver.exe";
	public static final String DEF_PROXY = "";
	public static final String DEF_LOCATOR = "xpath";

	public static final String PARAM_BROWSER = "Browser";
	public static final String PARAM_IEDRIVERPATH = "IEDriverPath";
	public static final String PARAM_EDGEDRIVERPATH = "EdgeDriverPath";
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
	private final String ieDriverFileName, edgeDriverFileName, chromeDriverFileName, firefoxDriverFileName, 
			httpProxySetting, sslProxySetting, ftpProxySetting, socksProxySetting, noProxySetting, profilePath, binary, defaultLocator;
	
	private final Level browserLoggingLevel;
	private final Level clientLoggingLevel;
	private final Level driverLoggingLevel;
	private final Level performanceLoggingLevel;
	
	private boolean isProxySettingsSet;
	private volatile File downloadsDir;
	private boolean disableLeavePageAlert;
	private boolean createDownloadSubDir;

	private WebConfiguration(CommandLine commandLine) {
		super(commandLine);

		instance = this;

		browserToUse = Browser.valueByLabel(this.loadProperty(PARAM_BROWSER, ""));
		if (browserToUse == Browser.INVALID)
		{
			logger.warn(format("Property '%s' is not set or has invalid value. Using default value = '%s'", PARAM_BROWSER, DEF_BROWSER.getLabel()));
			browserToUse = DEF_BROWSER;
		}
		
		profilePath = this.loadProperty(PARAM_PROFILE, "temporary profile");
		
		ieDriverFileName = this.loadProperty(PARAM_IEDRIVERPATH, DEF_IEDRIVER_PATH);
		edgeDriverFileName = this.loadProperty(PARAM_EDGEDRIVERPATH, DEF_EDGEDRIVER_PATH);
		chromeDriverFileName = this.loadProperty(PARAM_CHROMEDRIVERPATH, DEF_CHROMEDRIVER_PATH);
		firefoxDriverFileName = this.loadProperty(PARAM_FIREFOXDRIVERPATH, DEF_FIREFOXDRIVER_PATH);
		binary = this.loadProperty(PARAM_BINARY, "");
		defaultLocator = this.loadProperty(PARAM_DEFAULT_LOCATOR, DEF_LOCATOR);
		
		browserLoggingLevel = loadLogLevel(BROWSER_LOGGING_LEVEL);
		clientLoggingLevel = loadLogLevel(CLIENT_LOGGING_LEVEL);
		driverLoggingLevel = loadLogLevel(DRIVER_LOGGING_LEVEL);
		performanceLoggingLevel = loadLogLevel(PERFORMANCE_LOGGING_LEVEL);

		httpProxySetting = loadProxySetting(PARAM_HTTPPROXY);
		sslProxySetting = loadProxySetting(PARAM_SSLPROXY);
		ftpProxySetting = loadProxySetting(PARAM_FTPPROXY);
		socksProxySetting = loadProxySetting(PARAM_SOCKSPROXY);
		noProxySetting = loadProxySetting(PARAM_NOPROXY);
		this.downloadsDir = new File(this.loadProperty("DownloadsDir", "downloads/"));
		disableLeavePageAlert = this.loadProperty("DisableLeavePageAlert", true, Boolean::parseBoolean);
		createDownloadSubDir = this.loadProperty("CreateDownloadSubDir", true, Boolean::parseBoolean);
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
	
	private String loadProxySetting(String propertyName)
	{
		String setting = this.loadProperty(propertyName, "");
		
		if (setting.isEmpty())
			logger.warn(format("Property '%s' is not set.", propertyName));
		else 
		{
			logger.info("{} = {}", propertyName, setting);
			if (!isProxySettingsSet)
				isProxySettingsSet = true;
		}
		return setting;
	}
	
	private Level loadLogLevel(String propertyName)
	{
		return this.loadProperty(propertyName, DEF_LOG_LEVEL, Level::parse);
	}

	public Browser getBrowserToUse()
	{
		return browserToUse;
	}

	public String getIeDriverFileName()
	{
		return ieDriverFileName;
	}
	
	public String getEdgeDriverFileName()
	{
		return edgeDriverFileName;
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

	public boolean isDisableLeavePageAlert()
	{
		return disableLeavePageAlert;
	}
	
	public boolean isCreateDownloadSubDir()
	{
		return createDownloadSubDir;
	}

	public static void init(CommandLine commandLine)
	{
		if (instance != null)
			throw new RuntimeException("Web configuration already exists");

		instance = new WebConfiguration(commandLine);
	}

	public static WebConfiguration getInstance()
	{
		return instance;
	}
}
