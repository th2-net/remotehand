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

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.windows.WindowsSessionContext.CachedWebElements;
import com.exactprosystems.remotehand.windows.locator.ByAccessibilityId;
import io.appium.java_client.windows.WindowsDriver;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ElementSearcher {

	private static final Logger logger = LoggerFactory.getLogger(ElementSearcher.class);
	
	private List<Pair<String, String>> processFrom(Map<String, String> str000) {
		
		int ind = 1;
		String locator, matcher;
		List<Pair<String, String>> l = new ArrayList<>();
		do {
			locator = str000.get("locator" + (ind == 1 ? "" : ind));
			matcher = str000.get("matcher" + (ind == 1 ? "" : ind));
			ind++;
			if (locator != null && matcher != null) {
				l.add(new ImmutablePair<>(locator, matcher));
			}
			
		} while (locator != null && matcher != null);
		
		return l;

	}
	
	private By parseBy (String using, String id) {

		switch (using.toLowerCase()) {
			case "accessibilityid" : return new ByAccessibilityId(id);
			case "name" : return new By.ByName(id);
			case "tagname" : return new By.ByTagName(id);
		}
		throw new IllegalArgumentException("unknown using methods");
	}
	
	
	public WebElement searchElement(Map<String, String> map, WindowsDriver<?> driver,
									CachedWebElements webElements) throws ScriptExecuteException {
		List<Pair<String, String>> pairs = this.processFrom(map);

		WebElement we = null;
		for (Pair<String, String> pair : pairs) {

			String value = pair.getValue();
			if ("cachedId".equals(pair.getKey())) {
				logger.trace("Get element from cache by {} = {}", pair.getKey(), value);
				we = webElements.getWebElement(value);
				if (we == null) {
					throw new ScriptExecuteException("Saved elements with rh-id " + value + " is not found");
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Found rh-id {} win_id {}", value,
							we instanceof RemoteWebElement ? ((RemoteWebElement) we).getId() : "");
				}
			} else {
				By by = parseBy(pair.getKey(), value);
				logger.trace("Searching by {} = {}", pair.getKey(), value);

				if (we == null) {
					we = driver.findElement(by);
				} else {
					we = we.findElement(by);
				}
			}
		}
		
		return we;
	}

	public WebElement searchElementWithoutWait(Map<String, String> map, WindowsDriver<?> driver, int implicitTimeout) {
		List<Pair<String, String>> pairs = this.processFrom(map);
		
		driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		try {
			WebElement we = null;
			for (Pair<String, String> pair : pairs) {

				By by = parseBy(pair.getKey(), pair.getValue());
				logger.trace("Searching by {} = {}", pair.getKey(), pair.getValue());

				if (we == null) {
					we = driver.findElement(by);
					if (logger.isTraceEnabled()) {
						logger.trace("Element found: {}.", (we instanceof RemoteWebElement)
								? ((RemoteWebElement) we).getId() : "");
					}
					if (we == null) {
						return null;
					}

				} else {
					we = we.findElement(by);
					if (logger.isTraceEnabled()) {
						logger.trace("Element found: {}.", (we instanceof RemoteWebElement)
								? ((RemoteWebElement) we).getId() : "");
					}
					if (we == null) {
						return null;
					}
				}
			}

			return we;
		} catch (NoSuchElementException e) {
			logger.trace("Element not found");
			return null;
		} finally {
			driver.manage().timeouts().implicitlyWait(implicitTimeout, TimeUnit.SECONDS);	
		}
	}
	
	
}
