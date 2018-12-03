/******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web.actions.mtable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

/**
 * @author daria.plotnikova
 *
 */
public class MTableGetTable extends WebAction
{
	
	private static final Logger logger = LoggerFactory.getLogger(MTableGetTable.class);
	@Override
	public boolean isNeedLocator()
	{
		return false;
	}

	@Override
	public boolean isCanWait()
	{
		return false;
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

		
		int tableInd;

		if (tableIndex == null || tableIndex.isEmpty())
		{
			tableInd = 0;
		}
		else 
			try
			{
				tableInd = Integer.valueOf(tableIndex);
			}
			catch(Exception e)
			{
				tableInd = 0;
			}
		
		
		if (tableId == null)
			logError("Some of input parameters are not defined!");
		
		List<Map<String,String>> result = new ArrayList<Map<String,String>> ();
		for (int i = 0; i < MTableUtils.getTableRowCount(webDriver, tableId, tableInd); i++)
		{
			Map<String,String> map = MTableUtils.getTableRow(webDriver, tableId, tableInd, i);
			result.add(map);
		}
		
		
		return result.toString();
	}
}
