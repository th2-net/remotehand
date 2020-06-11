/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web;

import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.RhConfigurationException;
import com.exactprosystems.remotehand.web.logging.DriverLoggerThread;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Thread.currentThread;

/**
 * Created by alexey.karpukhin on 2/1/16.
 */
public class WebDriverManager 
{
	private static final Logger log = LoggerFactory.getLogger(WebDriverManager.class);
	
	private final Map<String, DriverLoggerThread> loggers = new ConcurrentHashMap<>();
	private final Queue<DriverStorage> webDriverPool = new ConcurrentLinkedQueue<>();

	public boolean isDriverAlive(WebDriver driver)
	{
		if (driver == null)
			return false;

		try {
			return driver.getWindowHandles().size() > 0;
		} catch (WebDriverException e) {
			log.warn("Error received as result of checking browser state", e);
			return false;
		}
	}

	public void initDriverPool()
	{
		WebConfiguration config = (WebConfiguration) Configuration.getInstance();
		int i = config.getDriverPoolSize();
		try
		{
			if (i > 0)
				for (int j = 0; j < i; j++)
					webDriverPool.add(createDriverStorage(config));
		}
		catch (RhConfigurationException e)
		{
			log.warn("Unable init driver pool", e);
		}
	}

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

	public WebDriver getWebDriver(WebSessionContext context) throws RhConfigurationException
	{
		WebConfiguration configuration = (WebConfiguration) Configuration.getInstance();

		WebDriver driver;
		DriverStorage driverStorage = webDriverPool.poll();
		if (driverStorage == null || !isDriverAlive(driverStorage.getDriver()))
		{
			driverStorage = createDriverStorage(configuration);
		}
		driver = driverStorage.getDriver();
		context.setDownloadDir(driverStorage.getDownloadDir());

		if (configuration.isDriverLoggingEnabled())
			initLogger(driver, context.getSessionId());

		return driver;
	}

	private DriverStorage createDriverStorage(WebConfiguration configuration) throws RhConfigurationException
	{
		DesiredCapabilities dc = createDesiredCapabilities(configuration);
		WebDriver driver;
		File downloadDir = WebUtils.createDownloadDirectory();
		try
		{
			switch (configuration.getBrowserToUse())
			{
				case IE:
					driver = createIeDriver(configuration, dc);
					break;
				case EDGE:
					driver = createEdgeDriver(configuration, dc);
					break;
				case CHROME:
					driver = createChromeDriver(configuration, dc, downloadDir, false);
					break;
				case CHROME_HEADLESS:
					driver = createChromeDriver(configuration, dc, downloadDir,true);
					break;
				case FIREFOX_HEADLESS:
					driver = createFirefoxDriver(configuration, dc, downloadDir, true);
					break;
				default:
					driver = createFirefoxDriver(configuration, dc, downloadDir, false);
			}
		}
		catch (RhConfigurationException e)
		{
			WebUtils.deleteDownloadDirectory(downloadDir);
			throw e;
		}

		return new DriverStorage(driver, downloadDir);
	}

	// Notes about driver initialization:
	// 1. For some driver's constructors we will get an error if we pass null as DesiredCapabilities.
	// 2. Constructor can throw RuntimeException in case of driver file absence.
	
	private InternetExplorerDriver createIeDriver(WebConfiguration cfg, DesiredCapabilities dc) throws RhConfigurationException
	{
		try
		{
			System.setProperty("webdriver.ie.driver", cfg.getIeDriverFileName());
			return dc != null ? new InternetExplorerDriver(new InternetExplorerOptions(dc)) : new InternetExplorerDriver();
		}
		catch (Exception e)
		{
			throw new RhConfigurationException("Unable to create Internet Explorer driver: " + e.getMessage(), e);
		}
	}
	
