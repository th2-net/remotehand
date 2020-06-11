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

public class ScriptCompileException extends Exception
{
	private static final long serialVersionUID = 1L;

	public ScriptCompileException(String message)
	{
		super(message);
	}

	public ScriptCompileException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScriptCompileException(Throwable cause) {
		super(cause);
	}
}
