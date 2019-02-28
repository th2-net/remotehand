/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.requests;

public class ExecutionRequest implements RhRequest
{
	private final String script;
	
	public ExecutionRequest(String script)
	{
		this.script = script;
	}

	
	public String getScript()
	{
		return script;
	}
}
