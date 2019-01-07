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
import com.exactprosystems.remotehand.web.WebAction;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SetZoom extends WebAction
{
    private static final Logger logger = LoggerFactory.getLogger(SetZoom.class);

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
    public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
    {
        String zoomVal = params.get("value");
        
        JavascriptExecutor executor = (JavascriptExecutor)webDriver;
        executor.executeScript(String.format("document.body.style.zoom = '%s'", zoomVal));
        
        return null;
    }

    @Override
    protected Logger getLogger()
    {
        return logger;
    }
}
