package com.exactprosystems.remotehand.actions;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.exactprosystems.remotehand.Logger;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.WebAction;

public class Wait extends WebAction
{
	private final static Logger logger = Logger.getLogger();
	
	private static enum ActionParams
	{
		seconds
	};

	public Wait()
	{
		super.needLocator = false;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		int secs = 0;
		try
		{
			secs = Integer.parseInt(params.get(ActionParams.seconds.toString()));
		}
		catch (NumberFormatException ex)
		{
			throw new ScriptExecuteException("Error while parsing parameter '" + 
					ActionParams.seconds.toString() + "' = '" + params.get(ActionParams.seconds.toString()) + "'. It must to be numeric.");
		}
		
		logger.info("Pause for "+secs+" second(s)");

		try
		{
			(new WebDriverWait(webDriver, secs)).until((new ExpectedCondition<Boolean>()
			{
				@Override
				public Boolean apply(WebDriver driver)
				{
					return false;
				}
			}));
		}
		catch (TimeoutException ex)
		{
			// Nothing should happen, it's normal to have timeout here
		}

		return null;
	}

}
