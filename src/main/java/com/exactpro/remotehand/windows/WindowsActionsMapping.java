/*
 * Copyright 2020-2021 Exactpro (Exactpro Systems Limited)
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

package com.exactpro.remotehand.windows;

import com.exactpro.remotehand.ScriptCompileException;
import com.exactpro.remotehand.windows.actions.*;

public class WindowsActionsMapping {

	private enum WindowsActionName
	{
		Click,
		Open,
		SendText,
		GetActiveWindow,
		GetElementAttribute,
		Wait,
		ToggleCheckBox,
		CheckElement,
		GetWindow,
		SearchElement,
		WaitForAttribute,
		ScrollUsingText,
		ScrollToElement,
		GetDataFromClipboard,
		MaximizeMainWindow,
		TableSearch,
		WaitForElement,
		GetScreenshot,
		GetElementColor,
		DragAndDropElement,
		RestartDriver,
		TakeScreenshot,
		ColorsCollector;
	}

	private static WindowsActionName getByLabel(String label) throws ScriptCompileException
	{
		for (WindowsActionName name : WindowsActionName.values())
			if (name.toString().equalsIgnoreCase(label))
				return name;

		throw new ScriptCompileException("Windows action '" + label + "' not found in actions list");
	}

	public static WindowsAction getByName(String actionName) throws ScriptCompileException {
		switch (getByLabel(actionName)) {
			case Open :					return new Open();
			case Click :				return new Click();
			case SendText :				return new SendText();
			case GetActiveWindow :		return new SwitchActiveWindow();
			case MaximizeMainWindow :	return new MaximizeMainWindow();
			case GetElementAttribute:	return new GetElementAttribute();
			case Wait:					return new Wait();
			case CheckElement:			return new CheckElement();
			case ToggleCheckBox:		return new ToggleCheckBox();
			case GetWindow:				return new GetWindow();
			case SearchElement:			return new SearchElement();
			case WaitForAttribute:		return new WaitForAttribute();
			case ScrollUsingText:		return new ScrollByText();
			case ScrollToElement:		return new ScrollToElement();
			case GetDataFromClipboard:	return new GetDataFromClipboard();
			case TableSearch: 			return new TableSearch();
			case WaitForElement:		return new WaitForElement();
			case TakeScreenshot:		return new TakeScreenshot();
			case GetScreenshot:			return new GetScreenshot();
			case RestartDriver:			return new RestartDriver();
			case GetElementColor:		return new GetElementColor();
			case DragAndDropElement:	return new DragAndDropElement();
			case ColorsCollector:		return new ColorsCollector();
			default :
				throw new ScriptCompileException("Unknown action name '" + actionName + "'");
		}
	}

}
