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

package com.exactpro.remotehand.windows;

import com.exactpro.remotehand.DriverPoolProvider;
import com.exactpro.remotehand.RhConfigurationException;
import com.exactpro.remotehand.sessions.SessionContext;

import java.net.MalformedURLException;
import java.net.URL;

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
