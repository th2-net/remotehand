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

package com.exactprosystems.remotehand.web.actions;

import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.screenwriter.DefaultScreenWriter;
import com.exactprosystems.remotehand.screenwriter.ScreenWriter;
import com.exactprosystems.remotehand.web.WebAction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WaitForChanges extends WebAction {
	private static final Logger logger = LoggerFactory.getLogger(WaitForChanges.class);
	private static final String PARAM_SECONDS = "seconds",
			PARAM_SCREENSHOTID = "screenshotid",
			PARAM_CHECKMILLIS = "checkmillis";
	private static final ScreenWriter<?> screenWriter = new DefaultScreenWriter();


	@Override
	public boolean isNeedLocator()
	{
		return true;
	}
	
	@Override
	public boolean isCanWait()
	{
		return false;
	}
	
	@Override
	public String[] getMandatoryParams() throws ScriptCompileException
	{
		return new String[] {PARAM_SECONDS, PARAM_SCREENSHOTID, PARAM_CHECKMILLIS};
	}
	
	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		int seconds = getIntegerParam(params, PARAM_SECONDS),
				checkMillis = getIntegerParam(params, PARAM_CHECKMILLIS);
		String id = params.get(PARAM_SCREENSHOTID);
		
		Path screenPath = (Path)context.getContextData().get(StoreElementState.buildScreenshotId(id));
		if (screenPath == null)
			throw new ScriptExecuteException("No screenshot stored for ID='"+id+"'");

		byte[] initialState;
		try {
			initialState = Files.readAllBytes(screenPath);
		} catch (Exception e) {
			throw new ScriptExecuteException("Error retrieving saved screenshot for ID='"+id+"'");
		}
		
		long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(seconds);
		do
		{
			WebElement element = findElement(webDriver, webLocator);
			byte[] currentState = screenWriter.takeElementScreenshot(webDriver, element);
			if (!compareStates(initialState, currentState))
				return null;
			
			if (System.currentTimeMillis() >= endTime)
				break;
			
			try
			{
				Thread.sleep(checkMillis);
			}
			catch (InterruptedException e)
			{
				// do nothing like in WaitForNew
			}
		}
		while (true);

		throw new ScriptExecuteException("No changes caught in element during "+seconds+" seconds");
	}
	
	@Override
	protected Logger getLogger()
	{
		return logger;
	}
	
	
	private boolean compareStates(byte[] state1, byte[] state2)
	{
		return Arrays.equals(state1, state2);
	}
}
