package com.exactprosystems.remotehand.actions;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.exactprosystems.remotehand.Logger;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.WebAction;

public class GetDynamicTable extends WebAction
{
	private static final int EXPIRED_TIME = 100; // seconds

	private int count = 0;

	private static Logger logger = Logger.getLogger();

	public GetDynamicTable()
	{
		super.needLocator = true;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		StringBuffer tableHTML = new StringBuffer();
		try
		{
			boolean haveMoreRows = true;

			WebElement tableElement = webDriver.findElement(webLocator);

			List<WebElement> previousRows = null;
			WebElement lastRow = null;

			while (haveMoreRows)
			{
				List<WebElement> rows = getRows(webDriver, tableElement);
				haveMoreRows = !rows.equals(previousRows);
				if (haveMoreRows && !rows.isEmpty())
				{
					tableHTML.append(getInnerHTML(webDriver, rows, lastRow));

					lastRow = rows.get(rows.size() - 1);
					((Locatable)lastRow).getCoordinates().inViewPort();

					logger.info("Obtained part of the table until the following row: " + printTD(lastRow));
				}

				previousRows = rows;
			}

			logger.info("Obtained <" + count + "> rows of the table '" + tableElement + "'");

			return tableHTML.toString();
		}
		catch (TimeoutException ex)
		{
			throw new ScriptExecuteException("Timed out after " + EXPIRED_TIME + " seconds waiting for '" + webLocator.toString());
		}
	}

	private List<WebElement> getRows(WebDriver webDriver, final WebElement tableElement) throws TimeoutException
	{
		(new WebDriverWait(webDriver, EXPIRED_TIME)).until(new ExpectedCondition<Boolean>()
		{
			List<WebElement> previusElements = null;
			WebElement currentTable = tableElement;

			@Override
			public Boolean apply(WebDriver driver)
			{
				List<WebElement> elements = currentTable.findElements(By.tagName("tr"));

				boolean foundEquals = elements.equals(previusElements);

				previusElements = elements;
				return foundEquals;
			}
		});
		return tableElement.findElements(By.tagName("tr"));
	}

	private String getInnerHTML(WebDriver webDriver, List<WebElement> elements, WebElement fromRow)
	{
		StringBuffer result = new StringBuffer();

		final int posFrom;
		if (fromRow == null)
			posFrom = 0;
		else
			posFrom = elements.indexOf(fromRow) + 1;

		for (int inx = posFrom; inx < elements.size(); inx++)
		{
			result.append(elements.get(inx).getAttribute("outerHTML"));
			count++;
		}

		return result.toString();
	}

	private String printTD(WebElement trElement)
	{
		if (trElement.getTagName().equals("tr"))
		{
			List<WebElement> tdElements = trElement.findElements(By.tagName("td"));

			if (tdElements.isEmpty())
				return "no one cells";

			StringBuffer result = new StringBuffer();
			result.append("|");
			for (WebElement tdElement : tdElements)
				result.append(tdElement.getText()).append("|");
			return result.toString();
		}

		return null;
	}
}