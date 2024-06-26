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

import com.exactpro.remotehand.utils.RhUtils;
import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.web.WebAction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.Map;

public class SetCheckbox extends WebAction {
	private static final String PARAM_CHECKED = "checked";
	private static final String STATE_CHECKED = "checked";
	private static final String STATE_UNCHECKED = "unchecked";

	public SetCheckbox() {
		super(true, true);
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException {
		WebElement checkbox = findElement(webDriver, webLocator);
		boolean shouldBeChecked = !RhUtils.NO.contains(params.get(PARAM_CHECKED));
		switchCheckbox(checkbox, shouldBeChecked);
		return null;
	}
	
	private void switchCheckbox(WebElement checkbox, boolean shouldBeChecked) throws ScriptExecuteException {
		String newState = shouldBeChecked ? STATE_CHECKED : STATE_UNCHECKED;

		boolean checkedNow = isCheckboxChecked(checkbox);

		if (shouldBeChecked == checkedNow) {
			logger.info("Checkbox is already in required state: {}", newState);
			return;
		}
		
		try {
			checkbox.click();
		} catch (WebDriverException e) {
			if (e.getMessage().contains("Element is not clickable") && checkbox.isSelected() != shouldBeChecked) {
				WebElement parent = checkbox.findElement(By.xpath(".."));
				logger.info("Trying to click on parent element: {}", parent);
				parent.click();
				if (checkbox.isSelected() != shouldBeChecked)
					throw new ScriptExecuteException(String.format("Cannot change state of checkbox '%s'", checkbox), e);
			} else {
				throw e;
			}
		}

		logger.info("Checkbox has been successfully updated, new state: {}", newState);
	}

	private boolean isCheckboxChecked(WebElement checkbox) {
		String type = checkbox.getAttribute("type");
		String clazz = checkbox.getAttribute("class");
		if ("checkbox".equalsIgnoreCase(type) || clazz == null) {
			return checkbox.isSelected();
		} else {
			return clazz.contains(STATE_CHECKED) && !clazz.contains(STATE_UNCHECKED);
		}
	}
}