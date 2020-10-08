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

import com.exactprosystems.remotehand.RhConfigurationException;
import com.exactprosystems.remotehand.sessions.SessionContext;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;

public class WindowsGridDriverPoolProvider extends BaseGridDriverPoolProvider<WindowsDriverWrapper>
{
	public WindowsGridDriverPoolProvider(Map<String, String> sessionTargetUrls)
	{
		super(sessionTargetUrls);
	}


	@Override
	public WindowsDriverWrapper getDriverWrapper(SessionContext context) throws RhConfigurationException
	{
		WindowsDriverWrapper driverStorage = driversPool.get(context.getSessionId());
		if (driverStorage == null)
		{
			driverStorage = createDriver(context);
			driversPool.put(context.getSessionId(), driverStorage);
		}

		return driverStorage;
	}

	@Override
	public void clearDriverPool()
	{
		driversPool.forEach((session, driverWrapper) -> {
			sessionTargetUrls.remove(session);
			driverWrapper.close();
		});
		driversPool.clear();
	}

	@Override
	protected WindowsDriverWrapper createDriver(SessionContext context) throws RhConfigurationException
	{
		String driverUrl = sessionTargetUrls.get(context.getSessionId());
		try
		{
			return new WindowsDriverWrapper(new URL(driverUrl));
		}
		catch (MalformedURLException e)
		{
			throw new RhConfigurationException("Cannot create URL", e);
		}
	}
	
}
