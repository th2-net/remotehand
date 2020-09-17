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
import org.apache.commons.lang3.StringUtils;
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
	
	private final Map<String, String> record;
	private final WindowsDriver<?> driver;
	private final CachedWebElements webElements;
	
	private static final Pair<String, String> DEFAULT_KEYS = new ImmutablePair<>("locator", "matcher");

	public ElementSearcher(Map<String, String> record, WindowsDriver<?> driver, CachedWebElements webElements) {
		this.record = record;
		this.driver = driver;
		this.webElements = webElements;
	}

	private List<Pair<String, String>> processFrom(Pair<String, String> keys) {
		
		int ind = 1;
		String locator, matcher;
		List<Pair<String, String>> l = new ArrayList<>();
		do {
			String indexSuffix = ind == 1 ? "" : String.valueOf(ind);
			locator = record.get(keys.getKey() + indexSuffix);
			matcher = record.get(keys.getValue() + indexSuffix);
			ind++;
			if (StringUtils.isNotEmpty(locator) && StringUtils.isNotEmpty(matcher)) {
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
	
	public boolean isLocatorsAvailable(Pair<String, String> keys) {
		return this.record.get(keys.getKey()) != null && this.record.get(keys.getValue()) != null;
	}

	public boolean isLocatorsAvailable() {
		return isLocatorsAvailable(DEFAULT_KEYS);
	}

	public WebElement searchElement() throws ScriptExecuteException {
		return searchElement(DEFAULT_KEYS);
	}
	
	public WebElement searchElement(Pair<String, String> keys) throws ScriptExecuteException {
		List<Pair<String, String>> pairs = this.processFrom(keys);

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

	public WebElement searchElementWithoutWait(int implicitTimeout) throws ScriptExecuteException {
		return searchElementWithoutWait(DEFAULT_KEYS, implicitTimeout);
	}

	public WebElement searchElementWithoutWait(Pair<String, String> keys, int implicitTimeout) throws ScriptExecuteException {
		driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		try {
			return searchElement(keys);
		} catch (NoSuchElementException e) {
			logger.trace("Element not found");
			return null;
		} finally {
			driver.manage().timeouts().implicitlyWait(implicitTimeout, TimeUnit.SECONDS);	
		}
	}
	
	
}
