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
	public WindowsDriverWrapper createDriverWrapper(SessionContext context) throws RhConfigurationException
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
			throw new RhConfigurationException("Invalid driver URL", e);
		}
	}
}
