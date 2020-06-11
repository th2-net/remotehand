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
 * Created by alexey.karpukhin on 2/1/16.
 */
public abstract class Action {

	public abstract String execute() throws ScriptExecuteException;

	public void beforeExecute() {}
}
