/*
 * Copyright 2020-2020 Exactpro (Exactpro Systems Limited)
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

package com.exactpro.remotehand.web;

public enum Browser
{
	IE("IE"),
	EDGE("Edge"),
	CHROME("Chrome"),
	CHROME_HEADLESS("Chrome-Headless"),
	FIREFOX("Firefox"),
	FIREFOX_HEADLESS("Firefox-Headless"),
	INVALID("");

	private String label;

	Browser(String label)
	{
		this.label = label;
	}

	public String getLabel()
	{
		return label;
	}

	public static Browser valueByLabel(String label)
	{
		if (label == null)
			return INVALID;

		for (Browser b : values())
			if (b.label.equals(label))
				return b;
		return INVALID;
	}
}
