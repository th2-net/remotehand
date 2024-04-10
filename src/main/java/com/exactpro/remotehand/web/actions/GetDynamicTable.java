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

import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.web.WebAction;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Map;

public class GetDynamicTable extends WebAction {
	private static final int EXPIRED_TIME_SECONDS = 100;

	private int count = 0;

	public GetDynamicTable() {
		super(true, true);
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException {
		StringBuilder tableHTML = new StringBuilder();
		try {
			boolean haveMoreRows = true;

			WebElement tableElement = findElement(webDriver, webLocator);

			List<WebElement> previousRows = null;
			WebElement lastRow = null;

			while (haveMoreRows) {
				List<WebElement> rows = getRows(webDriver, tableElement);
				haveMoreRows = !rows.equals(previousRows);
				if (haveMoreRows && !rows.isEmpty()) {
					tableHTML.append(getInnerHTML(rows, lastRow));

					lastRow = rows.get(rows.size() - 1);
					((Locatable)lastRow).getCoordinates().inViewPort();

					if (logger.isInfoEnabled()) {
						logger.info("Obtained part of the table until the following row: {}", printTD(lastRow));
					}
				}

				previousRows = rows;
			}

			logger.info("Obtained {} rows of the table '{}'.", count, webLocator);

			return tableHTML.toString();
		}
		catch (TimeoutException ex) {
			throw new ScriptExecuteException("Timed out after " + EXPIRED_TIME_SECONDS + " seconds waiting for '" + webLocator.toString());
		}
	}

	private List<WebElement> getRows(WebDriver webDriver, final WebElement tableElement) throws TimeoutException {
		new WebDriverWait(webDriver, EXPIRED_TIME_SECONDS).until(new ExpectedCondition<Boolean>() {
			List<WebElement> previousElements = null;
			final WebElement currentTable = tableElement;

			@Override
			public Boolean apply(WebDriver driver) {
				List<WebElement> elements = currentTable.findElements(By.tagName("tr"));

				boolean foundEquals = elements.equals(previousElements);

				previousElements = elements;
				return foundEquals;
			}
		});
		return tableElement.findElements(By.tagName("tr"));
	}

	private String getInnerHTML(List<WebElement> elements, WebElement fromRow) {
		StringBuilder result = new StringBuilder();

		int posFrom = fromRow != null ? elements.indexOf(fromRow) + 1 : 0;

		for (int index = posFrom; index < elements.size(); index++) {
			result.append(elements.get(index).getAttribute("outerHTML"));
			count++;
		}

		return result.toString();
	}

	private String printTD(WebElement trElement) {
		if (trElement.getTagName().equals("tr")) {
			List<WebElement> tdElements = trElement.findElements(By.tagName("td"));

			if (tdElements.isEmpty())
				return "no one cells";

			StringBuilder result = new StringBuilder();
			result.append("|");
			for (WebElement tdElement : tdElements)
				result.append(tdElement.getText()).append("|");
			return result.toString();
		}

		return null;
	}
}