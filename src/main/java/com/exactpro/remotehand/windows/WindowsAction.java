/*
 * Copyright 2020-2021 Exactpro (Exactpro Systems Limited)
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

package com.exactpro.remotehand.windows;

import com.exactpro.remotehand.Action;
import com.exactpro.remotehand.RhUtils;
import com.exactpro.remotehand.ScriptCompileException;
import com.exactpro.remotehand.ScriptExecuteException;
import com.exactpro.remotehand.web.WebScriptCompiler;
import com.exactpro.remotehand.windows.WindowsSessionContext.CachedWebElements;
import io.appium.java_client.windows.WindowsDriver;
import org.mvel2.MVEL;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.Map;

public abstract class WindowsAction extends Action {
	
	private static final String END_EXCEPTION_MESSAGE = "(WARNING: The server did not provide any stacktrace information)";

	public static final String EXPERIMENTAL_PARAM = "isexperimental";
	public static final String FROM_ROOT_PARAM = "fromroot";
	
	public static final Boolean DEFAULT_EXPERIMENTAL = Boolean.TRUE;

	protected WindowsSessionContext windowsSessionContext;
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
			} catch (ScriptExecuteException e) {
				throw addScreenshot(e);
			} catch (WebDriverException e) {
				throw addScreenshot(new WindowsScriptExecuteException(tryExtractErrorMessage(e), e));
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

	protected String takeScreenshot(String name) throws ScriptExecuteException {
		WindowsDriver<?> driver = windowsSessionContext.getCurrentDriver().getDriver(false, true);
		return screenWriter.takeAndSaveScreenshot(name, driver);
	}

	protected ScriptExecuteException addScreenshot(ScriptExecuteException see)
	{
		String screenshotId = null;
		try {
			screenshotId = takeScreenshot(null);
		} catch (ScriptExecuteException e) {
			logger.error("Could not create screenshot", e);
		}
		see.setScreenshotId(screenshotId);
		return see;
	}

	protected WindowsDriver<?> getDriver(WindowsDriverWrapper driverWrapper) throws ScriptExecuteException {
		boolean fromRoot = RhUtils.getBooleanOrDefault(params, FROM_ROOT_PARAM, false);
		boolean experimental = RhUtils.getBooleanOrDefault(params, EXPERIMENTAL_PARAM, DEFAULT_EXPERIMENTAL);
		return driverWrapper.getDriver(fromRoot, experimental);
	}

	private static String tryExtractErrorMessage(WebDriverException e) {
		
		StringBuilder errorMsgBuilder = new StringBuilder();
		
		String exceptionMessage = e.getMessage();
		int endFirstLine = exceptionMessage.indexOf('\n');
		if (endFirstLine == -1) {
			endFirstLine = exceptionMessage.length();
		}

		String baseExceptionMessage = exceptionMessage.substring(0, endFirstLine);
		int endMessage = baseExceptionMessage.indexOf(END_EXCEPTION_MESSAGE);
		if (endMessage != -1) {
			errorMsgBuilder.append(baseExceptionMessage, 0, endMessage);
		} else {
			errorMsgBuilder.append(baseExceptionMessage);
		}

		String additionalInfo = e.getAdditionalInformation();
		if (additionalInfo != null && !additionalInfo.isEmpty()) {
			additionalInfo = additionalInfo.substring(Math.max(0, additionalInfo.indexOf("Capabilities ")));
			errorMsgBuilder.append(' ').append(additionalInfo);
		}
		
		String systemInfo = e.getSystemInformation();
		if (systemInfo != null) {
			errorMsgBuilder.append(' ').append(systemInfo);
		}

		return errorMsgBuilder.toString();
	}
}
