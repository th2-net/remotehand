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
    private final static String WINDOW = "window";

    public SwitchWindow()
    {
        super.mandatoryParams = new String[]{WINDOW};
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
    public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
    {
        Set<String> windowHandles = webDriver.getWindowHandles();
        Iterator<String> iterator = windowHandles.iterator();
        if (iterator.hasNext())
        {
            int windowNumber = getIntegerParam(params, WINDOW);
            if (windowNumber < 0 || windowNumber > windowHandles.size() - 1)
            {
                String errorMessage = "There is no such window: " + windowNumber;
                logger.error(errorMessage);
                throw new ScriptExecuteException(errorMessage);
            } else
            {
                String windowHandle = iterator.next();
                for (int i = 0; i < windowNumber; i++)
                {
                    windowHandle = iterator.next();
                }
                webDriver.switchTo().window(windowHandle);
                logger.debug("Child number is: " + windowNumber);
                webDriver.switchTo().window(windowHandle);
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
