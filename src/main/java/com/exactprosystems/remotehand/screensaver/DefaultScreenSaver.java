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

package com.exactprosystems.remotehand.screensaver;

import com.exactprosystems.remotehand.Configuration;
import com.jhlabs.image.PosterizeFilter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class DefaultScreenSaver extends ScreenSaver<BufferedImage> {

	public DefaultScreenSaver() {
		screenshotExtension = "png";
	}

	@Override
	protected BufferedImage processImage(BufferedImage bufferedImage) {
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		BufferedImage filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		// Use "posterization" method to reduce screenshot file's size
		PosterizeFilter posterizeFilter = new PosterizeFilter();
		posterizeFilter.setNumLevels(10);
		posterizeFilter.filter(bufferedImage, filteredImage);

		return bufferedImage;
	}

	@Override
	protected String saveImage(BufferedImage data, String name) throws IOException {
		Path storageDirPath = Configuration.SCREENSHOTS_DIR_PATH;
		String fileName = createScreenshotFileName(name);
		ImageIO.write(data, screenshotExtension, storageDirPath.resolve(fileName).toFile());
		return fileName;
	}

	@Override
	protected byte[] dataToBytes(BufferedImage image) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		//closing ByteArrayOutputStream is not required
		ImageIO.write(image, screenshotExtension, os);
		return os.toByteArray();
	}
}
