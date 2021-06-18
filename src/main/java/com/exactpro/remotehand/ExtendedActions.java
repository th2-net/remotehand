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

package com.exactpro.remotehand;

import com.exactpro.remotehand.web.utils.SendKeysHandler;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

import java.util.LinkedHashSet;
import java.util.Set;

public class ExtendedActions extends Actions {
	public ExtendedActions(WebDriver driver) {
		super(driver);
	}

	public Set<CharSequence> applyClickModifiers(String modifiers) {
		if (StringUtils.isEmpty(modifiers))
			return null;

		String[] keys = extractKeys(modifiers);
		Set<CharSequence> appliedModifiers = new LinkedHashSet<>(keys.length);
		for (String key : keys) {
			CharSequence c = SendKeysHandler.KEYS.get(key.trim().toLowerCase());
			if (c == null || appliedModifiers.contains(c))
				continue;
			this.keyDown(c);
			appliedModifiers.add(c);
		}

		return appliedModifiers;
	}

	public ExtendedActions applyClickModifiers(Set<CharSequence> modifiers) {
		modifiers.forEach(mod -> {
			if (StringUtils.isNotEmpty(mod))
				this.keyDown(mod);
		});

		return this;
	}

	public ExtendedActions resetClickModifiers(Set<CharSequence> modifiers) {
		if (modifiers == null || modifiers.isEmpty())
			return this;

		modifiers.forEach(this::keyUp);

		return this;
	}


	protected String[] extractKeys(String modifiers) {
		return modifiers.split(",");
	}
}
