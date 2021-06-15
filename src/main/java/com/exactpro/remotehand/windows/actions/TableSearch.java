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

import com.exactpro.remotehand.RhUtils;
import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.windows.ElementSearcher;
import com.exactpro.remotehand.windows.WindowsAction;
import com.exactpro.remotehand.windows.WindowsDriverWrapper;
import com.exactpro.remotehand.windows.WindowsSessionContext;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final String DEFAULT_FIRST_INDEX_NUMBER = "0";
	private static final String ROW_NAME_FORMAT = "rownameformat";
	private static final String DEFAULT_ROW_NAME_FORMAT = "Row %s";
	private static final String ROW_ELEMENT_NAME_FORMAT = "rowelementnameformat";
	private static final String DEFAULT_ROW_ELEMENT_NAME_FORMAT = "%s row %s";
	private static final String ROW_ELEMENT_VALUE_FORMAT = "rowelementvalueformat";
	private static final String DEFAULT_ELEMENT_VALUE_FORMAT = "Value.Value";
	private static final String SAVE_RESULT = "saveresult";
	
	public static final String NOT_FOUND = "not found";
	public static final String FOUND = "found";

	private String rowNameFormat;
	private String rowElementNameFormat;
	private String rowElementValueFormat;
	private String targetColumnName;
	private int columnIndex;
	private int firstRowIndex;
	private boolean saveResult;
	private List<RhUtils.Filter> filtersMap;

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
			int i = 0;
			while (!rowFound) {
				row = findRow(table, i + firstRowIndex);

				for (RhUtils.Filter kvFilter : filtersMap) {
					String rowName = format(rowElementNameFormat, kvFilter.name, i);
					WebElement rowElement;
					
					if (kvFilter.index == null) {
						rowElement = row.findElement(By.name(rowName));
					} else {
						List<WebElement> elements = row.findElements(By.name(rowName));
						if (elements == null || elements.size() < kvFilter.index) {
							logger.warn("Not found element with id {} and index {} on row #{}", rowName, kvFilter.index, i + firstRowIndex);
							return NOT_FOUND;
						}
						rowElement = elements.get(kvFilter.index);
					}
					
					String attribute = StringUtils.defaultString(rowElement.getAttribute(rowElementValueFormat), StringUtils.EMPTY);
					rowFound = kvFilter.value.equals(attribute);
					if (!rowFound)
						break;
				}

				if (saveResult && rowFound) {
					WebElement targetColumn = getTargetColumn(row, i);
					cachedElements.storeWebElement(getId(), targetColumn);
				}

				i++;
			}
		} catch (NoSuchElementException e) {
			logger.warn("Column cannot be found", e);
			return NOT_FOUND;
		} finally {
			setTimeOut(driverWrapper, driverWrapper.getImplicitlyWaitTimeout());
		}

		return FOUND;
	}

	private void handleInputParams(Map<String, String> params) throws ScriptExecuteException
	{
		if (getId() == null) {
			throw new ScriptExecuteException("Id is not specified");
		}
		firstRowIndex = Integer.parseInt(params.getOrDefault(FIRST_ROW_INDEX, DEFAULT_FIRST_INDEX_NUMBER));
		columnIndex = Integer.parseInt(params.getOrDefault(INDEX, DEFAULT_FIRST_INDEX_NUMBER));
		rowNameFormat = params.getOrDefault(ROW_NAME_FORMAT, DEFAULT_ROW_NAME_FORMAT);
		rowElementNameFormat = params.getOrDefault(ROW_ELEMENT_NAME_FORMAT, DEFAULT_ROW_ELEMENT_NAME_FORMAT);
		rowElementValueFormat = params.getOrDefault(ROW_ELEMENT_VALUE_FORMAT, DEFAULT_ELEMENT_VALUE_FORMAT);
		targetColumnName = params.get(TARGET_COLUMN);
		saveResult = RhUtils.getBooleanOrDefault(params, SAVE_RESULT, true);
		filtersMap = RhUtils.buildFilters(params.get(FILTER));
		if (filtersMap.isEmpty())
			throw new ScriptExecuteException("Filter map cannot be empty");
	}

	protected WebElement findRow(WebElement table, int index) {
		return table.findElement(By.name(format(rowNameFormat, index)));
	}

	private WebElement getTargetColumn(WebElement row, int rowIndex) {
		By columnLocator = By.name(format(rowElementNameFormat, targetColumnName, rowIndex));
		if (columnIndex > 0) {
			List<WebElement> elements = row.findElements(columnLocator);
			if (columnIndex > elements.size())
				throw new NoSuchElementException("Cannot find column with index " + columnIndex
						+ ", column index is greater than the total column range");
			return elements.get(columnIndex);
		} else {
			return row.findElement(columnLocator);
		}
	}

	private void setTimeOut(WindowsDriverWrapper driverWrapper, int seconds) throws ScriptExecuteException {
		driverWrapper.getDriver().manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
	}

	@Override
	protected Logger getLoggerInstance()
	{
		return loggerInstance;
	}
}
