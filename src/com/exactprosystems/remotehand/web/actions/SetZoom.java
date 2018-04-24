/******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
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
import org.apache.log4j.Logger;
import org.openqa.selenium.*;

import java.util.Map;

public class SetZoom extends WebAction
{
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
        return getLogger();
    }
}
