/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.windows.locator;

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
