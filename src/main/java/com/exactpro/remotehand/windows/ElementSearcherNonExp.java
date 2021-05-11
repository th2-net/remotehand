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
import io.appium.java_client.windows.WindowsDriver;
import org.apache.commons.lang3.NotImplementedException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ElementSearcherNonExp extends ElementSearcher {

	private static final Logger logger = LoggerFactory.getLogger(ElementSearcherNonExp.class);
	
	protected WindowsDriver<?> driverNotExp;
	
	public ElementSearcherNonExp(Map<String, String> record, WindowsDriver<?> driverExp, WindowsDriver<?> driverNotExp,
								 WindowsSessionContext.CachedWebElements webElements) {
		super(record, driverExp, webElements);
		this.driverNotExp = driverNotExp;
	}

	@Override
	public WebElement searchElement(SearchParams.HeaderKeys keys) throws ScriptExecuteException {
		List<SearchParams> pairs = this.processFrom(keys);

		WebElement we = null;
		WebElement lastSavedEl = null;
		for (SearchParams pair : pairs) {
			By by;
			Integer index = null;
			if ("cachedId".equals(pair.locator)) {
				logger.trace("Get element from cache by {} = {}", pair.locator, pair.matcher);
				lastSavedEl = getCachedElement(pair);
				String runtimeId = this.getRuntimeId(lastSavedEl);
				if (logger.isDebugEnabled()) {
					logger.debug("Found rh-id {} win_id {}", pair.matcher, runtimeId);
				}
				by = By.id(runtimeId);
			} else {
				by = parseBy(pair.locator, pair.matcher);
				index = pair.parsedIndex;
				logger.trace("Searching by {} = {}", pair.locator, pair.matcher);
			}
			
			we = findWebElement(we == null ? driverNotExp : we, by, index);
		}

		return findWebElement(lastSavedEl == null ? driver : lastSavedEl, By.id(this.getRuntimeId(we)), null);
	}

	@Override
	public List<WebElement> searchElements(SearchParams.HeaderKeys keys) throws ScriptExecuteException {
		throw new NotImplementedException("Not implemented");
	}

	protected String getRuntimeId(WebElement webElement) throws ScriptExecuteException {
		if (webElement instanceof RemoteWebElement) {
			return ((RemoteWebElement) webElement).getId();
		} else {
			logger.error("Expected message class type: {}, actual: {}", RemoteWebElement.class.getName(),
					webElement.getClass().getName());
			throw new ScriptExecuteException("Incorrect found element");
		}
	}
	
	
}
