package com.exactprosystems.remotehand.webelements;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CssSelector extends WebLocator
{
	@Override
	public By getWebLocator(WebDriver webDrv, Map<String, String> params)
	{
		return By.cssSelector(params.get(WebLocator.MATCHER));
	}
}
