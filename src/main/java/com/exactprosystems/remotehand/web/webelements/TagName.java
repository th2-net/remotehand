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

package com.exactprosystems.remotehand.web.webelements;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class TagName extends WebLocator
{
	@Override
	public By getWebLocator(WebDriver webDriver, String matcher)
	{
		return By.tagName(matcher);
	}
	
	@Override
	public By getWebLocator(WebDriver webDriver, Map<String, String> params)
	{
		return getWebLocator(webDriver, params.get(WebLocator.MATCHER));
	}
}
