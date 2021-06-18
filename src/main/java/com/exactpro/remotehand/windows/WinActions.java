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

import com.exactpro.remotehand.ExtendedActions;
import com.exactpro.remotehand.web.utils.SendKeysHandler;
import org.openqa.selenium.WebDriver;

public class WinActions extends ExtendedActions {
	public WinActions(WebDriver driver) {
		super(driver);
	}


	@Override
	protected String[] extractKeys(String modifiers) {
		String trimModifiers = modifiers.trim();
		if (!trimModifiers.startsWith(SendKeysHandler.KEY_SIGN) && !trimModifiers.endsWith(SendKeysHandler.KEY_SIGN))
			return new String[] { };

		return trimModifiers.substring(1, trimModifiers.length() - 1).split("\\+");
	}
}
