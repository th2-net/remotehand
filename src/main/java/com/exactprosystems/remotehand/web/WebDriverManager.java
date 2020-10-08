/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.*;
import com.exactprosystems.remotehand.web.logging.DriverLoggerThread;

import static java.lang.Thread.currentThread;

/**
 * Created by alexey.karpukhin on 2/1/16.
 */
public class WebDriverManager implements IDriverManager
{
	private static final Logger log = LoggerFactory.getLogger(WebDriverManager.class);
	
	private final Map<String, DriverLoggerThread> loggers = new ConcurrentHashMap<>();
	private final DriverPoolProvider<? extends DriverWrapper<WebDriver>> driverPoolProvider;


	public WebDriverManager(DriverPoolProvider<? extends DriverWrapper<WebDriver>> driverPoolProvider)
	{
		this.driverPoolProvider = driverPoolProvider;
	}

	@Override
	public void initDriverPool()
	{
		if (driverPoolProvider instanceof WebDriverPoolProvider)
			((WebDriverPoolProvider) driverPoolProvider).initDriverPool();
	}

	public WebDriver getWebDriver(WebSessionContext context) throws RhConfigurationException
	{
		WebDriverWrapper webDriverWrapper = (WebDriverWrapper)driverPoolProvider.getDriverWrapper(context);
		WebDriver driver = webDriverWrapper.getDriver();
		context.setDownloadDir(webDriverWrapper.getDownloadDir());

		if (WebConfiguration.getInstance().isDriverLoggingEnabled())
			initLogger(driver, context.getSessionId());

		return driver;
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
		driverPoolProvider.closeDriver(sessionId, driver);
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

	@Override
	public void clearDriverPool()
	{
		driverPoolProvider.clearDriverPool();
	}
}
