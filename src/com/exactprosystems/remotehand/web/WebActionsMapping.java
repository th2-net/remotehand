////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2017, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web;

import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.web.actions.*;
import com.exactprosystems.remotehand.web.actions.mtable.*;

public class WebActionsMapping
{
	private static WebActionsMapping mapping = new WebActionsMapping();

	public static WebActionsMapping getInstance()
	{
		return mapping;
	}

	private enum WebActionName
	{
		Click, Open, SendKeys, WaitForElement, WaitForNew, Wait, GetElement, GetDynamicTable, ScrollTo, PageSource, Refresh, ClearElement, Output, SelectFrame, FindElement, SetCheckbox,
		GetFromCanvasTable, MTableGetValue, MTableGetRow, MTableGetTable, MTableGetRowCount, SendKeysToActive, PressKey, GetFormFields, GetScreenshot, SwitchWindow, UploadFile, CloseWindow
		, Select;

		private static WebActionName getByLabel(String label) throws ScriptCompileException
		{
			for (WebActionName name : WebActionName.values())
				if (name.toString().equalsIgnoreCase(label))
					return name;

			throw new ScriptCompileException("Web action '" + label + "' not found in actions list");
		}
	}

	public WebAction getByName(String actionName) throws ScriptCompileException
	{
		switch (WebActionName.getByLabel(actionName))
		{
		case Open :	               return new Open();
		case Click :               return new Click();
		case SendKeys :	           return new SendKeys();
		case WaitForElement :      return new WaitForElement();
		case WaitForNew :          return new WaitForNew();
		case Wait :	               return new Wait();
		case GetElement :          return new GetElement();
		case GetDynamicTable :     return new GetDynamicTable();
		case ScrollTo :            return new ScrollTo();
		case PageSource :          return new PageSource();
		case Refresh :             return new Refresh();
		case ClearElement :        return new ClearElement();
		case Output :              return new Output();
		case SelectFrame :         return new SelectFrame();
		case FindElement :         return new FindElement();
		case SetCheckbox :         return new SetCheckbox();
		case MTableGetValue :      return new MTableGetValue();
		case MTableGetRow :        return new MTableGetRow();
		case MTableGetTable :      return new MTableGetTable();
		case MTableGetRowCount :   return new MTableGetRowCount();
		case SendKeysToActive:     return new SendKeysToActive();
		case PressKey :            return new PressKey();
		case GetFormFields:        return new GetFormFields();
		case GetScreenshot:        return new GetScreenshot();
		case SwitchWindow:         return new SwitchWindow();
		case UploadFile:           return new UploadFile();
		case CloseWindow:          return new CloseWindow();
		case Select: 			   return new Select();
		default : throw new ScriptCompileException("Unknown action name '" + actionName + "'");
		}
	}
}
