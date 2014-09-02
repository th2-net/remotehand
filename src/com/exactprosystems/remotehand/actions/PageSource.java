package com.exactprosystems.remotehand.actions;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.WebAction;

public class PageSource extends WebAction
{
	public PageSource()
	{
		super.needLocator = false;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		StringBuilder sb = new StringBuilder("URL: " + webDriver.getCurrentUrl() + "\r\n");
		sb.append("Title: " + webDriver.getTitle() + "\r\n");
		sb.append("Source: \r\n" + webDriver.getPageSource());
		return sb.toString();
	}
}
