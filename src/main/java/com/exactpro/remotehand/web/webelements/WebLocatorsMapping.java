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

package com.exactpro.remotehand.web.webelements;

import com.exactpro.remotehand.ScriptCompileException;

public class WebLocatorsMapping
{
	private enum WebLocatorName
	{
		cssSelector, 
		tagName, 
		id, 
		xpath;

		private static WebLocatorName getByLabel(String label) throws ScriptCompileException
		{
			for (WebLocatorName name : WebLocatorName.values())
				if (name.toString().equalsIgnoreCase(label))
					return name;

			throw new ScriptCompileException("Web locator '" + label + "' not found in locators list");
		}
	};

	public static WebLocator getByName(String locatorName) throws ScriptCompileException
	{
		if (locatorName.isEmpty())
			return null;

		switch (WebLocatorName.getByLabel(locatorName))
		{
		case cssSelector : return new CssSelector();
		case tagName :     return new TagName();
		case id :          return new Id();
		case xpath :       return new XPath();
		default : throw new ScriptCompileException("Unknown locator name '" + locatorName + "'");
		}
	}
}
