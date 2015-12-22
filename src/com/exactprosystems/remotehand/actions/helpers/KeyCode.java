////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.actions.helpers;

public enum KeyCode
{
	UP("up"),
	DOWN("down"),
	LEFT("left"),
	RIGHT("right"),
	RETURN("return"),
	SPACE("space"),
	HASH("hash"),
	DOLLAR("dollar"),
	PERCENT("percent"),
	INVALID("");
	
	private final String label;
	
	KeyCode(String label)
	{
		this.label = label;
	}
	
	public static KeyCode codeByLabel(String label)
	{
		for (KeyCode kc : values())
			if (kc.label.equalsIgnoreCase(label))
				return kc;
		return INVALID;
	}
}
