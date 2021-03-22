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

package com.exactprosystems.remotehand.screenwriter;

import com.exactprosystems.remotehand.Configuration;
import com.sun.jna.Native;
import com.sun.jna.ptr.PointerByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class WebpScreenWriter extends ScreenWriter<byte[]> {
	private static final Logger logger = LoggerFactory.getLogger(WebpScreenWriter.class);

	private final float qualityFactor;
	private final boolean losslessCompression;

	static {
		loadLibrary();
	}

	public WebpScreenWriter(float qualityFactor, boolean losslessCompression) {
		screenshotExtension = "webp";
		this.qualityFactor = qualityFactor;
		this.losslessCompression = losslessCompression;
	}

	public int getEncoderVersion() {
		return WebPGetEncoderVersion();
	}


	@Override
	protected byte[] processImage(BufferedImage bufferedImage) throws IOException {
		PointerByReference outputPointer = new PointerByReference();
		int size;
		if (hasAlphaChannel(bufferedImage)) {
			byte[] rgbaData = getRGBAData(bufferedImage);
			if (losslessCompression) {
				size = WebPEncodeLosslessRGBA(rgbaData, bufferedImage.getWidth(), bufferedImage.getHeight(),
						bufferedImage.getWidth() * 4, qualityFactor, outputPointer);
			} else {
				size = WebPEncodeRGBA(rgbaData, bufferedImage.getWidth(), bufferedImage.getHeight(),
						bufferedImage.getWidth() * 4, qualityFactor, outputPointer);
			}
		} else {
			byte[] rgbData = getRGBData(bufferedImage);
			if (losslessCompression) {
				size = WebPEncodeLosslessRGB(rgbData, bufferedImage.getWidth(), bufferedImage.getHeight(),
						bufferedImage.getWidth() * 3, qualityFactor, outputPointer);
			} else {
				size = WebPEncodeRGB(rgbData, bufferedImage.getWidth(), bufferedImage.getHeight(),
						bufferedImage.getWidth() * 3, qualityFactor, outputPointer);
			}
		}

		return outputPointer.getValue().getByteArray(0, size);
	}

	@Override
	protected String saveImage(byte[] data, String name) throws IOException {
		Path storageDirPath = Configuration.SCREENSHOTS_DIR_PATH;
		String fileName = createScreenshotFileName(name);
		try (FileOutputStream os = new FileOutputStream(storageDirPath.resolve(fileName).toFile())) {
			os.write(data);
		}

		return fileName;
	}

	@Override
	protected byte[] dataToBytes(byte[] data) throws IOException {
		return data;
	}

	protected byte[] getRGBAData(BufferedImage bufferedImage) {
		byte[] result = new byte[bufferedImage.getWidth() * bufferedImage.getHeight() * 4];
		int offset = 0;
		for (int i = 0; i < bufferedImage.getHeight(); i++) {
			for (int j = 0; j < bufferedImage.getWidth(); j++) {
				result[offset++] = (byte) ((bufferedImage.getRGB(j, i) >> 16) & 0xff);
				result[offset++] = (byte) ((bufferedImage.getRGB(j, i) >> 8) & 0xff);
				result[offset++] = (byte) (bufferedImage.getRGB(j, i) & 0xff);
				result[offset++] = (byte) ((bufferedImage.getRGB(j, i) >> 24) & 0xff);
			}
		}

		return result;
	}

	protected byte[] getRGBData(BufferedImage bufferedImage) {
		byte[] result = new byte[bufferedImage.getWidth() * bufferedImage.getHeight() * 3];
		int offset = 0;
		for (int i = 0; i < bufferedImage.getHeight(); i++) {
			for (int j = 0; j < bufferedImage.getWidth(); j++) {
				result[offset++] = (byte) ((bufferedImage.getRGB(j, i) >> 16) & 0xff);
				result[offset++] = (byte) ((bufferedImage.getRGB(j, i) >> 8) & 0xff);
				result[offset++] = (byte) (bufferedImage.getRGB(j, i) & 0xff);
			}
		}

		return result;
	}


	private native int WebPGetEncoderVersion();
	private native int WebPEncodeRGB(byte[] rgb, int width, int height, int stride, float qualityFactor, PointerByReference outputPointer);
	private native int WebPEncodeLosslessRGB(byte[] rgb, int width, int height, int stride, float qualityFactor, PointerByReference outputPointer);
	private native int WebPEncodeRGBA(byte[] rgbaData, int width, int height, int stride, float qualityFactor, PointerByReference outputPointer);
	private native int WebPEncodeLosslessRGBA(byte[] rgbaData, int width, int height, int stride, float qualityFactor, PointerByReference outputPointer);


	private static void loadLibrary() {
		File library = new File(Configuration.getInstance().getWebpLibraryPath());
		if (!library.exists()) {
			logger.error("WebP librarys cannot be initialized");
			throw new RuntimeException("WebP library is not exist");
		}
		Native.register(library.getAbsolutePath());
		logger.info("WebP library successfully initialized");
	}
}
