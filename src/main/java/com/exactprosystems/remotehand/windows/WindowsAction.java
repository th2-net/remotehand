/*
 * Copyright 2020-2020 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exactprosystems.remotehand.windows;

import com.exactprosystems.remotehand.Action;
import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebScriptCompiler;
import com.exactprosystems.remotehand.windows.WindowsSessionContext.CachedWebElements;
import org.mvel2.MVEL;
import org.openqa.selenium.NoSuchElementException;
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
			try {
				result = this.run(windowsSessionContext.getCurrentDriver(), params, windowsSessionContext.getCachedObjects());
				logger.debug("Action result: {}", result);
				if (result != null && id != null) {
					windowsSessionContext.getMvelVars().put(id, result);
					logger.trace("Action result saved to id: {}", id);
				}
			} catch (NoSuchElementException e) {
				throw new ScriptExecuteException(String.format("Action '%s' cannot be executed", getActionName()), e);
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
