/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.windows;

import java.net.MalformedURLException;
import java.net.URL;

import com.exactprosystems.remotehand.DriverPoolProvider;
import com.exactprosystems.remotehand.RhConfigurationException;
import com.exactprosystems.remotehand.sessions.SessionContext;

public class WindowsDriverPoolProvider implements DriverPoolProvider<WindowsDriverWrapper>
{
	private static final String URL_DELIMITER = "/";

	@Override
	public WindowsDriverWrapper createDriverWrapper(SessionContext context) throws RhConfigurationException
	{
		try
		{
			return new WindowsDriverWrapper(new URL(getConnectionUrl()));
		}
		catch (MalformedURLException e)
		{
			throw new RhConfigurationException("Cannot create URL", e);
		}
	}

	@Override
	public void clearDriverPool()
	{
	}

	@Override
	public void closeDriver(String sessionId, WindowsDriverWrapper driver)
	{
	}

	private String getConnectionUrl()
	{
		WindowsConfiguration instance = WindowsConfiguration.getInstance();
		String urlPath = instance.getWinAppUrlPath();
		if (!urlPath.startsWith(URL_DELIMITER))
			urlPath = URL_DELIMITER + urlPath;
		if (!urlPath.endsWith(URL_DELIMITER))
			urlPath = urlPath + URL_DELIMITER;
		
		return String.format("http://%s:%s%s", instance.getWinAppHost(), instance.getWinAppPort(), urlPath);
	}
}
