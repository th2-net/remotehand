////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2017, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web;

import java.io.File;
import java.util.Collections;

import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.RhConfigurationException;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Created by alexey.karpukhin on 2/1/16.
 */
public class WebDriverManager {

	private DesiredCapabilities createDesiredCapabilities(WebConfiguration cfg)
	{
		if (cfg.isProxySettingsSet())
		{
			Proxy proxy = new Proxy();
			proxy.setHttpProxy(cfg.getHttpProxySetting());
			proxy.setSslProxy(cfg.getSslProxySetting());
			proxy.setFtpProxy(cfg.getFtpProxySetting());
			proxy.setSocksProxy(cfg.getSocksProxySetting());
			proxy.setNoProxy(cfg.getNoProxySetting());
			return new DesiredCapabilities(Collections.singletonMap(CapabilityType.PROXY, proxy));
		}
		else 
			return null;
	}
	
	public WebDriver createWebDriver() throws RhConfigurationException
	{
		WebConfiguration configuration = (WebConfiguration) Configuration.getInstance();
		DesiredCapabilities dc = createDesiredCapabilities(configuration);
		switch (configuration.getBrowserToUse())
		{
			case IE :       return createIeDriver(configuration, dc);
			case CHROME :   return createChromeDriver(configuration, dc);
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
	
	private ChromeDriver createChromeDriver(WebConfiguration cfg, DesiredCapabilities dc) throws RhConfigurationException
	{
		try
		{
			System.setProperty("webdriver.chrome.driver", cfg.getChromeDriverFileName());
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--no-sandbox");
			
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
}
