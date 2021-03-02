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
import com.exactprosystems.remotehand.web.WebConfiguration;
import com.jhlabs.image.PosterizeFilter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotUtils {

	private static final Logger logger = LoggerFactory.getLogger(ScreenshotUtils.class);

	protected static final String SCREENSHOT_EXTENSION = ".png";
	protected static final DateTimeFormatter SCREENSHOT_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
	

	public static String takeAndSaveScreenshot(String name, TakesScreenshot takesScreenshot) throws ScriptExecuteException {
		Path storageDirPath = Paths.get(WebConfiguration.SCREENSHOTS_DIR_NAME);
		createScreenshotsDirIfNeeded(storageDirPath);
		try {
			byte[] byteArr = takesScreenshot.getScreenshotAs(OutputType.BYTES);
			// Use "posterization" method to reduce screenshot file's size
			BufferedImage sourceImage = ImageIO.read(new ByteArrayInputStream(byteArr));
			BufferedImage filteredImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_RGB);
			PosterizeFilter posterizeFilter = new PosterizeFilter();
			posterizeFilter.setNumLevels(10);
			posterizeFilter.filter(sourceImage, filteredImage);

			String fileName = createScreenshotFileName(name);
			ImageIO.write(filteredImage, "png", storageDirPath.resolve(fileName).toFile());
			
			return fileName;
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

	private static void createScreenshotsDirIfNeeded(Path relativeDirPath) throws ScriptExecuteException {
		if (Files.notExists(relativeDirPath)) {
			try {
				Files.createDirectory(relativeDirPath);
			} catch (IOException e) {
				throw new ScriptExecuteException("Unable to create directory to store screenshots.", e);
			}
		}
	}

	private static String createScreenshotFileName(String name) {
		return SCREENSHOT_TIMESTAMP_FORMAT.format(LocalDateTime.now()) + "_" + (name != null ? name : "screenshot") + SCREENSHOT_EXTENSION;
	}	
}
