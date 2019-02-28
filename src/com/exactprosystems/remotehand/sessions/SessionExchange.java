/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.sessions;

import java.io.IOException;

/**
 * Adapter for various mechanisms to interact with requestor
 */
public interface SessionExchange
{
	void sendResponse(int code, String message) throws IOException;
	String getRemoteAddress();
}
