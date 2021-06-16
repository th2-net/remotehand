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
import com.exactpro.remotehand.windows.ElementOffsetUtils;
import io.appium.java_client.windows.WindowsDriver;
import org.apache.commons.lang3.tuple.Pair;
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
import java.util.LinkedHashSet;
import java.util.Set;

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

	public Color getElementColor(WindowsDriver<?> driver, ElementOffsetUtils.ElementOffsets elementOffsets) throws ScriptExecuteException {
		try {
			BufferedImage elementImage = getSubImage(driver, elementOffsets.element);
			int width = elementImage.getWidth();
			int height = elementImage.getHeight();

			if (!elementOffsets.hasOffset)
				return new Color(elementImage.getRGB(width / 2, height / 2)); // get the color of the center pixel

			if (elementOffsets.xOffset > width || elementOffsets.yOffset > height)
				throw new ScriptExecuteException("The selected point is outside the bounds of the element");

			return new Color(elementImage.getRGB(elementOffsets.xOffset, elementOffsets.yOffset)); // get the color of the displaced pixel
		} catch (IOException e) {
			throw new ScriptExecuteException("Error while extracting color of element", e);
		}
	}

	public Set<Color> getElementColors(WindowsDriver<?> driver, WebElement element, Pair<Point, Point> coordinates) throws ScriptExecuteException {
		try {
			BufferedImage elementImage = getSubImage(driver, element);
			Set<Color> colors = new LinkedHashSet<>();

			for (int i = coordinates.getLeft().getX(); i < coordinates.getRight().getX(); i++) {
				for (int j = coordinates.getLeft().getY(); j < coordinates.getRight().getY(); j++) {
					colors.add(new Color(elementImage.getRGB(i, j)));
				}
			}

			return colors;
		} catch (IOException e) {
			throw new ScriptExecuteException("Error while extracting color of element", e);
		}
	}

	public String getScreenshotExtension() {
		return screenshotExtension;
	}


	public static String convertToHex(Color elementColor) {
		return String.format("#%02X%02X%02X", elementColor.getRed(), elementColor.getGreen(), elementColor.getBlue());
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
		try {
			BufferedImage elementImage = getSubImage((TakesScreenshot) webDriver, element);
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

	protected <D extends TakesScreenshot> BufferedImage getSubImage(D driver, WebElement element) throws ScriptExecuteException, IOException {
		BufferedImage sourceImage = bytesToImage(driver.getScreenshotAs(OutputType.BYTES));
		Point p = element.getLocation();
		Dimension size = element.getSize();
		int width = getElementScreenshotSize(p.getX(), size.getWidth(), sourceImage.getWidth());
		int height = getElementScreenshotSize(p.getY(), size.getHeight(), sourceImage.getHeight());

		return sourceImage.getSubimage(p.getX(), p.getY(), width, height);
	}
}
