/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.windows.actions;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.windows.WindowsAction;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Wait extends WindowsAction {

	private static final Logger loggerInstance = LoggerFactory.getLogger(Wait.class);

	private static final String PARAM_MILLIS = "millis";
	
	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params) throws ScriptExecuteException {
		int millis = Integer.parseInt(params.get(PARAM_MILLIS));
		this.logger.info("Pause for " + millis + " millis");
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			this.logger.error("Sleep interrupted", e);
		}

		return null;
	}

	@Override
	protected String[] mandatoryParams() {
		return new String[] { PARAM_MILLIS };
	}

	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}
}
