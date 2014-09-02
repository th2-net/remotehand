package com.exactprosystems.remotehand;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.exactprosystems.remotehand.webelements.WebLocator;

public class ActionsLauncher
{
	WebDriver webDriver = getWebDriver();

	ScriptProcessorThread parent = null;

	ActionsLauncher(ScriptProcessorThread parentThread)
	{
		this.parent = parentThread;
	}

	public String runActions(List<ScriptAction> scriptActions) throws ScriptExecuteException
	{
		StringBuilder result = null;

		for (ScriptAction scriptAction : scriptActions)
		{
			final WebAction action = scriptAction.getWebAction();
			final WebLocator locator = scriptAction.getWebLocator();
			final Map<String, String> params = scriptAction.getParams();

			By webLocator = null;
			if (locator != null)
			{
				webLocator = locator.getWebLocator(webDriver, params);
			}

			final String actionResult = action.run(webDriver, webLocator, params);
			if (actionResult != null)
			{
				if (result==null)
					result = new StringBuilder();
				result.append(actionResult+"\r\n");
			}

			if (parent != null && parent.isClosing())
			{
				return null;
			}
		}

		return result!=null ? result.toString() : null;
	}

	public void close()
	{
		if (webDriver != null)
			webDriver.quit();
	}

	private static WebDriver getWebDriver()
	{
		switch (Configuration.getInstance().getBrowserToUse())
		{
		case IE :
		{
			System.setProperty("webdriver.ie.driver", Configuration.getInstance().getIeDriverFileName());
			return new InternetExplorerDriver();
		}
		case CHROME :
		{
			System.setProperty("webdriver.chrome.driver", Configuration.getInstance().getChromeDriverFileName());
			return new ChromeDriver();
		}
		default :
			Configuration c = Configuration.getInstance();
			String httpProxy = c.getHttpProxySetting(),
			sslProxy = c.getSslProxySetting(),
			ftpProxy = c.getFtpProxySetting(),
			socksProxy = c.getSocksProxySetting(),
			noProxy = c.getNoProxySetting();
			if ((httpProxy.isEmpty()) && (sslProxy.isEmpty()) && (ftpProxy.isEmpty()) && (socksProxy.isEmpty()) && (noProxy.isEmpty()))
				return new FirefoxDriver();

			Proxy proxy = new Proxy();
			proxy.setHttpProxy(httpProxy);
			proxy.setSslProxy(sslProxy);
			proxy.setFtpProxy(ftpProxy);
			proxy.setSocksProxy(socksProxy);
			proxy.setNoProxy(noProxy);
			DesiredCapabilities dc = new DesiredCapabilities();
			dc.setCapability(CapabilityType.PROXY, proxy);
			return new FirefoxDriver(dc);
		}
	}
}
