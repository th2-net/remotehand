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
import com.exactprosystems.remotehand.RhConfigurationException;
import com.exactprosystems.remotehand.web.logging.DriverLoggerThread;
import org.apache.log4j.Logger;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Thread.currentThread;

/**
 * Created by alexey.karpukhin on 2/1/16.
 */
public class WebDriverManager 
{
	private static final Logger log = Logger.getLogger(WebDriverManager.class);
	
	private final Map<String, DriverLoggerThread> loggers = new ConcurrentHashMap<>();
	

	private DesiredCapabilities createDesiredCapabilities(WebConfiguration cfg)
	{
		DesiredCapabilities dc = new DesiredCapabilities();
		
		if (cfg.isProxySettingsSet())
			dc.setCapability(CapabilityType.PROXY, createProxySettings(cfg));
		
		if (cfg.isDriverLoggingEnabled())
			dc.setCapability(CapabilityType.LOGGING_PREFS, createLoggingPreferences(cfg));
		
		return dc;
	}

	private Proxy createProxySettings(WebConfiguration cfg)
	{
		Proxy proxy = new Proxy();
		proxy.setHttpProxy(cfg.getHttpProxySetting());
		proxy.setSslProxy(cfg.getSslProxySetting());
		proxy.setFtpProxy(cfg.getFtpProxySetting());
		proxy.setSocksProxy(cfg.getSocksProxySetting());
		proxy.setNoProxy(cfg.getNoProxySetting());
		return proxy;
	}

	private LoggingPreferences createLoggingPreferences(WebConfiguration cfg)
	{
		LoggingPreferences preferences = new LoggingPreferences();
		preferences.enable(LogType.BROWSER, cfg.getBrowserLoggingLevel());
		preferences.enable(LogType.DRIVER, cfg.getDriverLoggingLevel());
		preferences.enable(LogType.PERFORMANCE, cfg.getPerformanceLoggingLevel());
		preferences.enable(LogType.CLIENT, cfg.getClientLoggingLevel());
		return preferences;
	}
	
	
	public WebDriver createWebDriver(String sessionId, File downloadDir) throws RhConfigurationException
	{
		WebConfiguration configuration = (WebConfiguration) Configuration.getInstance();
		WebDriver driver = createDriver(configuration, downloadDir);
		if (configuration.isDriverLoggingEnabled())
			initLogger(driver, sessionId);
		return driver;
	}

	private WebDriver createDriver(WebConfiguration configuration, File downloadDir) throws RhConfigurationException
	{
		DesiredCapabilities dc = createDesiredCapabilities(configuration);
		switch (configuration.getBrowserToUse())
		{
			case IE :       return createIeDriver(configuration, dc);
			case CHROME :   return createChromeDriver(configuration, dc, downloadDir);
			case HEADLESS:  return createPhantomJsDriver(dc);
			default :       return createFireFoxDriver(configuration, dc);
		}
	}

	// Notes about driver initialization:
	// 1. For some driver's constructors we will get an error if we pass null as DesiredCapabilities.
	// 2. Constructor can throw RuntimeException in case of driver file absence.
	
	private InternetExplorerDriver createIeDriver(WebConfiguration cfg, DesiredCapabilities dc) throws RhConfigurationException
	{
		try
		{
			System.setProperty("webdriver.ie.driver", cfg.getIeDriverFileName());
			return (dc != null) ? new InternetExplorerDriver(dc) : new InternetExplorerDriver();
		}
		catch (Exception e)
		{
			throw new RhConfigurationException("Unable to create Internet Explorer driver: " + e.getMessage(), e);
		}
	}
	
	private ChromeDriver createChromeDriver(WebConfiguration cfg, DesiredCapabilities dc, File downloadDir) throws RhConfigurationException
	{
		try
		{			
			System.setProperty("webdriver.chrome.driver", cfg.getChromeDriverFileName());
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--no-sandbox");
			Map<String, String> prefs = new HashMap<>(2);
			prefs.put("profile.default_content_settings.popups", "0");
			prefs.put("download.default_directory", downloadDir.getAbsolutePath());
			options.setExperimentalOption("prefs", prefs);

			String binaryParam = cfg.getBinary();
			if (binaryParam != null && !binaryParam.isEmpty())
			{
				File binaryFile = new File(binaryParam);
				if (binaryFile.exists())
					options.setBinary(binaryParam);
			}

			if (dc != null)
				dc.setCapability(ChromeOptions.CAPABILITY, options);
			return (dc != null) ? new ChromeDriver(dc) : new ChromeDriver(options);
		}
		catch (Exception e)
		{
			throw new RhConfigurationException("Unable to create Chrome driver: " + e.getMessage(), e);
		}
	}
	
	private PhantomJSDriver createPhantomJsDriver(DesiredCapabilities dc) throws RhConfigurationException
	{
		try
		{
			return (dc != null) ? new PhantomJSDriver(dc) : new PhantomJSDriver();
		}
		catch (Exception e)
		{
			throw new RhConfigurationException("Unable to create PhantomJS (Headless) driver: " + e.getMessage(), e);
		}
	}
	
	private FirefoxDriver createFireFoxDriver(WebConfiguration cfg, DesiredCapabilities dc) throws RhConfigurationException
	{
		try
		{
			String profile = cfg.getProfilePath();
			if (profile != null && !profile.isEmpty())
			{
				File profileDir = new File(profile);
				if (profileDir.exists())
				{
					FirefoxProfile fxProfile = new FirefoxProfile(profileDir);
					if (dc == null)
						dc = new DesiredCapabilities();
					dc.setCapability(FirefoxDriver.PROFILE, fxProfile);
				}
			}

			String ffBinaryParam = cfg.getBinary();
			if (ffBinaryParam != null && !ffBinaryParam.isEmpty()) {
				File ffBinaryFile = new File(ffBinaryParam);
				if (ffBinaryFile.exists())
				{
					FirefoxBinary ffBinary = new FirefoxBinary(ffBinaryFile);
					if (dc == null)
						dc = new DesiredCapabilities();
					dc.setCapability(FirefoxDriver.BINARY, ffBinary);
				}
			}

			return (dc != null) ? new FirefoxDriver(dc) : new FirefoxDriver();
		}
		catch (Exception e)
		{
			throw new RhConfigurationException("Unable to create FireFox driver: " + e.getMessage(), e);
		}
	}
	
	
	private void initLogger(WebDriver driver, String sessionId)
	{
		DriverLoggerThread logger = new DriverLoggerThread(sessionId, driver);
		logger.start();
		loggers.put(sessionId, logger);
	}
	
	
	public void closeWebDriver(WebDriver driver, String sessionId)
	{
		stopLogger(sessionId);	
		try
		{
			driver.quit();
		}
		catch (Exception e)
		{
			log.error("Error while closing driver.", e);
		}
	}

	private void stopLogger(String sessionId)
	{
		DriverLoggerThread logger = loggers.remove(sessionId);
		if (logger != null)
		{
			logger.interrupt();
			try
			{
				logger.join();
			}
			catch (InterruptedException e)
			{
				currentThread().interrupt();
			}
		}
	}
}
