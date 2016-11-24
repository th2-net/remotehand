////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
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
