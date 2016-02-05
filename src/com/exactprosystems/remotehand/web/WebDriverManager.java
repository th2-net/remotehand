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
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Created by alexey.karpukhin on 2/1/16.
 */
public class WebDriverManager {

	WebDriver webDriver = null;

	public WebDriver getWebDriver () {
		if (webDriver == null) {
			webDriver = createWebDriver();
		}
		return webDriver;
	}

	private WebDriver createWebDriver()
	{
		WebConfiguration configuration = (WebConfiguration) Configuration.getInstance();
		String httpProxy = configuration.getHttpProxySetting(),
				sslProxy = configuration.getSslProxySetting(),
				ftpProxy = configuration.getFtpProxySetting(),
				socksProxy = configuration.getSocksProxySetting(),
				noProxy = configuration.getNoProxySetting();
		DesiredCapabilities dc;
		if ((!httpProxy.isEmpty()) || (!sslProxy.isEmpty()) || (!ftpProxy.isEmpty()) || (!socksProxy.isEmpty()) || (!noProxy.isEmpty()))
		{
			Proxy proxy = new Proxy();
			proxy.setHttpProxy(httpProxy);
			proxy.setSslProxy(sslProxy);
			proxy.setFtpProxy(ftpProxy);
			proxy.setSocksProxy(socksProxy);
			proxy.setNoProxy(noProxy);
			dc = new DesiredCapabilities();
			dc.setCapability(CapabilityType.PROXY, proxy);
		}
		else
			dc = null;

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
			default :
				if (dc!=null)
					driver = new FirefoxDriver(dc);
				else
					driver = new FirefoxDriver();
				break;
		}
		return driver;
	}

	public void close()
	{
		if (webDriver != null) {
			webDriver.quit();
			webDriver = null;
		}
	}

}
