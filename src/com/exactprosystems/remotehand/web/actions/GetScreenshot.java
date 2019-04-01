/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web.actions;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.ActionOutputType;
import com.exactprosystems.remotehand.web.WebAction;
import org.apache.commons.lang3.math.NumberUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author anna.bykova.
 */
public class GetScreenshot extends WebAction
{
	private static final Logger log = LoggerFactory.getLogger(GetScreenshot.class);
	
	public static final String NAME_PARAM = "name", COMPRESSION_QUALITY_PARAM = "compressionquality";

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
	{
		float compressionQuality = DEFAULT_COMPRESSION_QUALITY;
		if (params.containsKey(COMPRESSION_QUALITY_PARAM))
		{
			String compressionQualityString = params.get(COMPRESSION_QUALITY_PARAM);
			if (NumberUtils.isNumber(compressionQualityString))
			{
				compressionQuality = Float.valueOf(compressionQualityString);
				if (compressionQuality < 0 || compressionQuality > 1)
					throw new ScriptExecuteException("Value in parameter '" + COMPRESSION_QUALITY_PARAM + "' should be in range from 0 to 1.");
			}
		}
		
		return takeScreenshot(params.get(NAME_PARAM), compressionQuality);
	}
	
	@Override
	public ActionOutputType getOutputType()
	{
		return ActionOutputType.SCREENSHOT;
	}

	@Override
	public boolean isNeedLocator()
	{
		return false;
	}

	@Override
	public boolean isCanWait()
	{
		return false;
	}
	
	@Override
	protected Logger getLogger()
	{
		return log;
	}
}
