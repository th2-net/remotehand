////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web.actions.mtable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
public class MTableGetRow extends WebAction
{
	
	private static final Logger logger = Logger.getLogger(MTableGetRow.class);
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
				tableIndex = params.get(MTableUtils.TABLE_INDEX),
				row = params.get(MTableUtils.ROW),
				columns = params.get(MTableUtils.COLUMNS);
		
		List<String> colList = new ArrayList<String>();
		int tableInd, rowInd;

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
		
		if (tableId == null)
			logError("Some of input parameters are not defined!");
		
		Map<String,String> result = null;
		
		try
		{
			if (columns != null && !columns.isEmpty())
			{
				colList = Arrays.asList(columns.split(MTableUtils.COL_DELIMITER));
				result = MTableUtils.getTableRow(webDriver, tableId, tableInd, rowInd, colList);
			}
			else
			{
				result = new HashMap<String,String>();
				//Getting values from all cells doesn't work
				//result = MTableUtils.getTableRow(webDriver, tableId, tableInd, rowInd);
			}
		}
		catch(Exception e)
		{
			logError("Exception while executing javascript ", e);
		}
		
		return result.toString();
	}
}
