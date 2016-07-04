package com.exactprosystems.remotehand.web.actions.mtable;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.Map;

import static com.exactprosystems.remotehand.web.actions.mtable.MTableUtils.getIntValue;

public class MTableGetRowCount extends WebAction
{
	@Override
	public boolean isNeedLocator()
	{
		return false;
	}

	@Override
	public boolean isCanWait()
	{
		return true;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		String tableId = params.get(MTableUtils.TABLE_ID),
				tableIndex = params.get(MTableUtils.TABLE_INDEX);
		if (tableId == null || tableId.isEmpty())
			throw new ScriptExecuteException("Mandatory parameter '"+MTableUtils.TABLE_ID+"' is empty");
		int rowCount = MTableUtils.getTableRowCount(webDriver, tableId, getIntValue(tableIndex, 0));
		return String.valueOf(rowCount);
	}
}