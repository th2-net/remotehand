/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web;

import com.exactprosystems.remotehand.Action;
import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.RhUtils;
import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.webelements.WebLocator;
import com.jhlabs.image.PosterizeFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.exactprosystems.remotehand.RhUtils.isBrowserNotReachable;

public abstract class WebAction extends Action
{
	protected static final String PARAM_WAIT = "wait", PARAM_NOTFOUNDFAIL = "notfoundfail";
	
	protected static final String SCREENSHOT_EXTENSION = ".png";
	protected static final DateTimeFormatter SCREENSHOT_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
	
	protected String[] mandatoryParams;

	protected WebSessionContext context;
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
		catch (NumberFormatException e)
		{
			throw new ScriptExecuteException(String.format("Error while parsing parameter '%s' = '%s' as number",
					paramName, params.get(paramName)), e);
		}
	}

	public static int getIntegerParam(Map<String, String> params, String paramName, int defaultValue)
	{
		try
		{
			return Integer.parseInt(params.get(paramName));
		}
		catch (NumberFormatException e)
		{
			return defaultValue;
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
		return !params.containsKey(PARAM_NOTFOUNDFAIL) || RhUtils.YES.contains(params.get(PARAM_NOTFOUNDFAIL));
	}
	
	protected boolean waitForElement(WebDriver webDriver, int seconds, By webLocator) throws ScriptExecuteException
	{
		return waitForElement(webDriver, seconds, webLocator, isElementMandatory());
	}
	
	protected boolean waitForElement(WebDriver webDriver, int seconds, By webLocator, boolean isElementMandatory)
			throws ScriptExecuteException
	{
		try
		{
			new WebDriverWait(webDriver, seconds).until((ExpectedCondition<Boolean>) webDriver1 ->
					webDriver1 != null && webDriver1.findElements(webLocator).size() > 0);
			logInfo("Appeared locator: '%s'", webLocator);
		}
		catch (TimeoutException ex)
		{
			if (isElementMandatory)
				throw new ScriptExecuteException(String.format("Timed out after %s seconds waiting for '%s'",
						seconds, webLocator), ex);
			return false;
		}
		return true;
	}
	
	@Override
	public void beforeExecute()
	{
		if (context == null || context.getContextData() == null || context.getContextData().isEmpty())
		{
			if (context == null)
				getLogger().warn("Context is null");
			else if (context.getContextData() == null)
				getLogger().warn("ContextData is null");
			return;
		}

		for (Map.Entry<String, String> param : params.entrySet())
		{
			int start;
			String value = param.getValue();
			if((start = value.indexOf("@{")) < 0)
				continue;

			int end = value.lastIndexOf('}');
			String name = value.substring(start + 2, end);
			String contextValue = (String) context.getContextData().get(name);

			if(StringUtils.isNotEmpty(contextValue))
				value = value.substring(0, start) + contextValue + value.substring(end + 1);

			param.setValue(value);
		}
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
				int waitSecs = getIntegerParam(params, PARAM_WAIT, 0);
				if (waitSecs != 0 && !waitForElement(webDriver, waitSecs, locator))
					needRun = false;
			}

			if (isCanSwitchPage() && isNeedDisableLeavePageAlert())
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
			if (isBrowserNotReachable(e))
				throw see;
			throw addScreenshot(see);
		}
	}
	
	protected ScriptExecuteException addScreenshot(ScriptExecuteException see)
	{
		see.setScreenshotId(takeScreenshotIfError());
		return see;
	}

	public String[] getMandatoryParams() throws ScriptCompileException
	{
		return mandatoryParams;
	}
	
	public boolean isNeedDisableLeavePageAlert()
	{
		return ((WebConfiguration) Configuration.getInstance()).isDisableLeavePageAlert();
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
		try
		{
			File tmpFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
			// Use "posterization" method to reduce screenshot file's size
			BufferedImage sourceImage = ImageIO.read(tmpFile),
					filteredImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_RGB);
			PosterizeFilter posterizeFilter = new PosterizeFilter();
			posterizeFilter.setNumLevels(10);
			posterizeFilter.filter(sourceImage, filteredImage);
			ImageIO.write(filteredImage, "png", tmpFile);
			
			String fileName = createScreenshotFileName(name);
			saveScreenshot(tmpFile, storageDirPath.resolve(fileName));
			return fileName;
		}
		catch (WebDriverException wde)
		{
			throw new ScriptExecuteException("Unable to create screenshot: " + wde.getMessage(), wde);
		}
		catch (IOException e)
		{
			throw new ScriptExecuteException("Couldn't apply 'posterization' effect for made screenshot", e);
		}
		catch (RuntimeException e)
		{
			String msg = "Unexpected error while trying to create screenshot";
			logError(msg, e);
			throw new ScriptExecuteException(msg, e);
		}
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
		return (name != null ? name : "screenshot") + SCREENSHOT_TIMESTAMP_FORMAT.format(LocalDateTime.now()) + SCREENSHOT_EXTENSION;
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
			throw new ScriptExecuteException(String.format("Unable to copy screenshot file '%s' to the storage directory '%s'.",
					tmpFile, WebConfiguration.SCREENSHOTS_DIR_NAME), e);
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
	
	
	private BufferedImage bytesToImage(byte[] bytes) throws IOException
	{
		try (InputStream is = new ByteArrayInputStream(bytes))
		{
			return ImageIO.read(is);
		}
	}
	
	private byte[] imageToBytes(BufferedImage image) throws IOException
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try
		{
			ImageIO.write(image, "jpg", os);
		}
		finally
		{
			os.close();
		}
		return os.toByteArray();
	}
	
	private int getElementScreenshotSize(int elementPos, int elementSize, int screenSize) throws ScriptExecuteException
	{
		if (elementPos > screenSize)
			throw new ScriptExecuteException("Element position ("+elementPos+") is outside of screen ("+screenSize+")");
		
		if (elementPos+elementSize <= screenSize)
			return elementSize;
		else
			return screenSize-elementPos;
	}
	
	protected byte[] takeElementScreenshot(WebDriver webDriver, WebElement element) throws ScriptExecuteException
	{
		TakesScreenshot takesScreenshot = (TakesScreenshot)webDriver;
		try
		{
			BufferedImage fullscreen = bytesToImage(takesScreenshot.getScreenshotAs(OutputType.BYTES));
			
			Point p = element.getLocation();
			Dimension size = element.getSize();
			int width = getElementScreenshotSize(p.getX(), size.getWidth(), fullscreen.getWidth()),
					height = getElementScreenshotSize(p.getY(), size.getHeight(), fullscreen.getHeight());
			BufferedImage elementImage = fullscreen.getSubimage(p.getX(), p.getY(), 
					width, height);
			return imageToBytes(elementImage);
		}
		catch (WebDriverException wde)
		{
			throw new ScriptExecuteException("Unable to create screenshot of element: " + wde.getMessage(), wde);
		}
		catch (IOException e)
		{
			throw new ScriptExecuteException("Error while processing screenshot of element", e);
		}
		catch (RuntimeException e)
		{
			String msg = "Unexpected error while trying to create screenshot of element";
			logError(msg, e);
			throw new ScriptExecuteException(msg, e);
		}
	}
	
	protected byte[] takeElementScreenshot(WebDriver webDriver, By webLocator) throws ScriptExecuteException
	{
		WebElement element = findElement(webDriver, webLocator);
		return takeElementScreenshot(webDriver, element);
	}
	
	
	protected void logError(String msg)
	{
		getLogger().error("{}{}", sessionIdForLogs, msg);
	}
	
	protected void logError(String msg, Throwable e)
	{
		getLogger().error(String.format("%s%s", sessionIdForLogs, msg), e);
	}
	
	protected void logWarn(String msg)
	{
		getLogger().warn(msg);
	}
	
	protected void logWarn(String msgTemplate, Object... args)
	{
		getLogger().warn("{}{}", sessionIdForLogs, String.format(msgTemplate, args));
	}
	
	protected void logInfo(String msg)
	{
		Logger logger = getLogger();
		if (logger.isInfoEnabled())
			logger.info("{}{}", sessionIdForLogs, msg);
	}
	
	protected void logInfo(String msgTemplate, Object... args)
	{
		Logger logger = getLogger();
		if (logger.isInfoEnabled())
			logger.info("{}{}", sessionIdForLogs, String.format(msgTemplate, args));
	}
}