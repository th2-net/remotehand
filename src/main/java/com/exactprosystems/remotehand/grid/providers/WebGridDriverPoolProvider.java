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
import java.util.HashMap;
import java.util.Map;

import com.exactprosystems.remotehand.web.WebConfiguration;
import com.exactprosystems.remotehand.web.WebDriverPoolProvider;
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
	public WebDriverWrapper createDriverWrapper(SessionContext context) throws RhConfigurationException
	{
		return createDriver(context);
	}

	@Override
	public void closeDriver(String sessionId, WebDriverWrapper driver)
	{
		super.closeDriver(sessionId, driver);
		closeDriver(driver);
	}

	@Override
	public void clearDriverPool()
	{
		driversPool.forEach((session, driverWrapper) -> {
				sessionTargetUrls.remove(session);
				closeDriver(driverWrapper);
			});
		driversPool.clear();
	}


	@Override
	protected WebDriverWrapper createDriver(SessionContext context) throws RhConfigurationException
	{
		String driverUrl = sessionTargetUrls.get(context.getSessionId());
		WebConfiguration cfg = WebConfiguration.getInstance();
		ChromeOptions chromeOptions = buildChromeOptions(cfg);
		try
		{
			RemoteWebDriver driver = new RemoteWebDriver(new URL(driverUrl + "/wd/hub/"), chromeOptions);
			return new WebDriverWrapper(driver, null);
		}
		catch (MalformedURLException e)
		{
			throw new RhConfigurationException("Invalid driver URL", e);
		}
	}


	private ChromeOptions buildChromeOptions(WebConfiguration cfg)
	{
		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.addArguments("--no-sandbox", "--ignore-ssl-errors=yes", "--ignore-certificate-errors");

		Map<String, Object> prefs = new HashMap<>(1);
		prefs.put("profile.content_settings.exceptions.clipboard", 
				WebDriverPoolProvider.createClipboardSettingsChrome(cfg.isReadClipboardPermissions()));
		chromeOptions.setExperimentalOption("prefs", prefs);
		
		return chromeOptions;
	}

	private void closeDriver(WebDriverWrapper driver)
	{
		try
		{
			driver.getDriver().quit();
		}
		catch (Exception e)
		{
			logger.error("Error while closing driver", e);
		}
	}
}
