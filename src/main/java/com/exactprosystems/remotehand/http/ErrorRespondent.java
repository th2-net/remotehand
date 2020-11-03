/*
 * Copyright 2020-2020 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exactprosystems.remotehand.http;

import com.exactprosystems.clearth.connectivity.data.rhdata.RhScriptResult;
import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.utils.ExceptionUtils;

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
		return error(ex, ExceptionUtils.getDetailedMessage(ex));
	}

	public RhScriptResult error(Exception ex, String errorMessage)
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

		result.setErrorMessage(errorMessage);

		return result;
	}
}
