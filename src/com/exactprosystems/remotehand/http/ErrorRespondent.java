package com.exactprosystems.remotehand.http;

import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptExecuteException;

public class ErrorRespondent
{
	private static final int COMPILE_ERROR_CODE = 1, 
			EXECUTE_ERROR_CODE = 2, 
			BUSY_ERROR_CODE = 3, 
			INCORRECT_REQ_CODE = 4;

	private static final String ERROR_MARK = "%error%";

	private static ErrorRespondent respondent;

	public static synchronized ErrorRespondent getRespondent()
	{
		if (respondent == null)
			respondent = new ErrorRespondent();
		return respondent;
	}

	public String error(ScriptCompileException ex)
	{
		return ERROR_MARK + "=" + COMPILE_ERROR_CODE + " : " + ex.getMessage();
	}

	public String error(ScriptExecuteException ex)
	{
		return ERROR_MARK + "=" + EXECUTE_ERROR_CODE + " : " + ex.getMessage();
	}

	public String error(ThreadBusyException ex)
	{
		return ERROR_MARK + "=" + BUSY_ERROR_CODE + " : " + ex.getMessage();
	}

	public String error(IncorrectRequestException ex)
	{
		return ERROR_MARK + "=" + INCORRECT_REQ_CODE + " : " + ex.getMessage();
	}
}
