/******************************************************************************
 * Copyright 2009-2020 Exactpro Systems Limited
 * https://www.exactpro.com
 * Build Software to Test Software
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
 ******************************************************************************/

package com.exactprosystems.remotehand.windows;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;

import com.exactprosystems.remotehand.DriverPoolProvider;
import com.exactprosystems.remotehand.RhConfigurationException;
import com.exactprosystems.remotehand.sessions.SessionContext;

public class WindowsDriverPoolProvider implements DriverPoolProvider<WindowsDriverWrapper>
{
	private static final String URL_DELIMITER = "/";


	@Override
	public WindowsDriverWrapper getDriverWrapper(SessionContext context) throws RhConfigurationException
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
	public void closeDriver(String sessionId, WebDriver driver)
	{
	
	}

	private String getConnectionUrl()
	{
		WindowsConfiguration instance = WindowsConfiguration.getInstance();
		String urlPath = instance.getWinAppUrlPath();
		if (!urlPath.startsWith(URL_DELIMITER))
		{
			urlPath = URL_DELIMITER + urlPath;
		}
		if (!urlPath.endsWith(URL_DELIMITER))
		{
			urlPath = urlPath + URL_DELIMITER;
		}

		return String.format("http://%s:%s%s", instance.getWinAppHost(), instance.getWinAppPort(), urlPath);
	}
}
