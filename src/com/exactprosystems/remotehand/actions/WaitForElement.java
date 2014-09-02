package com.exactprosystems.remotehand.actions;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.exactprosystems.remotehand.Logger;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.WebAction;

public class WaitForElement extends WebAction
{
	private final static Logger logger = Logger.getLogger();

	private static enum ActionParams
	{
		seconds
	};

	public WaitForElement()
	{
		super.needLocator = true;

		String[] result = new String[ActionParams.values().length];
		for (int inx = 0; inx < result.length; inx++)
		{
			result[inx] = ActionParams.values()[inx].toString();
		}
		super.mandatoryParams = result;
	}

	@Override
	public String run(WebDriver webDriver, final By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		int secs = 0;
		try
		{
			secs = Integer.parseInt(params.get(ActionParams.seconds.toString()));
		}
		catch (NumberFormatException ex)
		{
			throw new ScriptExecuteException("Error while parsing parameter '" + ActionParams.seconds.toString() + 
					"' = '" + params.get(ActionParams.seconds.toString()) + "'. It must to be numeric.");
		}

		try
		{
			(new WebDriverWait(webDriver, secs)).until(new ExpectedCondition<Boolean>()
			{
				@Override
				public Boolean apply(WebDriver driver)
				{
					List<WebElement> elements = driver.findElements(webLocator);

					return elements.size() > 0;
				}
			});

			logger.info("Appeared locator: '" + webLocator.toString() + "'");
		}
		catch (TimeoutException ex)
		{
			throw new ScriptExecuteException("Timed out after " + secs + " seconds waiting for '" + webLocator.toString());
		}

		return null;
	}
}
