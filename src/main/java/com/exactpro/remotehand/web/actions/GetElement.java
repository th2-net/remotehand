/*
 * Copyright 2020-2024 Exactpro (Exactpro Systems Limited)
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

package com.exactpro.remotehand.web.actions;

import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.web.WebAction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Map;

public class GetElement extends WebAction {
	public GetElement() {
		super(true, true);
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException {
		WebElement element = findElement(webDriver, webLocator);
		String elementHTML = getElementHtml(element);

		logger.info("Obtained element: {}.", elementHTML);

		String id = params.getOrDefault(FindElement.PARAM_ID, "");
		if (!id.isEmpty()) {
			context.getContextData().put(id, element.getText());
			id += "=";
		}

		return id + elementHTML;
	}
	
	protected String getElementHtml(WebElement element) {
		return element.getAttribute("outerHTML");
	}
}