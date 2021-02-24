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

package com.exactprosystems.remotehand.utils;

import com.exactprosystems.remotehand.ScriptExecuteException;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ScreenshotRegionUtils {

	private static final Logger logger = LoggerFactory.getLogger(ScreenshotRegionUtils.class);
	

	private static BufferedImage bytesToImage(byte[] bytes) throws IOException {
		//closing ByteArrayInputStream is not required
		return ImageIO.read(new ByteArrayInputStream(bytes));
	}

	private static byte[] imageToBytes(BufferedImage image) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		//closing ByteArrayOutputStream is not required
		ImageIO.write(image, "jpg", os);
		return os.toByteArray();
	}

	private static int getElementScreenshotSize(int elementPos, int elementSize, int screenSize) throws ScriptExecuteException {
		if (elementPos > screenSize)
			throw new ScriptExecuteException("Element position ("+elementPos+") is outside of screen ("+screenSize+")");

		if (elementPos+elementSize <= screenSize)
			return elementSize;
		else
			return screenSize-elementPos;
	}

	public static byte[] takeElementScreenshot(WebDriver webDriver, WebElement element) throws ScriptExecuteException {
		TakesScreenshot takesScreenshot = (TakesScreenshot)webDriver;
		try {
			BufferedImage fullscreen = bytesToImage(takesScreenshot.getScreenshotAs(OutputType.BYTES));

			Point p = element.getLocation();
			Dimension size = element.getSize();
			int width = getElementScreenshotSize(p.getX(), size.getWidth(), fullscreen.getWidth()),
					height = getElementScreenshotSize(p.getY(), size.getHeight(), fullscreen.getHeight());
			BufferedImage elementImage = fullscreen.getSubimage(p.getX(), p.getY(),
					width, height);
			return imageToBytes(elementImage);
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
