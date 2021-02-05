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

package com.exactprosystems.remotehand.windows.actions;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.windows.ElementSearcher;
import com.exactprosystems.remotehand.windows.WindowsAction;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;
import com.exactprosystems.remotehand.windows.WindowsSessionContext;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class TableSearch extends WindowsAction
{
	private static final Logger loggerInstance = LoggerFactory.getLogger(TableSearch.class);
	private static final String FILTER = "filter";
	private static final String TARGET_COLUMN = "column";
	private static final String INDEX = "index";
	private static final String FIRST_ROW_INDEX = "firstrowindex";
	private static final String DEFAULT_FIRST_ROW_NUMBER = "0";
	private static final String ROW_NAME_FORMAT = "rowNameFormat";
	private static final String DEFAULT_ROW_NAME_FORMAT = "Row %s";
	private static final String ROW_ELEMENT_NAME_FORMAT = "rowelementnameformat";
	private static final String DEFAULT_ROW_ELEMENT_NAME_FORMAT = "%s row %s";
	private static final String ROW_ELEMENT_VALUE_FORMAT = "rowelementvalueformat";
	private static final String DEFAULT_ELEMENT_VALUE_FORMAT = "Value.Value";

	private String rowNameFormat;
	private String rowElementNameFormat;
	private String rowElementValueFormat;
	private String targetColumnName;
	private int columnIndex;
	private int firstRowIndex;
	private Map<String, String> filtersMap;

	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params,
			WindowsSessionContext.CachedWebElements cachedElements) throws ScriptExecuteException
	{
		try {
			handleInputParams(params);
			ElementSearcher elementSearcher = new ElementSearcher(params, driverWrapper.getDriver(), cachedElements);
			WebElement table = elementSearcher.searchElement();
			setTimeOut(driverWrapper, 0);

			WebElement row;
			boolean rowFound = false;
			int i = firstRowIndex;
			while (!rowFound) {
				row = findRow(table, i);

				for (Map.Entry<String, String> kvFilter : filtersMap.entrySet()) {
					String rowName = format(rowElementNameFormat, kvFilter.getKey(), i);
					WebElement rowElement = row.findElement(By.name(rowName));
					String attribute = rowElement.getAttribute(rowElementValueFormat);
					rowFound = attribute.equals(kvFilter.getValue());
					if (!rowFound)
						break;
				}

				if (rowFound) {
					WebElement targetColumn = getTargetColumn(row, i);
					cachedElements.storeWebElement(getId(), targetColumn);
				}

				i++;
			}
		} finally {
			setTimeOut(driverWrapper, driverWrapper.getImplicitlyWaitTimeout());
		}

		return null;
	}
	
	private void handleInputParams(Map<String, String> params) throws ScriptExecuteException
	{
		if (getId() == null) {
			throw new ScriptExecuteException("Id is not specified");
		}
		firstRowIndex = Integer.parseInt(params.getOrDefault(FIRST_ROW_INDEX, DEFAULT_FIRST_ROW_NUMBER));
		columnIndex = Integer.parseInt(params.getOrDefault(INDEX, "0"));
		rowNameFormat = params.getOrDefault(ROW_NAME_FORMAT, DEFAULT_ROW_NAME_FORMAT);
		rowElementNameFormat = params.getOrDefault(ROW_ELEMENT_NAME_FORMAT, DEFAULT_ROW_ELEMENT_NAME_FORMAT);
		rowElementValueFormat = params.getOrDefault(ROW_ELEMENT_VALUE_FORMAT, DEFAULT_ELEMENT_VALUE_FORMAT);
		targetColumnName = params.get(TARGET_COLUMN);
		String filters = params.get(FILTER);
		filtersMap = getFilters(filters);
	}

	protected WebElement findRow(WebElement table, int index) throws ScriptExecuteException {
		try {
			return table.findElement(By.name(format(rowNameFormat, index)));
		} catch (NoSuchElementException e) {
			throw new ScriptExecuteException("Row with index " + index + " is not found", e);
		}
	}

	private WebElement getTargetColumn(WebElement row, int rowIndex) throws ScriptExecuteException {
		By columnLocator = By.name(format(rowElementNameFormat, targetColumnName, rowIndex));
		if (columnIndex > 0) {
			List<WebElement> elements = row.findElements(columnLocator);
			if (columnIndex > elements.size())
				throw new ScriptExecuteException("Cannot find column with index " + columnIndex);
			return elements.get(columnIndex);
		} else {
			return row.findElement(columnLocator);
		}
	}

	private void setTimeOut(WindowsDriverWrapper driverWrapper, int seconds) throws ScriptExecuteException {
		driverWrapper.getDriver().manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
	}

	private static Map<String, String> getFilters(String filters) {
		Map<String, String> result = new HashMap<>();

		String[] splitFilters = filters.split(";");
		for (String splitFilter : splitFilters) {
			String[] kvPair = splitFilter.split("=");
			if (kvPair.length != 2)
				continue;

			result.put(kvPair[0], kvPair[1]);
		}

		return result;
	}


	@Override
	protected Logger getLoggerInstance()
	{
		return loggerInstance;
	}
}
