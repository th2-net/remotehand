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

package com.exactprosystems.remotehand.windows;

import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.windows.actions.*;

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
		ClickContextMenu,
		GetWindow,
		SearchElement,
		WaitForAttribute,
		ScrollUsingText,
		GetDataFromClipboard,
		TableClick,
		MaximizeMainWindow,
		TableSearch,
		WaitForElement,
		GetScreenshot
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
			case ClickContextMenu:		return new ClickContextMenu();
			case GetWindow:				return new GetWindow();
			case SearchElement:			return new SearchElement();
			case WaitForAttribute:		return new WaitForAttribute();
			case ScrollUsingText:		return new ScrollByText();
			case GetDataFromClipboard:	return new GetDataFromClipboard();
			case TableClick:			return new TableClick();
			case TableSearch: 			return new TableSearch();
			case WaitForElement:		return new WaitForElement();
			case GetScreenshot:			return new GetScreenshot();
			default :
				throw new ScriptCompileException("Unknown action name '" + actionName + "'");
		}
	}

}
