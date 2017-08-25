////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2017, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.exactprosystems.remotehand.Action;
import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.actions.GetScreenshot;
import com.exactprosystems.remotehand.web.webelements.WebLocator;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.exactprosystems.remotehand.RhUtils.isBrowserNotReachable;
import static java.lang.String.format;

public abstract class WebAction extends Action
{
	protected static final String PARAM_WAIT = "wait",
			PARAM_NOTFOUNDFAIL = "notfoundfail";
	
	protected static final SimpleDateFormat SCREENSHOT_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	protected static final String SCREENSHOT_EXTENSION = ".png";
	
	protected String[] mandatoryParams;

	private WebSessionContext context;
	private String sessionIdForLogs;
	private WebLocator webLocator = null;
	private Map<String, String> params = null;

	public void init(WebSessionContext context, WebLocator webLocator, Map<String, String> params) throws ScriptCompileException
	{
		this.context = context;
		this.sessionIdForLogs = '<' + context.getSessionId() + "> ";
		this.webLocator = webLocator;
		this.params = params;
	}
	
	public static int getIntegerParam(Map<String, String> params, String paramName) throws ScriptExecuteException
	{
		try
		{
			return Integer.parseInt(params.get(paramName));
		}
		catch (NumberFormatException ex)
		{
			throw new ScriptExecuteException("Error while parsing parameter '" + paramName + "' = '" + params.get(paramName) + "' as number");
		}
	}
	
	public abstract boolean isNeedLocator();
	public abstract boolean isCanWait();
	public abstract String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException;
	protected abstract Logger getLogger();


	public boolean isCanSwitchPage()
	{
		return false;
	}
	
	public ActionOutputType getOutputType()
	{
		return ActionOutputType.TEXT;
	}
	
	public boolean isElementMandatory()
	{
		return !params.containsKey(PARAM_NOTFOUNDFAIL) || WebScriptCompiler.YES.contains(params.get(PARAM_NOTFOUNDFAIL));
	}
	
	protected boolean waitForElement(WebDriver webDriver, int seconds, final By webLocator) throws ScriptExecuteException
	{
		try
		{
			(new WebDriverWait(webDriver, seconds)).until(new ExpectedCondition<Boolean>()
			{
				@Override
				public Boolean apply(WebDriver driver)
				{
					List<WebElement> elements = driver.findElements(webLocator);

					return elements.size() > 0;
				}
			});
			logInfo("Appeared locator: '%s'", webLocator);
		}
		catch (TimeoutException ex)
		{
			if (isElementMandatory())
				throw new ScriptExecuteException("Timed out after " + seconds + " seconds waiting for '" + webLocator.toString() + "'");
			else
				return false;
		}
		return true;
	}
	
	@Override
	public String execute() throws ScriptExecuteException
	{
		try
		{
			WebDriver webDriver = context.getWebDriver();
			
			By locator = null;
			if (webLocator != null)
				locator = webLocator.getWebLocator(webDriver, params);

			boolean needRun = true;
			if (isCanWait())
			{
				if ((params.containsKey(PARAM_WAIT)) && (!params.get(PARAM_WAIT).isEmpty()))
					if (!waitForElement(webDriver, getIntegerParam(params, PARAM_WAIT), locator))
						needRun = false;
			}

			if (isCanSwitchPage())
				disableLeavePageAlert(webDriver);

			return (needRun) ? run(webDriver, locator, params) : null;
		}
		catch (ScriptExecuteException e)
		{
			throw addScreenshot(e);
		}
		catch (WebDriverException e)
		{
			ScriptExecuteException see = new ScriptExecuteException(e.getMessage(), e);
			if (!isBrowserNotReachable(e))
				see = addScreenshot(see);
			throw see;
		}
	}
	
	private ScriptExecuteException addScreenshot(ScriptExecuteException see)
	{
		if (this instanceof GetScreenshot)
			return see;
		see.setScreenshotId(takeScreenshotIfError());
		return see;
	}

	public String[] getMandatoryParams() throws ScriptCompileException
	{
		return mandatoryParams;
	}

	public void disableLeavePageAlert(WebDriver webDriver)
	{
		((JavascriptExecutor)webDriver).executeScript("window.onbeforeunload = function(e){};");
	}


