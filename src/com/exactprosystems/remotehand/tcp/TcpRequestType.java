/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.tcp;

public enum TcpRequestType
{
	LOGON(0),
	SCRIPT(1),
	STATUS(2),
	FILE(10),
	DOWNLOAD(20),
	LOGOUT(99);
	
	private final int code;
	
	private TcpRequestType(int code)
	{
		this.code = code;
	}
	
	public int getCode()
	{
		return code;
	}
	
	
	public static TcpRequestType byCode(int code)
	{
		for (TcpRequestType rt : values())
		{
			if (rt.getCode() == code)
				return rt;
		}
		return SCRIPT;  //This makes protocol less strict
	}
}