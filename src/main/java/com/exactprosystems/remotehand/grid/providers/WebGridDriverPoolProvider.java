/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.grid.providers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.RhConfigurationException;
import com.exactprosystems.remotehand.sessions.SessionContext;
import com.exactprosystems.remotehand.web.WebDriverWrapper;

public class WebGridDriverPoolProvider extends BaseGridDriverPoolProvider<WebDriverWrapper>
{
	private static final Logger logger = LoggerFactory.getLogger(WebGridDriverPoolProvider.class);


	public WebGridDriverPoolProvider(Map<String, String> sessionTargetUrls)
	{
		super(sessionTargetUrls);
	}


	@Override
	public WebDriverWrapper getDriverWrapper(SessionContext context) throws RhConfigurationException
	{
		return createDriver(context);
	}

	@Override
	public void closeDriver(String sessionId, WebDriver driver)
	{
		super.closeDriver(sessionId, driver);
		closeDriver(driver);
	}

	@Override
	public void clearDriverPool()
	{
		driversPool.forEach((session, driverWrapper) -> {
			sessionTargetUrls.remove(session);
			closeDriver(driverWrapper.getDriver());
		});
		driversPool.clear();
	}


	@Override
	protected WebDriverWrapper createDriver(SessionContext context) throws RhConfigurationException
	{
		String driverUrl = sessionTargetUrls.get(context.getSessionId());
		ChromeOptions chromeOptions = buildChromeOptions();
		try
		{
			RemoteWebDriver driver = new RemoteWebDriver(new URL(driverUrl + "/wd/hub/"), chromeOptions);
			return new WebDriverWrapper(driver, null);
		}
		catch (MalformedURLException e)
		{
			throw new RhConfigurationException("Unable to create Chrome driver: ", e);
		}
	}


	private ChromeOptions buildChromeOptions()
	{
		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.addArguments("--no-sandbox");
		
		return chromeOptions;
	}

	private void closeDriver(WebDriver driver)
	{
		try
		{
			driver.quit();
		}
		catch (Exception e)
		{
			logger.error("Error while closing driver.", e);
		}
	}
}
