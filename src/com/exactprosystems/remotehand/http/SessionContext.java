/******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.http;

import java.util.HashMap;
import java.util.Map;

/**
 * @author anna.bykova.
 */
public class SessionContext
{
	private final String sessionId;
	private Map<String, Object> contextData;
	
	public SessionContext(String sessionId)
	{
		this.sessionId = sessionId;
		this.contextData = new HashMap<String, Object>();
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public Map<String, Object> getContextData() {
		return contextData;
	}
}