	public WebLocator getWebLocator() {
		return webLocator;
	}


	public Map<String, String> getParams() {
		return params;
	}
	
	protected WebElement findElement(WebDriver webDriver, By webLocator)
	{
		WebElement element = webDriver.findElement(webLocator);
		if (!element.isDisplayed())
			scrollTo(element, webLocator);
		return element;
	}
	
	protected void scrollTo(WebElement element, By webLocator)
	{
		if (element instanceof Locatable)
		{
			((Locatable)element).getCoordinates().inViewPort();
			logInfo("Scrolled to %s.", webLocator);
		}
		else 
			logWarn("Cannot scroll %s.", webLocator);
	}
	
	protected String takeScreenshot(String name) throws ScriptExecuteException
	{
		WebDriver webDriver = context.getWebDriver();
		if (!(webDriver instanceof TakesScreenshot))
			throw new ScriptExecuteException("Current driver doesn't support taking screenshots.");
		TakesScreenshot takesScreenshot = (TakesScreenshot) webDriver;
		
		Path storageDirPath = Paths.get(WebConfiguration.SCREENSHOTS_DIR_NAME);
		createScreenshotsDirIfNeeded(storageDirPath);

		File tmpFile;
		try
		{
			tmpFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
		}
		catch (WebDriverException wde)
		{
			throw new ScriptExecuteException("Unable to create screenshot: " + wde.getMessage(), wde);
		}
		catch (RuntimeException e)
		{
			String msg = "Unexpected error while trying to create screenshot";
			logError(msg, e);
			throw new ScriptExecuteException(msg);
		}
		
		String fileName = createScreenshotFileName(name);
		saveScreenshot(tmpFile, storageDirPath.resolve(fileName));
		
		return fileName;
	}
	
	private void createScreenshotsDirIfNeeded(Path relativeDirPath) throws ScriptExecuteException
	{
		if (Files.notExists(relativeDirPath))
		{
			try
			{
				Files.createDirectory(relativeDirPath);
			}
			catch (IOException e)
			{
				throw new ScriptExecuteException("Unable to create directory to store screenshots.", e);
			}
		}
	}
	
	private String createScreenshotFileName(String name)
	{
		if (name != null)
		{
			if (name.endsWith(SCREENSHOT_EXTENSION))
				return name;
			else 
				return name + SCREENSHOT_EXTENSION;
		}
		else 
			return "screenshot" + SCREENSHOT_TIMESTAMP_FORMAT.format(new Date()) + SCREENSHOT_EXTENSION;
	}
	
	private void saveScreenshot(File tmpFile, Path targetPath) throws ScriptExecuteException
	{
		try
		{
			FileUtils.copyFile(tmpFile, targetPath.toFile());
			logInfo("Screenshot %s has been successfully saved.", targetPath);
		}
		catch (IOException e)
		{
			throw new ScriptExecuteException(format("Unable to copy screenshot file '%s' to the storage directory '%s'.",
					tmpFile, WebConfiguration.SCREENSHOTS_DIR_NAME));
		}
	}
	
	private String takeScreenshotIfError()
	{
		try
		{
			return takeScreenshot(null);
		}
		catch (ScriptExecuteException e)
		{
			logError("Unable to create screenshot.", e);
			return null;
		}
	}
	
	protected void logError(String msg)
	{
		getLogger().error(sessionIdForLogs + msg);
	}
	
	protected void logError(String msg, Throwable e)
	{
		getLogger().error(sessionIdForLogs + msg, e);
	}
	
	protected void logWarn(String msg)
	{
		getLogger().warn(msg);
	}
	
	protected void logWarn(String msgTemplate, Object... args)
	{
		getLogger().warn(sessionIdForLogs + format(msgTemplate, args));
	}
	
	protected void logInfo(String msg)
	{
		Logger logger = getLogger();
		if (logger.isInfoEnabled())
			logger.info(sessionIdForLogs + msg);
	}
	
	protected void logInfo(String msgTemplate, Object... args)
	{
		Logger logger = getLogger();
		if (logger.isInfoEnabled())
			logger.info(sessionIdForLogs + format(msgTemplate, args));
	}
}
