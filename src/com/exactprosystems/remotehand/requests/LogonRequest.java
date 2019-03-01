/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.requests;

/**
 * This class is not used by HTTP server mode as logon requests are handled by separate HTTP handler bound to particular URL.
 * Useful for other protocols like TCP/IP
 */
public class LogonRequest implements RhRequest
{
}
