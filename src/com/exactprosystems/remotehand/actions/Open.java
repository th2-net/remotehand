package com.exactprosystems.remotehand.actions;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.exactprosystems.remotehand.Logger;
import com.exactprosystems.remotehand.WebAction;

public class Open extends WebAction
{
	private static Logger logger = Logger.getLogger();

	private static enum ActionParams
	{
		url
	};

	public Open()
	{
		super.needLocator = false;

		String[] result = new String[ActionParams.values().length];
		for (int inx = 0; inx < result.length; inx++)
		{
			result[inx] = ActionParams.values()[inx].toString();
		}
		super.mandatoryParams = result;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params)
	{
		final String url = params.get(ActionParams.url.toString());

		webDriver.get(url);
		webDriver.manage().window().maximize();

		logger.info("Opened: '" + url + "'");

		return null;
	}
}