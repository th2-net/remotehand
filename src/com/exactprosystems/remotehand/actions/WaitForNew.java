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

public class WaitForNew extends WebAction
{
	private static Logger logger = Logger.getLogger();

	private static enum ActionParams
	{
		expiredSeconds, checkEveryMSecs
	};

	public WaitForNew()
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
		final int expSecs = parseToInt(params.get(ActionParams.expiredSeconds.toString()), ActionParams.expiredSeconds.toString()), 
				checkSecs = parseToInt(params.get(ActionParams.checkEveryMSecs.toString()), ActionParams.checkEveryMSecs.toString());

		try
		{
			(new WebDriverWait(webDriver, expSecs)).until(new ExpectedCondition<Boolean>()
			{
				List<WebElement> previusElements = null;

				@Override
				public Boolean apply(WebDriver driver)
				{
					List<WebElement> elements = driver.findElements(webLocator);

					boolean foundEquals = false;
					if (previusElements != null)
					{
						foundEquals = elements.equals(previusElements);

						if (!foundEquals)
							try
							{
								Thread.sleep(checkSecs);
							}
							catch (InterruptedException e)
							{
								// do nothing
							}
					}

					previusElements = elements;

					return foundEquals;
				}
			});

			logger.info("Appeared locator: '" + webLocator.toString() + "'");
		}
		catch (TimeoutException ex)
		{
			throw new ScriptExecuteException("Timed out after " + expSecs + " seconds waiting for '" + webLocator.toString());
		}

		return null;
	}

	private int parseToInt(String str, String param) throws ScriptExecuteException
	{
		int num;
		try
		{
			num = Integer.parseInt(str);
		}
		catch (NumberFormatException ex)
		{
			throw new ScriptExecuteException("Error while parsing parameter '" + param + "' = '" + str + "'. It must to be numeric.");
		}

		return num;
	}
}
