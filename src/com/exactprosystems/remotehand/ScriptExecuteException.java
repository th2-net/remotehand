////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand;

public class ScriptExecuteException extends Exception
{
	private static final long serialVersionUID = 2L;

	public ScriptExecuteException(String message)
	{
		super(message);
	}
}
