/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.remotehand;

/**
 * Additional special keys not defined in org.openqa.selenium.Keys
 * 
 * 12 June 2019
 */
public enum SpecialKeys implements CharSequence
{
	NON_BREAKING_SPACE('\u00a0');
	
	private final char keyCode;

	SpecialKeys(char keyCode)
	{
		this.keyCode = keyCode;
	}

	@Override
	public int length()
	{
		return 1;
	}

	@Override
	public char charAt(int index)
	{
		return index == 0 ? this.keyCode : '\u0000';
	}

	@Override
	public CharSequence subSequence(int start, int end)
	{
		if ((start == 0) && (end == 1))
			return String.valueOf(keyCode);
		else 
			throw new IndexOutOfBoundsException();
	}

	@Override
	public String toString()
	{
		return String.valueOf(keyCode);
	}
}
