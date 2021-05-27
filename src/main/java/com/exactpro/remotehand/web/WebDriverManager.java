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

package com.exactpro.remotehand.web;

import com.exactpro.remotehand.DriverPoolProvider;
import com.exactpro.remotehand.IDriverManager;
import com.exactpro.remotehand.RhConfigurationException;
import com.exactpro.remotehand.web.logging.DriverLoggerThread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
