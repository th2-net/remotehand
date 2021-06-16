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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
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

		WindowsDriver<?> driver = driverWrapper.getDriver();
		ElementSearcher es = new ElementSearcher(params, driver, cachedElements);
		WebElement element = es.searchElement();
		if (element == null)
			throw new ScriptExecuteException("Error while extracting color of element. Element not found");

		Pair<Point, Point> coordinates;
		if (isCoordinatesEmpty(params)) {
			coordinates = getDefaultCoordinates(element);
		} else {
			ElementOffsetUtils.ElementOffsets startOffset = getOffset(element, params.get(START_X_OFFSET), params.get(STAR_Y_OFFSET));
			ElementOffsetUtils.ElementOffsets endOffset = getOffset(element, params.get(END_X_OFFSET), params.get(END_Y_OFFSET));
			coordinates = correctCoordinates(startOffset, endOffset);
		}

		return screenWriter.getElementColors(driver, element, coordinates).stream()
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

	private Pair<Point, Point> correctCoordinates(ElementOffsetUtils.ElementOffsets firstOffset,
	                                              ElementOffsetUtils.ElementOffsets secondOffset) {
		Point firstPoint = firstOffset.xOffset > secondOffset.xOffset
				? new Point(secondOffset.xOffset, firstOffset.xOffset)
				: new Point(firstOffset.xOffset, secondOffset.xOffset);

		Point secondPoint = firstOffset.yOffset > secondOffset.yOffset
				? new Point(secondOffset.yOffset, firstOffset.yOffset)
				: new Point(firstOffset.yOffset, secondOffset.yOffset);

		return new ImmutablePair<>(firstPoint, secondPoint);
	}

	private boolean isCoordinatesEmpty(Map<String, String> params) {
		return StringUtils.isEmpty(params.get(START_X_OFFSET)) || StringUtils.isEmpty(params.get(END_X_OFFSET))
				|| StringUtils.isEmpty(params.get(STAR_Y_OFFSET)) || StringUtils.isEmpty(params.get(END_Y_OFFSET));
	}

	private Pair<Point, Point> getDefaultCoordinates(WebElement element) {
		Point firstPoint = new Point(0, 0);
		Dimension size = element.getSize();
		Point secondPoint = new Point(size.getWidth(), size.getHeight());

		return new ImmutablePair<>(firstPoint, secondPoint);
	}
}
