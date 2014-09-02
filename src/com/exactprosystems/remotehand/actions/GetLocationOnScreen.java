package com.exactprosystems.remotehand.actions;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.Locatable;

import com.exactprosystems.remotehand.Logger;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.WebAction;

public class GetLocationOnScreen extends WebAction
{
	private static Logger logger = Logger.getLogger();

	public GetLocationOnScreen()
	{
		super.needLocator = true;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		WebElement element = webDriver.findElement(webLocator);

		if (element instanceof Locatable)
		{
			((Locatable)element).getCoordinates().inViewPort();
			logger.info("Locate on: " + webLocator.toString());
		}
		else
			throw new ScriptExecuteException("Can't scroll to the following web element: '" + element.toString() + "'");

		return null;
	}

}
