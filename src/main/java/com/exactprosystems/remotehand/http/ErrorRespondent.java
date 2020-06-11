/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.http;

import com.exactprosystems.clearth.connectivity.data.rhdata.RhScriptResult;
import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptExecuteException;

import static com.exactprosystems.clearth.connectivity.data.rhdata.RhResponseCode.COMPILE_ERROR;
import static com.exactprosystems.clearth.connectivity.data.rhdata.RhResponseCode.EXECUTION_ERROR;
import static com.exactprosystems.clearth.connectivity.data.rhdata.RhResponseCode.RH_ERROR;

public class ErrorRespondent
{
	protected static ErrorRespondent respondent;

	public static synchronized ErrorRespondent getRespondent()
	{
		if (respondent == null)
			respondent = new ErrorRespondent();
		return respondent;
	}

	public RhScriptResult error(Exception ex)
	{
		RhScriptResult result = new RhScriptResult();
		if (ex instanceof ScriptCompileException)
		{
			result.setCode(COMPILE_ERROR.getCode());
		}
		else if (ex instanceof ScriptExecuteException)
		{
			ScriptExecuteException see = (ScriptExecuteException) ex; 
			result.setCode(EXECUTION_ERROR.getCode());
			if (see.getScreenshotId() != null)
				result.addScreenshotId(see.getScreenshotId());
		}
		else
		{
			result.setCode(RH_ERROR.getCode());
		}
		result.setErrorMessage(ex.getMessage());
		return result;
	}
}
