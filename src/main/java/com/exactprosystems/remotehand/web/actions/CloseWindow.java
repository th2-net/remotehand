/*
 * Copyright 2020-2020 Exactpro (Exactpro Systems Limited)
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

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

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
