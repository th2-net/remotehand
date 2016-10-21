////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.http;

import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptExecuteException;

public class ErrorRespondent
{
	protected static final int COMPILE_ERROR_CODE = 1,
			EXECUTE_ERROR_CODE = 2, 
			BUSY_ERROR_CODE = 3, 
			INCORRECT_REQ_CODE = 4;

	protected static final String ERROR_MARK = "%error%";

	protected static ErrorRespondent respondent;

	public static synchronized ErrorRespondent getRespondent()
	{
		if (respondent == null)
			respondent = new ErrorRespondent();
		return respondent;
	}

	public String error(Exception ex) {
		if (ex instanceof ScriptCompileException) {
			return ERROR_MARK + "=" + COMPILE_ERROR_CODE + " : " + ex.getMessage();
		} if (ex instanceof ScriptExecuteException) {
			return ERROR_MARK + "=" + EXECUTE_ERROR_CODE + " : " + ex.getMessage();
		} else if (ex instanceof ThreadBusyException) {
			return ERROR_MARK + "=" + BUSY_ERROR_CODE + " : " + ex.getMessage();
		} else if (ex instanceof IncorrectRequestException) {
			return ERROR_MARK + "=" + INCORRECT_REQ_CODE + " : " + ex.getMessage();
		}
		throw ex instanceof RuntimeException? (RuntimeException) ex : new RuntimeException(ex);
	}
}