	private EdgeDriver createEdgeDriver(WebConfiguration cfg, DesiredCapabilities dc) throws RhConfigurationException
	{
		try
		{
			System.setProperty("webdriver.edge.driver", cfg.getEdgeDriverFileName());
			EdgeOptions options = new EdgeOptions();
			if (dc != null)
			{
				for (Entry<String, Object> capability : dc.asMap().entrySet())
					options.setCapability(capability.getKey(), capability.getValue());
			}
			return new EdgeDriver(options);
		}
		catch (Exception e)
		{
			throw new RhConfigurationException("Unable to create Microsoft Edge driver: " + e.getMessage(), e);
		}
	}
	
	private ChromeDriver createChromeDriver(WebConfiguration cfg, DesiredCapabilities dc, File downloadDir, boolean headlessMode) throws RhConfigurationException
	{
		try
		{			
			System.setProperty("webdriver.chrome.driver", cfg.getChromeDriverFileName());
			ChromeOptions options = new ChromeOptions();
			if (headlessMode)
			{
				options.setHeadless(true);
				options.addArguments("window-size=1920x1080");
			}
			options.addArguments("--no-sandbox");
			String binaryParam = cfg.getBinary();
			if (binaryParam != null && !binaryParam.isEmpty())
			{
				File binaryFile = new File(binaryParam);
				if (binaryFile.exists())
					options.setBinary(binaryParam);
			}

			Map<String, String> prefs = new HashMap<>(2);
			prefs.put("profile.default_content_settings.popups", "0");
			prefs.put("download.default_directory", downloadDir.getAbsolutePath());
			options.setExperimentalOption("prefs", prefs);

			options.setExperimentalOption("prefs", prefs);

			if (dc != null)
			{
				for (Entry<String, Object> capability : dc.asMap().entrySet())
					options.setCapability(capability.getKey(), capability.getValue());
			}
			return new ChromeDriver(options);
		}
		catch (Exception e)
		{
			throw new RhConfigurationException("Unable to create Chrome driver: " + e.getMessage(), e);
		}
	}

	private FirefoxDriver createFirefoxDriver(WebConfiguration cfg, DesiredCapabilities dc, File downloadDir, boolean headlessMode) throws RhConfigurationException
	{
		try
		{
			System.setProperty("webdriver.gecko.driver", cfg.getFirefoxDriverFileName());
			FirefoxOptions options = new FirefoxOptions();
			if (headlessMode)
			{
				options.setHeadless(true);
				options.addArguments("--width=1920");
				options.addArguments("--height=1080");
			}

			//Use for the default download directory the last folder specified for a download
			options.addPreference("browser.download.folderList", 2);
			//Set the last directory used for saving a file from the "What should (browser) do with this file?" dialog.
			options.addPreference("browser.download.dir", downloadDir.getAbsolutePath());
			//This is true by default.
			options.addPreference("browser.download.useDownloadDir", true);
			
			if (dc != null)
			{
				for (Entry<String, Object> capability : dc.asMap().entrySet())
					options.setCapability(capability.getKey(), capability.getValue());
			}
			return new FirefoxDriver(options);
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
		closeDriver(driver);
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

	public void clearDriverPool()
	{
		DriverStorage driverStorage;
		while ((driverStorage = webDriverPool.poll()) != null)
		{
			WebUtils.deleteDownloadDirectory(driverStorage.getDownloadDir());
			closeDriver(driverStorage.getDriver());
		}
	}

	private void closeDriver(WebDriver driver)
	{
		try
		{
			driver.quit();
		}
		catch (Exception e)
		{
			log.error("Error while closing driver.", e);
		}
	}

	public static class DriverStorage
	{
		private final WebDriver driver;
		private final File downloadDir;

		public DriverStorage(WebDriver driver, File downloadDir)
		{
			this.driver = driver;
			this.downloadDir = downloadDir;
		}

		public WebDriver getDriver()
		{
			return driver;
		}

		public File getDownloadDir()
		{
			return downloadDir;
		}
	}
}
