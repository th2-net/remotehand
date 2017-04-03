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
 * Created by alexey.suknatov on 4/3/17.
 */
public class SwitchWindow extends WebAction
{
    private final static Logger logger = Logger.getLogger(SwitchWindow.class);
    private final static String CHILD = "child";

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
        String parentWindowHandle;
        if (iterator.hasNext())
        {
            parentWindowHandle = iterator.next();
            if (params.containsKey(CHILD))
            {
                int childNumber = getIntegerParam(params, CHILD);
                String childWindowHandler = null;
                for (int i = 1; i <= childNumber; i++)
                {
                    if (iterator.hasNext())
                    {
                        childWindowHandler = iterator.next();
                    } else
                    {
                        logger.error("There is no such child: " + childNumber);
                        throw new ScriptExecuteException("There is no such child: " + childNumber);
                    }
                }
                if (childWindowHandler != null)
                {
                    logger.debug("Child number is: " + childNumber);
                    webDriver.switchTo().window(childWindowHandler);
                }
            } else
            {
                webDriver.switchTo().window(parentWindowHandle);
            }
        }
        return null;
    }

    @Override
    protected Logger getLogger()
    {
        return logger;
    }
}
