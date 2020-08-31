/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.windows;

import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.windows.actions.CheckElement;
import com.exactprosystems.remotehand.windows.actions.ClickContextMenu;
import com.exactprosystems.remotehand.windows.actions.GetElementAttribute;
import com.exactprosystems.remotehand.windows.actions.GetWindow;
import com.exactprosystems.remotehand.windows.actions.Open;
import com.exactprosystems.remotehand.windows.actions.Click;
import com.exactprosystems.remotehand.windows.actions.SwitchActiveWindow;
import com.exactprosystems.remotehand.windows.actions.SendText;
import com.exactprosystems.remotehand.windows.actions.ToggleCheckBox;
import com.exactprosystems.remotehand.windows.actions.Wait;

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
		GetWindow;
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
			case GetElementAttribute:	return new GetElementAttribute();
			case Wait:					return new Wait();
			case CheckElement:			return new CheckElement();
			case ToggleCheckBox:		return new ToggleCheckBox();
			case ClickContextMenu:		return new ClickContextMenu();
			case GetWindow:				return new GetWindow();
			default : 
				throw new ScriptCompileException("Unknown action name '" + actionName + "'");
		}
	}
	
}
