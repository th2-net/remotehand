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

package com.exactprosystems.remotehand;

public class ScriptExecuteException extends Exception
{
	private static final long serialVersionUID = 2L;
	
	private String screenshotId;

	public ScriptExecuteException(String message)
	{
		super(message);
	}

	public ScriptExecuteException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScriptExecuteException(Throwable cause) {
		super(cause);
	}

	public String getScreenshotId()
	{
		return screenshotId;
	}

	public void setScreenshotId(String screenshotId)
	{
		this.screenshotId = screenshotId;
	}
}
