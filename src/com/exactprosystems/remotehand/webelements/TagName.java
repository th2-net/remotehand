package com.exactprosystems.remotehand.webelements;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class TagName extends WebLocator
{
	@Override
	public By getWebLocator(WebDriver webDriver, Map<String, String> params)
	{
		return By.tagName(params.get(WebLocator.MATCHER));
	}

}
