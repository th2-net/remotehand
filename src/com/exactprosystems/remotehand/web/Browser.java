/******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web;

public enum Browser
{
	FIREFOX("Firefox"), 
	IE("IE"), 
	CHROME("Chrome"), 
	HEADLESS("Headless"),
	INVALID("");

	private String label;

	Browser(String label)
	{
		this.label = label;
	}

	public String getLabel()
	{
		return label;
	}

	public static Browser valueByLabel(String label)
	{
		if (label == null)
			return INVALID;

		for (Browser b : values())
			if (b.label.equals(label))
				return b;
		return INVALID;
	}
}
