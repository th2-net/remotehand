/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand;

public enum RemoteManagerType
{
	WEB("WebRemoteHandManager"),
	WINDOWS("WindowsRemoteHandManager");

	private final String label;


	RemoteManagerType(String label)
	{
		this.label = label;
	}


	public String getLabel()
	{
		return label;
	}

	public static RemoteManagerType getByLabel(String label)
	{
		for (RemoteManagerType value : values())
		{
			if (value.getLabel().equals(label))
				return value;
		}

		return null;
	}
}
