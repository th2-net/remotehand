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

import com.exactprosystems.remotehand.requests.RhRequest;

public class TcpRequest
{
	private final String sessionId;
	private final RhRequest request;
	
	public TcpRequest(String sessionId, RhRequest request)
	{
		this.sessionId = sessionId;
		this.request = request;
	}
	
	
	public String getSessionId()
	{
		return sessionId;
	}
	
	public RhRequest getRequest()
	{
		return request;
	}
}