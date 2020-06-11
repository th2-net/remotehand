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

import java.util.HashMap;
import java.util.Map;

import com.exactprosystems.remotehand.IRemoteHandManager;

public class TcpSessions
{
	private static TcpSessions instance;
	
	private final Map<String, TcpSessionHandler> sessions = new HashMap<>();
	
	public static void init()
	{
		if (instance != null)
			return;
		instance = new TcpSessions();
	}
	
	public static TcpSessions getInstance()
	{
		return instance;
	}
	
	public void addSession(String sessionId, IRemoteHandManager manager)
	{
		sessions.put(sessionId, new TcpSessionHandler(sessionId, manager));
	}
	
	public TcpSessionHandler getSession(String sessionId)
	{
		return sessions.get(sessionId);
	}
	
	public void removeSession(String sessionId)
	{
		sessions.remove(sessionId);
	}
}
