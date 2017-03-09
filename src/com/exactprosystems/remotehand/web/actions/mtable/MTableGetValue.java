////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2017, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web.actions.mtable;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

/**
 * @author daria.plotnikova
 *
 */
public class MTableGetValue extends WebAction
{
	private static final Logger logger = Logger.getLogger(MTableGetValue.class);
	
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
				tableIndex = params.get(MTableUtils.TABLE_INDEX),
				row = params.get(MTableUtils.ROW),
				column = params.get(MTableUtils.COLUMN);
		
		int tableInd, rowInd;
		
		logInfo("Input params: "+params);

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
		
		if (row == null || row.isEmpty())
		{
			rowInd = 0;
		}
		else 
			try
			{
				rowInd = Integer.valueOf(row);
			}
			catch(Exception e)
			{
				throw new ScriptExecuteException("Row index is not valid", e);
			}
		
		if (column == null || tableId == null)
			logError("Some of input parameters are not defined!");
		
		
		String result = null;
		try
		{
		result = MTableUtils.getTableCellDataByColName(webDriver, tableId, tableInd, rowInd, column);
		}
		catch(Exception e)
		{
			logError("Exception while executing javascript ", e);
		}
		
		return result;
	}
	
}
