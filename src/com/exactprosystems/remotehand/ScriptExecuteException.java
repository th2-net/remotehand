/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

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
