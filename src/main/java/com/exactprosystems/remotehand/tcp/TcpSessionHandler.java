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
import com.exactprosystems.remotehand.sessions.SessionHandler;

public class TcpSessionHandler extends SessionHandler
{
	public TcpSessionHandler(String id, IRemoteHandManager manager)
	{
		super(id, manager);
	}

	@Override
	protected void closeConnection() throws IllegalArgumentException
	{
		TcpSessions.getInstance().removeSession(getId());
	}
}