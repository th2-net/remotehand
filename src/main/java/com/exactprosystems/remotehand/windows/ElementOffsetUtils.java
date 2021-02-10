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

package com.exactprosystems.remotehand.windows;

import com.exactprosystems.remotehand.ScriptExecuteException;
import org.apache.commons.lang3.StringUtils;
import org.mvel2.MVEL;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ElementOffsetUtils {
	
	public static ElementOffsets calculateOffset(ElementOffsetParams params) throws ScriptExecuteException {
		boolean emptyX = StringUtils.isEmpty(params.xOffset);
		boolean emptyY = StringUtils.isEmpty(params.yOffset);
		if (emptyX || emptyY) {
			if (emptyX && emptyY) {
				return new ElementOffsets(params.element);
			} else {
				throw new ScriptExecuteException("Both offsets should be present");
			}
		}
		
		if (isStringSimple(params.xOffset) && isStringSimple(params.yOffset)) {
			return new ElementOffsets(params.element, Integer.parseInt(params.xOffset), Integer.parseInt(params.yOffset));
		}

		Dimension rect = params.element.getSize();
		Map<String, Object> vars = new HashMap<>();
		vars.put("height", rect.getHeight());
		vars.put("width", rect.getWidth());

		Integer calculatedXOffset, calculatedYOffset;
		try {
			calculatedXOffset = MVEL.eval(params.xOffset, vars, Integer.class);
			Objects.requireNonNull(calculatedXOffset, "Evaluation didn't return correspond value");
		} catch (Exception e) {
			throw new ScriptExecuteException("Error calculating X-Offset: " + params.xOffset, e);
		}

		try {
			calculatedYOffset = MVEL.eval(params.yOffset, vars, Integer.class);
			Objects.requireNonNull(calculatedYOffset, "Evaluation didn't return correspond value");
		} catch (Exception e) {
			throw new ScriptExecuteException("Error calculating Y-Offset: " + params.yOffset, e);
		}

		return new ElementOffsets(params.element, calculatedXOffset, calculatedYOffset);
	}
	
	private static boolean isStringSimple (String s) {
		int strLength = s.length();
		int startInd = s.length() > 1 && s.charAt(0) == '-' ? 1 : 0;
		for (; startInd < strLength; ++startInd) {
			if (!Character.isDigit(s.charAt(startInd)))
				return false;
		}
		return true;
	}

	public static class ElementOffsetParams {
		private final WebElement element;
		private final String xOffset;
		private final String yOffset;

		public ElementOffsetParams(WebElement element, String xOffset, String yOffset) {
			this.element = element;
			this.xOffset = xOffset;
			this.yOffset = yOffset;
		}
	}

	public static class ElementOffsets {
		public final WebElement element;
		public final boolean hasOffset;
		public final int xOffset;
		public final int yOffset;

		public ElementOffsets(WebElement element) {
			this.element = element;
			this.hasOffset = false;
			this.xOffset = this.yOffset = 0;
		}

		public ElementOffsets(WebElement element, int xOffset, int yOffset) {
			this.element = element;
			this.hasOffset = true;
			this.xOffset = xOffset;
			this.yOffset = yOffset;
		}
	}
}
