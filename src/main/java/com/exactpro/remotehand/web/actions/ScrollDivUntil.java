/*
 * Copyright 2020-2024 Exactpro (Exactpro Systems Limited)
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

package com.exactpro.remotehand.web.actions;

import com.exactpro.remotehand.utils.RhUtils;
import com.exactpro.remotehand.ScriptExecuteException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Map;

public class ScrollDivUntil extends ScrollDivTo {
	private static final String PARAM_SCROLL_DIR = "searchdir";
	private static final String PARAM_SEARCH_OFFSET = "searchoffset";
	private static final String PARAM_DO_SCROLL_TO = "doscrollto";
	private static final int DEFAULT_SCROLL_OFFSET = 100;

	@Override
	protected By getWebLocator2(WebDriver webDriver, Map<String, String> params) throws ScriptExecuteException {
		logger.info("Element with scrollbar: {}", divWithScrollbar);
		return doScrollSearch(webDriver, params);
	}
	
	@Override
	protected String doScrollTo(WebDriver webDriver, By webLocator, By webLocator2, Map<String, String> params) {
		if (!needDoScrollTo(params))
			return null;
		logger.info("Scrolling element '{}' to element '{}'", webLocator, webLocator2);
		return super.doScrollTo(webDriver, webLocator, webLocator2, params);
	}

	private By doScrollSearch(WebDriver webDriver, Map<String, String> params) throws ScriptExecuteException {
		long prevScrollPos = getScrollPosition(divWithScrollbar);
		int scrollOffset = getIntegerParam(params, PARAM_SEARCH_OFFSET, DEFAULT_SCROLL_OFFSET);
		int totalScroll = 0;
		Direction dir = getDirection(params);
		boolean isBothDir = dir.equals(Direction.both);
		if (isBothDir)
			dir = Direction.down;
		
		By webLocator2 = super.getWebLocator2(webDriver, params);
		while (webLocator2 == null) {
			logger.info("Requested element didn't appear, scrolling by another {} in {} direction", scrollOffset, dir);
			switch (dir) {
				case down:
					scrollDivByOffset(divWithScrollbar, scrollOffset);
					totalScroll += scrollOffset;
					break;
				case up:
					scrollDivByOffset(divWithScrollbar, -scrollOffset);
					totalScroll -= scrollOffset;
					break;
			}
			webLocator2 = super.getWebLocator2(webDriver, params);

			long newScrollPos = getScrollPosition(divWithScrollbar);
			if (newScrollPos == prevScrollPos) {
				if (!isBothDir)
					throw new ScriptExecuteException("Requested element didn't appear while scrolling");

				dir = Direction.reverseDirection(dir);
				isBothDir = false;
			}
			prevScrollPos = newScrollPos;
		}

		logger.info("Requested element appear, after scrolling by {}", totalScroll);
		return webLocator2;
	}

	private Direction getDirection(Map<String, String> params) {
		Direction defaultDir = Direction.getDefault();
		String direction = params.get(PARAM_SCROLL_DIR);
		if (direction == null)
			return defaultDir;

		try {
			return Direction.valueOf(direction);
		} catch (IllegalArgumentException e) {
			logger.warn("Illegal value of '{}' param ('{}'), default value ('{}') will be used",
					PARAM_SCROLL_DIR, direction, defaultDir);
			return defaultDir;
		}
	}

	private boolean needDoScrollTo(Map<String, String> params) {
		return !params.containsKey(PARAM_DO_SCROLL_TO) || RhUtils.YES.contains(params.get(PARAM_DO_SCROLL_TO));
	}

	private long getScrollPosition(WebElement webElement) {
		return (Long) executeJsScript("return arguments[0].scrollTop", webElement);
	}

	private enum Direction {
		up,
		down,
		both;

		public static Direction getDefault() {
			return both;
		}

		public static Direction reverseDirection(Direction direction) {
			switch (direction) {
				case up:    return down;
				case down:  return up;
				default:    return direction;
			}
		}
	}
}