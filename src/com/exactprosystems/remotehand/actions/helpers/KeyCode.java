////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2014, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.actions.helpers;

public enum KeyCode
{
	UP("up"),
	DOWN("down"),
	LEFT("left"),
	RIGHT("right"),
	RETURN("return"),
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
