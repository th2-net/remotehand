/*
 * Copyright 2020-2021 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exactpro.remotehand.screenwriter;

import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.web.WebConfiguration;
import io.appium.java_client.windows.WindowsDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class ScreenWriter<T> {
	private static final Logger logger = LoggerFactory.getLogger(ScreenWriter.class);

	protected String screenshotExtension;
	protected static final DateTimeFormatter SCREENSHOT_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");


	public String takeAndSaveScreenshot(String name, TakesScreenshot takesScreenshot) throws ScriptExecuteException {
		Path storageDirPath = WebConfiguration.SCREENSHOTS_DIR_PATH;
		createScreenshotsDirIfNeeded(storageDirPath);
		try {
			BufferedImage sourceImage = bytesToImage(takesScreenshot.getScreenshotAs(OutputType.BYTES));
			T processedData = processImage(sourceImage);
			return saveImage(processedData, name);
		} catch (WebDriverException wde) {
			throw new ScriptExecuteException("Unable to create screenshot", wde);
		} catch (IOException e) {
			throw new ScriptExecuteException("Couldn't apply 'posterization' effect for made screenshot", e);
		} catch (RuntimeException e) {
			String msg = "Unexpected error while trying to create screenshot";
			logger.error(msg, e);
			throw new ScriptExecuteException(msg, e);
		}
	}

	public String takeAndSaveElementScreenshot(String name, WebDriver webDriver, WebElement element) throws ScriptExecuteException {
		T screenshotData = takeAndGetElementScreenshotData(webDriver, element);
		try {
			return saveImage(screenshotData, name);
		} catch (IOException e) {
			throw new ScriptExecuteException("Error while processing screenshot of element", e);
		}
	}

	public byte[] takeElementScreenshot(WebDriver webDriver, WebElement element) throws ScriptExecuteException {
		T screenshotData = takeAndGetElementScreenshotData(webDriver, element);
		try {
			return dataToBytes(screenshotData);
		} catch (IOException e) {
			throw new ScriptExecuteException("Error while processing screenshot of element", e);
		}
	}

	public Color getElementColor(WindowsDriver<?> driver, WebElement element) throws ScriptExecuteException {
		return getElementColor(driver, element, null);
	}

	public Color getElementColor(WindowsDriver<?> driver, WebElement element, java.awt.Point displacedPoint) throws ScriptExecuteException {
		try {
			BufferedImage sourceImage = bytesToImage(driver.getScreenshotAs(OutputType.BYTES));
			Point p = element.getLocation();
			Dimension size = element.getSize();
			int width = getElementScreenshotSize(p.getX(), size.getWidth(), sourceImage.getWidth());
			int height = getElementScreenshotSize(p.getY(), size.getHeight(), sourceImage.getHeight());
			BufferedImage elementImage = sourceImage.getSubimage(p.getX(), p.getY(), width, height);

			if (displacedPoint == null)
				return new Color(elementImage.getRGB(width / 2, height / 2)); // get the color of the center pixel

			if (displacedPoint.x > width || displacedPoint.y > height)
				throw new ScriptExecuteException("The selected point is outside the bounds of the element");

			return new Color(elementImage.getRGB(displacedPoint.x, displacedPoint.y)); // get the color of the displaced pixel
		} catch (IOException e) {
			throw new ScriptExecuteException("Error while extracting color of element", e);
		}
	}

	public String getScreenshotExtension() {
		return screenshotExtension;
	}

	protected abstract T processImage(BufferedImage bufferedImage) throws IOException;
	protected abstract String saveImage(T data, String name) throws IOException;
	protected abstract byte[] dataToBytes(T data) throws IOException;

	protected BufferedImage bytesToImage(byte[] data) throws IOException {
		return ImageIO.read(new ByteArrayInputStream(data));
	}

	protected boolean hasAlphaChannel(RenderedImage renderedImage) {
		return renderedImage.getColorModel().hasAlpha();
	}

	protected void createScreenshotsDirIfNeeded(Path relativeDirPath) throws ScriptExecuteException {
		if (Files.notExists(relativeDirPath)) {
			try {
				Files.createDirectory(relativeDirPath);
			} catch (IOException e) {
				throw new ScriptExecuteException("Unable to create directory to store screenshots.", e);
			}
		}
	}

	protected String createScreenshotFileName(String name) {
		return SCREENSHOT_TIMESTAMP_FORMAT.format(LocalDateTime.now()) + "_" + (name != null ? name : "screenshot")
				+ "." + screenshotExtension;
	}

	protected int getElementScreenshotSize(int elementPos, int elementSize, int screenSize) throws ScriptExecuteException {
		if (elementPos > screenSize)
			throw new ScriptExecuteException("Element position (" + elementPos + ") is outside of screen (" + screenSize + ")");

		if (elementPos + elementSize <= screenSize)
			return elementSize;
		else
			return screenSize - elementPos;
	}

	protected T takeAndGetElementScreenshotData(WebDriver webDriver, WebElement element) throws ScriptExecuteException {
		TakesScreenshot takesScreenshot = (TakesScreenshot)webDriver;
		try {
			BufferedImage fullscreen = bytesToImage(takesScreenshot.getScreenshotAs(OutputType.BYTES));
			Point p = element.getLocation();
			Dimension size = element.getSize();
			int width = getElementScreenshotSize(p.getX(), size.getWidth(), fullscreen.getWidth()),
					height = getElementScreenshotSize(p.getY(), size.getHeight(), fullscreen.getHeight());
			BufferedImage elementImage = fullscreen.getSubimage(p.getX(), p.getY(),
					width, height);
			return processImage(elementImage);
		} catch (WebDriverException wde) {
			throw new ScriptExecuteException("Unable to create screenshot of element: " + wde.getMessage(), wde);
		} catch (IOException e) {
			throw new ScriptExecuteException("Error while processing screenshot of element", e);
		} catch (RuntimeException e) {
			String msg = "Unexpected error while trying to create screenshot of element";
			logger.error(msg, e);
			throw new ScriptExecuteException(msg, e);
		}
	}
}
