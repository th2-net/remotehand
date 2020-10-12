/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.windows;

import com.exactprosystems.remotehand.Action;
import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebScriptCompiler;
import com.exactprosystems.remotehand.windows.WindowsSessionContext.CachedWebElements;
import org.mvel2.MVEL;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.Map;

public abstract class WindowsAction extends Action {

	private WindowsSessionContext windowsSessionContext;
	protected Logger logger;
	private Map<String, String> params = null;
	private int lineNumber;
	private String id;

	public void init(WindowsSessionContext context, Map<String, String> params,
					 int number, String id) throws ScriptCompileException
	{
		this.windowsSessionContext = context;
		this.params = params;
		this.lineNumber = number;
		this.id = id;
		this.logger = new SessionLogger(context.getSessionId(), getLoggerInstance());
	}
	
	public abstract String run (WindowsDriverWrapper driverWrapper, Map<String, String> params,
								CachedWebElements cachedElements) throws ScriptExecuteException;
	
	protected abstract Logger getLoggerInstance();
		
	protected String[] mandatoryParams() {
		return new String[0];
	}

	public Map<String, String> getParams() {
		return params;
	}
	
	private boolean checkIsExecute() {
		String execute = getParams().get("execute");
		if (execute == null || execute.isEmpty()) {
			return true;
		} else {
			logger.trace("Checking condition: {}", execute);
			boolean result = false;
			try {
				Serializable compiled = MVEL.compileExpression(execute);
				result = org.mvel2.MVEL.executeExpression(compiled, windowsSessionContext.getMvelVars(), Boolean.class);
			} catch (Exception e) {
				logger.warn("Error while executing expression: " + execute, e);
			}
			logger.trace("Calculated execute field: {}", result);
			return result;
		}
	}

	public String getId() {
		return id;
	}

	@Override
	public String execute() throws ScriptExecuteException {
		this.logger.info("Executing action in line: {} id {}", lineNumber, id);
		
		String result = null;
		
		if (checkIsExecute()) {
			result = this.run(windowsSessionContext.getCurrentDriver(), params, windowsSessionContext.getCachedObjects());
			logger.debug("Action result: {}", result);
			if (result != null && id != null) {
				windowsSessionContext.getMvelVars().put(id, result);
				logger.trace("Action result saved to id: {}", id);
			}
		} else {
			this.logger.info("Action was not executed due condition. And will be skipped");
		}
		result = this.checkMultiline(result);
		
		if (result != null && id != null) {
			return id + "=" + result;
		} else {
			return result;	
		}
	}
	
	//todo this logic should be in common. Check carefully web - part
	private String checkMultiline(String str) {
		if (str != null && str.indexOf('\n') >=0) {
			return str.replaceAll("\\r?\\n", WebScriptCompiler.SCRIPT_LINE_SEPARATOR);
		} else {
			return str;
		}
	}
	
	
}
