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

package com.exactpro.remotehand.windows.locator;

import io.appium.java_client.FindsByAccessibilityId;
import io.appium.java_client.FindsByFluentSelector;
import io.appium.java_client.MobileSelector;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.util.List;

public class ByAccessibilityId extends By {
	
	private final String accessibilityId;
	
	public ByAccessibilityId(String accessibilityId) {
		this.accessibilityId = accessibilityId;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<WebElement> findElements(SearchContext context) {
		if (context instanceof FindsByAccessibilityId) {
			return ((FindsByAccessibilityId<WebElement>) context).findElementsByAccessibilityId(accessibilityId);
		} else if (context instanceof FindsByFluentSelector) {
			return ((FindsByFluentSelector<WebElement>) context).findElements(MobileSelector.ACCESSIBILITY.toString(), accessibilityId);
		} else if (context instanceof RemoteWebElement) {
			WebElementDecorator webElementDecorator = new WebElementDecorator((RemoteWebElement) context);
			return webElementDecorator.findElements(MobileSelector.ACCESSIBILITY.toString(), accessibilityId);
		}
		throw new IllegalArgumentException("Illegal context argument.");
	}

	@Override
	@SuppressWarnings("unchecked")
	public WebElement findElement(SearchContext context) {
		if (context instanceof FindsByAccessibilityId) {
			return ((FindsByAccessibilityId<WebElement>) context).findElementByAccessibilityId(accessibilityId);
		} else if (context instanceof FindsByFluentSelector) {
			return ((FindsByFluentSelector<WebElement>) context).findElement(MobileSelector.ACCESSIBILITY.toString(), accessibilityId);
		} else if (context instanceof RemoteWebElement) {
			WebElementDecorator webElementDecorator = new WebElementDecorator((RemoteWebElement) context);
			return webElementDecorator.findElement(MobileSelector.ACCESSIBILITY.toString(), accessibilityId);
		}
		throw new IllegalArgumentException("Illegal context argument.");
	}
	
	
	private static class WebElementDecorator extends RemoteWebElement {
		
		protected WebElementDecorator (RemoteWebElement webElement) {
			
			this.id = webElement.getId();
			this.parent = (RemoteWebDriver) webElement.getWrappedDriver();
		}

		@Override
		protected WebElement findElement(String using, String value) {
			return super.findElement(using, value);
		}

		@Override
		protected List<WebElement> findElements(String using, String value) {
			return super.findElements(using, value);
		}
	}
}
