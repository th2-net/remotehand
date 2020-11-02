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
	WEB("web"),
	WINDOWS("windows");

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
	
	public static String[] labels()
	{
		String[] result = new String[values().length];
		int i = 0;
		for (RemoteManagerType t : values())
			result[i++] = t.getLabel();
		return result;
	}
}
