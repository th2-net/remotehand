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

import com.exactprosystems.remotehand.IRemoteHandManager;
import com.exactprosystems.remotehand.sessions.LogonHandler;
import com.exactprosystems.remotehand.sessions.SessionExchange;

public class TcpLogonHandler extends LogonHandler
{
	public TcpLogonHandler(IRemoteHandManager manager)
	{
		super(manager);
	}
	
	@Override
	public void handleLogon(String sessionId, SessionExchange exchange)
	{
		TcpSessions.getInstance().addSession(sessionId, getManager());
	}
}