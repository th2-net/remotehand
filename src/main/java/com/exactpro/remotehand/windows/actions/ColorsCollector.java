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

package com.exactpro.remotehand.windows.actions;

import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.screenwriter.ScreenWriter;
import com.exactpro.remotehand.windows.*;
import io.appium.java_client.windows.WindowsDriver;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Action collects and returns all the unique colors of an element
 */
public class ColorsCollector extends WindowsAction {
	private static final Logger loggerInstance = LoggerFactory.getLogger(ColorsCollector.class);

	public static final String START_X_OFFSET = "startxoffset", STAR_Y_OFFSET = "startyoffset",
			END_X_OFFSET = "endxoffset", END_Y_OFFSET = "endyoffset";


	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params,
	                  WindowsSessionContext.CachedWebElements cachedElements) throws ScriptExecuteException {
		WindowsDriver<?> driver = getDriver(driverWrapper);
		ElementSearcher es = new ElementSearcher(params, driver, cachedElements);
		WebElement element = es.searchElement();
		if (element == null)
			throw new ScriptExecuteException("Error while extracting color of element. Element not found");

		Rectangle rectangle;
		if (isCoordinatesEmpty(params)) {
			rectangle = getDefaultRectangle(element);
		} else {
			ElementOffsetUtils.ElementOffsets startOffset = getOffset(element, params.get(START_X_OFFSET), params.get(STAR_Y_OFFSET));
			ElementOffsetUtils.ElementOffsets endOffset = getOffset(element, params.get(END_X_OFFSET), params.get(END_Y_OFFSET));
			rectangle = getRectangleFromOffsets(startOffset, endOffset);
		}

		return screenWriter.getElementColors(driver, element, rectangle).stream()
				.map(ScreenWriter::convertToHex)
				.collect(Collectors.joining(";"));
	}

	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}


	private ElementOffsetUtils.ElementOffsets getOffset(WebElement element, String xOffset, String yOffset) throws ScriptExecuteException {
		ElementOffsetUtils.ElementOffsetParams position
				= new ElementOffsetUtils.ElementOffsetParams(element, xOffset, yOffset);
		return ElementOffsetUtils.calculateOffset(position);
	}

	private Rectangle getRectangleFromOffsets(ElementOffsetUtils.ElementOffsets firstOffset,
	                                          ElementOffsetUtils.ElementOffsets secondOffset) throws ScriptExecuteException {
		int startXCoordinate, endXCoordinate;
		if (firstOffset.xOffset > secondOffset.xOffset) {
			startXCoordinate = secondOffset.xOffset;
			endXCoordinate = firstOffset.xOffset;
		} else {
			startXCoordinate = firstOffset.xOffset;
			endXCoordinate = secondOffset.xOffset;
		}

		int startYCoordinate, endYCoordinate;
		if (firstOffset.yOffset > secondOffset.yOffset) {
			startYCoordinate = secondOffset.yOffset;
			endYCoordinate = firstOffset.yOffset;
		} else {
			startYCoordinate = firstOffset.yOffset;
			endYCoordinate = secondOffset.yOffset;
		}

		if (startXCoordinate != firstOffset.xOffset || startYCoordinate != firstOffset.yOffset)
			loggerInstance.info("Coordinates are swapped");

		if (startXCoordinate < 0 || startYCoordinate < 0)
			throw new ScriptExecuteException("Coordinates cannot be less than zero");

		return new Rectangle(startXCoordinate, startYCoordinate, endYCoordinate, endXCoordinate);
	}

	private boolean isCoordinatesEmpty(Map<String, String> params) {
		return StringUtils.isEmpty(params.get(START_X_OFFSET)) || StringUtils.isEmpty(params.get(END_X_OFFSET))
				|| StringUtils.isEmpty(params.get(STAR_Y_OFFSET)) || StringUtils.isEmpty(params.get(END_Y_OFFSET));
	}

	private Rectangle getDefaultRectangle(WebElement element) {
		Dimension size = element.getSize();
		return new Rectangle(0, 0, size.getHeight(), size.getWidth());
	}
}
