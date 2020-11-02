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

import com.exactprosystems.remotehand.*;
import com.exactprosystems.remotehand.web.logging.DriverLoggerThread;

import static java.lang.Thread.currentThread;

/**
 * Created by alexey.karpukhin on 2/1/16.
 */
public class WebDriverManager implements IDriverManager
{
	private final Map<String, DriverLoggerThread> loggers = new ConcurrentHashMap<>();
	private final DriverPoolProvider<WebDriverWrapper> driverPoolProvider;


	public WebDriverManager(DriverPoolProvider<WebDriverWrapper> driverPoolProvider)
	{
		this.driverPoolProvider = driverPoolProvider;
	}

	@Override
	public void initDriverPool()
	{
		if (driverPoolProvider instanceof WebDriverPoolProvider)
			((WebDriverPoolProvider) driverPoolProvider).initDriverPool();
	}

	public WebDriverWrapper createWebDriver(WebSessionContext context) throws RhConfigurationException
	{
		WebDriverWrapper driver = (WebDriverWrapper)driverPoolProvider.createDriverWrapper(context);
		context.setWebDriverManager(this);
		context.setWebDriverWrapper(driver);

		if (WebConfiguration.getInstance().isDriverLoggingEnabled())
			initLogger(driver, context.getSessionId());

		return driver;
	}

	private void initLogger(WebDriverWrapper driver, String sessionId)
	{
		DriverLoggerThread logger = new DriverLoggerThread(sessionId, driver.getDriver());
		logger.start();
		loggers.put(sessionId, logger);
	}
	
	
	public void closeWebDriver(WebDriverWrapper driver, String sessionId)
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
