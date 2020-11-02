/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.grid;

public class RequestParametersException extends Exception
{
	private static final long serialVersionUID = 962498436207752146L;
	
	public RequestParametersException(String message)
	{
		super(message);
	}
	
	public RequestParametersException(Throwable cause)
	{
		super(cause);
	}
	
	public RequestParametersException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
