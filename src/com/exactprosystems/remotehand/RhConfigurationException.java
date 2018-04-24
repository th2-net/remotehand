/******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand;

/**
 * @author anna.bykova.
 *         23 November 2016
 */
public class RhConfigurationException extends Exception
{
	public RhConfigurationException(String message)
	{
		super(message);
	}

	public RhConfigurationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public RhConfigurationException(Throwable cause)
	{
		super(cause);
	}
}
