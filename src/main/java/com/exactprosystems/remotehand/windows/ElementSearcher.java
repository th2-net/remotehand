/*
 * Copyright 2020-2021 Exactpro (Exactpro Systems Limited)
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
import org.openqa.selenium.SearchContext;
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
	
	private static final Pair<String,  Pair<String, String>> DEFAULT_KEYS
			= new ImmutablePair<>("locator", new ImmutablePair<>("matcher", "matcherindex"));

	public ElementSearcher(Map<String, String> record, WindowsDriver<?> driver, CachedWebElements webElements) {
		this.record = record;
		this.driver = driver;
		this.webElements = webElements;
	}

	private List<Pair<String, Pair<String, Integer>>> processFrom(Pair<String,  Pair<String, String>> keys) {
		
		int ind = 1;
		String locator, matcher;
		Integer matcherIndex;
		List<Pair<String, Pair<String, Integer>>> l = new ArrayList<>();
		do {
			String indexSuffix = ind == 1 ? "" : String.valueOf(ind);
			locator = record.get(keys.getKey() + indexSuffix);
			Pair<String, String> matcherPair = keys.getValue();
			matcher = record.get(matcherPair.getKey() + indexSuffix);
			String matcherIndexStr = record.get(matcherPair.getValue() + indexSuffix);
			matcherIndex = StringUtils.isEmpty(matcherIndexStr) ? null : Integer.parseInt(matcherIndexStr);
			ind++;
			if (StringUtils.isNotEmpty(locator) && StringUtils.isNotEmpty(matcher)) {
				l.add(new ImmutablePair<>(locator, new ImmutablePair<>(matcher, matcherIndex)));
			}
			
		} while (locator != null && matcher != null);
		
		return l;

	}
	
	private By parseBy (String using, String id) {

		switch (using.toLowerCase()) {
			case "accessibilityid" : return new ByAccessibilityId(id);
			case "name" : return new By.ByName(id);
			case "tagname" : return new By.ByTagName(id);
			case "xpath": return new By.ByXPath(id);
		}
		throw new IllegalArgumentException("unknown using methods");
	}
	
	public boolean isLocatorsAvailable(Pair<String,  Pair<String, String>> keys) {
		return this.record.get(keys.getKey()) != null && this.record.get(keys.getValue().getKey()) != null;
	}

	public boolean isLocatorsAvailable() {
		return isLocatorsAvailable(DEFAULT_KEYS);
	}

	public WebElement searchElement() throws ScriptExecuteException {
		return searchElement(DEFAULT_KEYS);
	}
	
	public WebElement searchElement(Pair<String,  Pair<String, String>> keys) throws ScriptExecuteException {
		List<Pair<String, Pair<String, Integer>>> pairs = this.processFrom(keys);

		WebElement we = null;
		for (Pair<String, Pair<String, Integer>> pair : pairs) {

			Pair<String, Integer> matcher = pair.getValue();
			if ("cachedId".equals(pair.getKey())) {
				logger.trace("Get element from cache by {} = {}", pair.getKey(), matcher.getKey());
				we = webElements.getWebElement(matcher.getKey());
				if (we == null) {
					throw new ScriptExecuteException("Saved elements with rh-id " + matcher.getKey() + " is not found");
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Found rh-id {} win_id {}", matcher.getKey(),
							we instanceof RemoteWebElement ? ((RemoteWebElement) we).getId() : "");
				}
			} else {
				By by = parseBy(pair.getKey(), matcher.getKey());
				logger.trace("Searching by {} = {}", pair.getKey(), matcher.getKey());
				we = findWebElement(we == null ? driver : we, by, matcher.getValue());
			}
		}
		
		return we;
	}

	public WebElement searchElementWithoutWait() throws ScriptExecuteException {
		return searchElementWithoutWait(DEFAULT_KEYS);
	}

	public WebElement searchElementWithoutWait(Pair<String, Pair<String, String>> keys) throws ScriptExecuteException {
		Integer implicitlyWaitTimeout = WindowsConfiguration.getInstance().getImplicitlyWaitTimeout();
		return searchElement(keys,
				() -> driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS),
				() -> driver.manage().timeouts().implicitlyWait(implicitlyWaitTimeout, TimeUnit.SECONDS));
	}

	public WebElement searchElementWithWait() throws ScriptExecuteException {
		return searchElementWithWait(WindowsConfiguration.getInstance().getImplicitlyWaitTimeout());
	}

	public WebElement searchElementWithWait(Pair<String,  Pair<String, String>> keys) throws ScriptExecuteException {
		return searchElementWithWait(keys, WindowsConfiguration.getInstance().getImplicitlyWaitTimeout());
	}

	public WebElement searchElementWithWait(int implicitTimeout) throws ScriptExecuteException {
		return searchElementWithWait(DEFAULT_KEYS, implicitTimeout);
	}

	public WebElement searchElementWithWait(Pair<String,  Pair<String, String>> keys, int implicitTimeout) throws ScriptExecuteException {
		Integer implicitlyWaitTimeout = WindowsConfiguration.getInstance().getImplicitlyWaitTimeout();

		Runnable beforeSearch, afterSearch;
		if (implicitlyWaitTimeout.equals(implicitTimeout)) {
			beforeSearch = null;
			afterSearch = null;
		} else {
			beforeSearch = () -> driver.manage().timeouts().implicitlyWait(implicitTimeout, TimeUnit.SECONDS);
			afterSearch = () -> driver.manage().timeouts().implicitlyWait(implicitlyWaitTimeout, TimeUnit.SECONDS);
		}

		return searchElement(keys, beforeSearch, afterSearch);
	}

	public WebElement searchElement(Pair<String, Pair<String, String>> keys, Runnable beforeSearch, Runnable afterSearch) throws ScriptExecuteException {
		if (beforeSearch != null)
			beforeSearch.run();
		try {
			return searchElement(keys);
		} catch (NoSuchElementException e) {
			logger.trace("Element not found");
			return null;
		} finally {
			if (afterSearch != null)
				afterSearch.run();
		}
	}


	private <T extends SearchContext> WebElement findWebElement(T element, By by, Integer matcherIndex) {
		return matcherIndex == null ? element.findElement(by) : element.findElements(by).get(matcherIndex);
	}
}
