package com.exactprosystems.remotehand.web.actions;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by alexey.suknatov on 4/6/17.
 */
public class CloseWindow extends WebAction
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
        Set<String> windowHandles = webDriver.getWindowHandles();
        Iterator<String> iterator = windowHandles.iterator();
        String parentWindowHandle = null;
        if (iterator.hasNext())
        {
            parentWindowHandle = iterator.next();
        }
        webDriver.close();
        if (windowHandles.size() > 1)
        {
           webDriver.switchTo().window(parentWindowHandle);
        }
        return null;
    }

    @Override
    protected Logger getLogger()
    {
        return null;
    }
}
