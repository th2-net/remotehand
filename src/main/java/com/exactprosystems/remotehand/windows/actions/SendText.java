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
import com.exactprosystems.remotehand.windows.ElementSearcher;
import com.exactprosystems.remotehand.windows.WindowsAction;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SendText extends WindowsAction {
	
	public static final String TEXT_PARAM = "text";

	private static final Logger logger = LoggerFactory.getLogger(SendText.class);
	

	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params) throws ScriptExecuteException {

		ElementSearcher es = new ElementSearcher();
		WebElement element = es.searchElement(params, driverWrapper.getDriver());

		String clearBefore = params.get("clearbefore");
		if ("y".equals(clearBefore)) {
			element.clear();
		}

		String s = params.get(TEXT_PARAM);
		if (s.contains("\\u")) {
			List<String> list = new ArrayList<>();
			int ind = 0, lastCopied = 0;
			while ((ind = s.indexOf("\\u", ind)) > -1) {
				String str = s.substring(lastCopied, ind);
				if (!str.isEmpty()) {
					list.add(str);
				}
				String code = s.substring(ind + 2, ind + 6);
				char i = (char) Integer.parseInt(code, 16);
				list.add(String.valueOf(i));
				lastCopied = ind + 6;
				ind = lastCopied;
			}
			
			String trailing = s.substring(lastCopied);
			if (!trailing.isEmpty()) {
				list.add(trailing);
			}
			
			element.sendKeys(list.toArray(new String[0]));
		} else {
			element.sendKeys(params.get(TEXT_PARAM));	
		}
		return null;
	}

	@Override
	public Logger getLoggerInstance() {
		return logger;
	}

	@Override
	protected String[] mandatoryParams() {
		return new String[] { TEXT_PARAM };
	}
}
