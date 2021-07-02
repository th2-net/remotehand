/*
 * Copyright 2020-2021 Exactpro (Exactpro Systems Limited)
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

package com.exactpro.remotehand.rhdata;

import com.exactpro.remotehand.ActionResult;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.exactpro.remotehand.rhdata.RhResponseCode.SUCCESS;

public class RhScriptResult
{
	private int code = SUCCESS.getCode();
	private String errorMessage;
	private List<ActionResult> actionResults;
	private List<ActionResult> screenshotIds;
	private List<ActionResult> encodedOutput;

	@JsonIgnore
	public boolean isSuccess()
	{
		return code == SUCCESS.getCode();
	}
	
	@JsonIgnore
	public boolean isFailed()
	{
		return code != SUCCESS.getCode();
	}
	

	public int getCode()
	{
		return code;
	}

	public void setCode(int code)
	{
		this.code = code;
	}

	
	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	
	public List<ActionResult> getActionResults()
	{
		return actionResults != null ? actionResults : Collections.emptyList();
	}

	public void setActionResults(List<ActionResult> actionResults)
	{
		this.actionResults = actionResults;
	}

	public void addToActionResults(ActionResult actionResult)
	{
		if (actionResults == null)
			actionResults = new ArrayList<>();
		actionResults.add(actionResult);
	}

	public List<ActionResult> getScreenshotIds()
	{
		return screenshotIds != null ? screenshotIds : Collections.emptyList();
	}

	public void setScreenshotIds(List<ActionResult> screenshotIds)
	{
		this.screenshotIds = screenshotIds;
	}
	
	public void addScreenshotId(ActionResult id)
	{
		if (screenshotIds == null)
			screenshotIds = new ArrayList<>();
		screenshotIds.add(id);
	}


	public List<ActionResult> getEncodedOutput()
	{
		return encodedOutput != null ? encodedOutput : Collections.emptyList();
	}

	public void setEncodedOutput(List<ActionResult> encodedOutput)
	{
		this.encodedOutput = encodedOutput;
	}
	
	public void addToEncodedOutput(ActionResult data)
	{
		if (encodedOutput == null)
			encodedOutput = new ArrayList<>();
		encodedOutput.add(data);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("RhScriptResult{");
		sb.append("code=").append(code);
		if (errorMessage != null)
			sb.append(", errorMessage='").append(errorMessage).append('\'');
		if (actionResults != null)
			sb.append(", actionResults=").append(actionResults);
		if (screenshotIds != null)
			sb.append(", screenshotIds=").append(screenshotIds);
		if (encodedOutput != null)
			sb.append(", encodedOutput=").append(encodedOutput);
		sb.append('}');
		return sb.toString();
	}
}
