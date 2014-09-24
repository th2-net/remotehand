package com.exactprosystems.remotehand.webelements;

import com.exactprosystems.remotehand.ScriptCompileException;

public class WebLocatorsMapping
{
	private static WebLocatorsMapping mapping = new WebLocatorsMapping();

	public static WebLocatorsMapping getInstance()
	{
		return mapping;
	}

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

	public WebLocator getByName(String locatorName) throws ScriptCompileException
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