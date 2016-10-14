////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
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

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
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

	private DesiredCapabilities createDesiredCapabilities()
	{
		WebConfiguration cfg = (WebConfiguration) Configuration.getInstance();
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
	
	public WebDriver createWebDriver()
	{
		WebConfiguration configuration = (WebConfiguration) Configuration.getInstance();
		DesiredCapabilities dc = createDesiredCapabilities();
		WebDriver driver;
		switch (configuration.getBrowserToUse())
		{
			case IE :
			{
				System.setProperty("webdriver.ie.driver", configuration.getIeDriverFileName());
				if (dc!=null)
					driver = new InternetExplorerDriver(dc);
				else
					driver = new InternetExplorerDriver();
				break;
			}
			case CHROME :
			{
				System.setProperty("webdriver.chrome.driver", configuration.getChromeDriverFileName());
				if (dc!=null)
					driver = new ChromeDriver(dc);
				else
					driver = new ChromeDriver();
				break;
			}
			case HEADLESS:
			{
				if (dc != null)
					driver = new PhantomJSDriver(dc);
				else 
					driver = new PhantomJSDriver();
				break;
			}
			default :
				String profile = configuration.getProfilePath();
				if (profile !=null && !profile.isEmpty())
				{
					File profileDir = new File(profile);
					if (profileDir.exists())
					{
						FirefoxProfile fxProfile = new FirefoxProfile(profileDir);
						if (dc == null)
							dc = new DesiredCapabilities();
						dc.setCapability(FirefoxDriver.PROFILE,  fxProfile);
					}
				}
				if (dc!=null)
					driver = new FirefoxDriver(dc);
				else
					driver = new FirefoxDriver();
				break;
		}
		return driver;
	}
}
