/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.windows.actions;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.windows.ElementSearcher;
import com.exactprosystems.remotehand.windows.WindowsAction;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;
import com.exactprosystems.remotehand.windows.WindowsSessionContext;
import io.appium.java_client.windows.WindowsDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class TableClick extends WindowsAction {
	private static final Logger logInstance = LoggerFactory.getLogger(TableClick.class);
	private static final String FILTER = "filter";
	private static final String TARGET = "target";
	private static final String INDEX = "index";
	private static final String ROW_FORMAT = "Row %s";
	private static final String ROW_ELEMENT_FORMAT = "%s row %s";
	private static final String COLUMN_VALUE = "Value.Value";
	
	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params,
					  WindowsSessionContext.CachedWebElements cachedElements) throws ScriptExecuteException {
		try {
			ElementSearcher elementSearcher = new ElementSearcher(params, driverWrapper.getDriver(), cachedElements);
			WebElement table = elementSearcher.searchElement();
			String filters = params.get(FILTER);
			Map<String, String> filtersMap = getFilters(filters);
			setTimeOut(driverWrapper, 0);

			int i = 1;
			WebElement row;
			boolean rowFound = false;
			while (!rowFound) {
				row = this.findRow(table, i);
				
				for (Map.Entry<String, String> kvFilter : filtersMap.entrySet()) {
					WebElement rowElement = row.findElement(By.name(format(ROW_ELEMENT_FORMAT, kvFilter.getKey(), i - 1)));
					String attribute = rowElement.getAttribute(COLUMN_VALUE);
					rowFound = attribute.equals(kvFilter.getValue());
					if (!rowFound)
						break;
				}

				if (rowFound) {
					WebElement targetColumn = getTargetColumn(row, params, i);
					clickToColumn(driverWrapper.getDriver(), targetColumn);
				}

				i++;
			}
		} finally {
			setTimeOut(driverWrapper, driverWrapper.getImplicitlyWaitTimeout());
		}

		return null;
	}
	
	protected WebElement findRow(WebElement table, int index) throws ScriptExecuteException {
		try {
			return table.findElement(By.name(format(ROW_FORMAT, index)));
		} catch (NoSuchElementException e) {
			logger.error("Row with num " + index + " is not found", e);
			throw new ScriptExecuteException("Row with index " + index + " is not found");
		}
	}

	@Override
	protected Logger getLoggerInstance() {
		return logInstance;
	}


	private WebElement getTargetColumn(WebElement row, Map<String, String> params, int rowIndex) throws ScriptExecuteException {
		int columnIndex = getColumnIndex(params);
		By columnLocator = By.name(format(ROW_ELEMENT_FORMAT, params.get(TARGET), rowIndex - 1));
		if (columnIndex > 0) {
			List<WebElement> elements = row.findElements(columnLocator);
			if (columnIndex > elements.size())
				throw new ScriptExecuteException("Cannot find column with index " + columnIndex);
			return elements.get(columnIndex);
		} else {
			return row.findElement(columnLocator);
		}
	}

	private void clickToColumn(WindowsDriver<?> driver, WebElement targetColumn) {
		Actions actions = new Actions(driver);
		actions.moveToElement(targetColumn);
		actions.click();
		actions.perform();
	}

	private int getColumnIndex(Map<String, String> params) {
		String index = params.get(INDEX);
		return index == null ? 0 : Integer.parseInt(index);
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
}
