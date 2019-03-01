package com.exactprosystems.remotehand.web.actions;

import com.exactprosystems.remotehand.ScriptExecuteException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.Map;

public class GetElementValue extends GetElement
{
    @Override
    public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException
    {
        return findElement(webDriver, webLocator).getAttribute("value");
    }
}
