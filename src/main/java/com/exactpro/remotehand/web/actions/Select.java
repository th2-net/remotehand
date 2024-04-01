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

import java.util.Map;

public class Select extends WebAction {
	private static final String TEXT = "text";
	private static final String DEFAULT_TEXT = "default";
	private static final String NO_OPTION_FAIL_PARAM = "nooptionfail";

	public Select() {
		super(true, true, TEXT);
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException {
		org.openqa.selenium.support.ui.Select dropdown = new org.openqa.selenium.support.ui.Select(webDriver.findElement(webLocator));

		String option = getParams().get(TEXT);
		String default_option = getParams().get(DEFAULT_TEXT);
		if (!doesOptionExist(dropdown, option) && default_option != null && !default_option.isEmpty())
			option = default_option;

		if (doesOptionExist(dropdown, option) || noOptionFail()) {
			dropdown.selectByVisibleText(option);
			logger.info("Option " + option + " is selected in element with locator " + webLocator);
		} else {
			logger.info("Option " + option + " doesn't exist. It hasn't been selected");
		}

		return null;
	}

	private boolean doesOptionExist(org.openqa.selenium.support.ui.Select dropdown, String text) {
		return dropdown.getOptions().stream().anyMatch(option -> option.getText().equals(text));
	}

	private boolean noOptionFail() {
		return !getParams().containsKey(NO_OPTION_FAIL_PARAM) || RhUtils.YES.contains(getParams().get(NO_OPTION_FAIL_PARAM));
	}
}