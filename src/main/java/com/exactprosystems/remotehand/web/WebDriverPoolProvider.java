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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
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

import com.exactprosystems.remotehand.DriverPoolProvider;
import com.exactprosystems.remotehand.RhConfigurationException;
import com.exactprosystems.remotehand.sessions.SessionContext;

public class WebDriverPoolProvider implements DriverPoolProvider<WebDriverWrapper>
{
	private static final Logger logger = LoggerFactory.getLogger(WebDriverPoolProvider.class);
	private final Queue<WebDriverWrapper> webDriverPool = new ConcurrentLinkedQueue<>();


	public void initDriverPool()
	{
		WebConfiguration config = WebConfiguration.getInstance();
		int i = config.getDriverPoolSize();
		try
		{
			if (i > 0)
				for (int j = 0; j < i; j++)
					webDriverPool.add(createDriverStorage(config));
		}
		catch (RhConfigurationException e)
		{
			logger.warn("Could not initialize driver pool", e);
		}
	}

	@Override
	public WebDriverWrapper createDriverWrapper(SessionContext context) throws RhConfigurationException
	{
		WebConfiguration config = WebConfiguration.getInstance();
		WebDriverWrapper webDriverWrapper = webDriverPool.poll();
		if (webDriverWrapper == null || !isDriverAlive(webDriverWrapper.getDriver()))
			webDriverWrapper = createDriverStorage(config);

		return webDriverWrapper;
	}

	private WebDriverWrapper createDriverStorage(WebConfiguration configuration) throws RhConfigurationException
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
		
		return new WebDriverWrapper(driver, downloadDir);
	}

	@Override
	public void clearDriverPool()
	{
		WebDriverWrapper webDriverWrapper;
		while ((webDriverWrapper = webDriverPool.poll()) != null)
		{
			WebUtils.deleteDownloadDirectory(webDriverWrapper.getDownloadDir());
			closeDriver(null, webDriverWrapper);
		}
	}

	public boolean isDriverAlive(WebDriver driver)
	{
		if (driver == null)
			return false;

		try
		{
			return driver.getWindowHandles().size() > 0;
		}
		catch (WebDriverException e)
		{
			logger.warn("Error received as result of checking browser state", e);
			return false;
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
			return dc != null ? new InternetExplorerDriver(new InternetExplorerOptions(dc)) : new InternetExplorerDriver();
		}
		catch (Exception e)
		{
			throw new RhConfigurationException("Could not create Internet Explorer driver", e);
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
				for (Map.Entry<String, Object> capability : dc.asMap().entrySet())
					options.setCapability(capability.getKey(), capability.getValue());
			}
			return new EdgeDriver(options);
		}
		catch (Exception e)
		{
			throw new RhConfigurationException("Could not create Microsoft Edge driver", e);
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
			options.addArguments("--ignore-ssl-errors=yes");
			options.addArguments("--ignore-certificate-errors");
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

			options.setExperimentalOption("w3c", cfg.isEnableW3C());
			
			if (dc != null)
			{
				for (Map.Entry<String, Object> capability : dc.asMap().entrySet())
					options.setCapability(capability.getKey(), capability.getValue());
			}
			return new ChromeDriver(ChromeDriverService.createDefaultService(), options);
		}
		catch (Exception e)
		{
			throw new RhConfigurationException("Could not create Chrome driver", e);
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
				for (Map.Entry<String, Object> capability : dc.asMap().entrySet())
					options.setCapability(capability.getKey(), capability.getValue());
			}
			return new FirefoxDriver(options);
		}
		catch (Exception e)
		{
			throw new RhConfigurationException("Could not create Firefox driver", e);
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

	@Override
	public void closeDriver(String sessionId, WebDriverWrapper driver)
	{
		try
		{
			driver.getDriver().quit();
		}
		catch (Exception e)
		{
			logger.error("Error while closing driver", e);
		}
	}
}
