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

package com.exactpro.remotehand.windows;

import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.windows.WindowsSessionContext.CachedWebElements;
import com.exactpro.remotehand.windows.locator.ByAccessibilityId;
import io.appium.java_client.windows.WindowsDriver;
import org.apache.commons.lang3.StringUtils;
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

	public ElementSearcher(Map<String, String> record, WindowsDriver<?> driver, CachedWebElements webElements) {
		this.record = record;
		this.driver = driver;
		this.webElements = webElements;
	}

	private List<SearchParams> processFrom(SearchParams.HeaderKeys keys) {
		int ind = 1;
		String locator, matcher;
		List<SearchParams> l = new ArrayList<>();
		do {
			String indexSuffix = ind == 1 ? "" : String.valueOf(ind);
			locator = record.get(keys.locator + indexSuffix);
			matcher = record.get(keys.matcher + indexSuffix);
			String matcherIndexStr = record.get(keys.index + indexSuffix);
			Integer matcherIndex = StringUtils.isEmpty(matcherIndexStr) ? null : Integer.parseInt(matcherIndexStr);
			ind++;
			if (StringUtils.isNotEmpty(locator) && StringUtils.isNotEmpty(matcher) || isRootLocator(locator)) {
				l.add(new SearchParams(locator, matcher, matcherIndex));
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
			case "root": return new By.ByName(driver.getTitle());
		}
		throw new IllegalArgumentException("unknown using methods");
	}
	
	public boolean isLocatorsAvailable(SearchParams.HeaderKeys keys) {
		return this.record.get(keys.locator) != null && this.record.get(keys.matcher) != null;
	}

	public boolean isLocatorsAvailable() {
		return isLocatorsAvailable(SearchParams.HeaderKeys.DEFAULT);
	}

	public WebElement searchElement() throws ScriptExecuteException {
		return searchElement(SearchParams.HeaderKeys.DEFAULT);
	}
	
	public WebElement searchElement(SearchParams.HeaderKeys keys) throws ScriptExecuteException {
		List<SearchParams> pairs = this.processFrom(keys);

		WebElement we = null;
		for (SearchParams pair : pairs) {
			if ("cachedId".equals(pair.locator)) {
				logger.trace("Get element from cache by {} = {}", pair.locator, pair.matcher);
				we = webElements.getWebElement(pair.matcher);
				if (we == null) {
					throw new ScriptExecuteException("Saved elements with rh-id " + pair.matcher + " is not found");
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Found rh-id {} win_id {}", pair.matcher,
							we instanceof RemoteWebElement ? ((RemoteWebElement) we).getId() : "");
				}
			} else {
				By by = parseBy(pair.locator, pair.matcher);
				logger.trace("Searching by {} = {}", pair.locator, pair.matcher);
				we = findWebElement(we == null ? driver : we, by, pair.parsedIndex);
			}
		}
		
		return we;
	}

	public WebElement searchElementWithoutWait() throws ScriptExecuteException {
		return searchElementWithoutWait(SearchParams.HeaderKeys.DEFAULT);
	}

	public WebElement searchElementWithoutWait(SearchParams.HeaderKeys keys) throws ScriptExecuteException {
		Integer implicitlyWaitTimeout = WindowsConfiguration.getInstance().getImplicitlyWaitTimeout();
		return searchElement(keys,
				() -> driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS),
				() -> driver.manage().timeouts().implicitlyWait(implicitlyWaitTimeout, TimeUnit.SECONDS));
	}

	public WebElement searchElementWithWait() throws ScriptExecuteException {
		return searchElementWithWait(WindowsConfiguration.getInstance().getImplicitlyWaitTimeout());
	}

	public WebElement searchElementWithWait(SearchParams.HeaderKeys keys) throws ScriptExecuteException {
		return searchElementWithWait(keys, WindowsConfiguration.getInstance().getImplicitlyWaitTimeout());
	}

	public WebElement searchElementWithWait(int implicitTimeout) throws ScriptExecuteException {
		return searchElementWithWait(SearchParams.HeaderKeys.DEFAULT, implicitTimeout);
	}

	public WebElement searchElementWithWait(SearchParams.HeaderKeys keys, int implicitTimeout) throws ScriptExecuteException {
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

	public WebElement searchElement(SearchParams.HeaderKeys keys, Runnable beforeSearch, Runnable afterSearch) throws ScriptExecuteException {
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

	private boolean isRootLocator(String locatorAsString) {
		return "root".equals(locatorAsString);
	}
}
