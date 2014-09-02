package com.exactprosystems.remotehand.actions;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.exactprosystems.remotehand.Logger;
import com.exactprosystems.remotehand.WebAction;

public class Click extends WebAction
{
	private static Logger logger = Logger.getLogger();

	public Click()
	{
		super.needLocator = true;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params)
	{
		WebElement element = webDriver.findElement(webLocator);
		element.click();

		logger.info("Clicked on: '" + element.toString());

		return null;
	}
}