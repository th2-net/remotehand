/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web.actions.mtable;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.exactprosystems.remotehand.web.actions.mtable.MTableUtils.getIntValue;

public class MTableGetRowCount extends WebAction
{
	private static final Logger logger = LoggerFactory.getLogger(MTableGetRowCount.class);
	
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
	protected Logger getLogger()
	{
		return logger;
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
