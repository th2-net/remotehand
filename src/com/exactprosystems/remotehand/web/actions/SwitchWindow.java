package com.exactprosystems.remotehand.web.actions;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

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
    protected boolean waitForElement(WebDriver webDriver, int seconds, By webLocator) throws ScriptExecuteException
    {
        Boolean findWindow;
        try
        {
            findWindow = (new WebDriverWait(webDriver, seconds)).until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver)
                {
                    try
                    {
                        return driver.getWindowHandles().size() >= getIntegerParam(getParams(), WINDOW) + 1;
                    } catch (WebDriverException | ScriptExecuteException e)
                    {
                        logger.error("During waiting opened window was thrown exception {}", e);
                        return false;
                    }
                }
            });
        } catch (TimeoutException ex)
        {
            throw new ScriptExecuteException("Timed out after " + seconds + ". Actual number opened windows is: " + webDriver.getWindowHandles().size() + ". Expected: " + getIntegerParam(getParams(), WINDOW) + 1);
        }

        if (!findWindow)
            throw new ScriptExecuteException("Actual number opened windows is: " + webDriver.getWindowHandles().size() + ". Expected: " + getIntegerParam(getParams(), WINDOW) + 1);

        logInfo("Number of windows: '%s'", webDriver.getWindowHandles().size());
        return findWindow;
    }

    @Override
    public boolean isCanWait()
    {
        return true;
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
