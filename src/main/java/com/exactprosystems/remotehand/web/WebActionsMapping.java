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
		Click,
		Open,
		SendKeys,
		WaitForElement,
		WaitForNew,
		Wait,
		GetElement,
		GetElementValue,
		GetElementAttribute,
		GetDynamicTable,
		ScrollTo,
		ScrollDivTo,
		ScrollDivUntil,
		PageSource,
		Refresh,
		ClearElement,
		Output,
		SelectFrame,
		FindElement,
		SetCheckbox,
		GetFromCanvasTable,
		MTableGetValue,
		MTableGetRow,
		MTableGetTable,
		MTableGetRowCount,
		SendKeysToActive,
		KeyAction,
		PressKey,
		GetFormFields,
		GetScreenshot,
		SwitchWindow,
		UploadFile,
		CloseWindow,
		Select,
		DownloadFile,
		GetCurrentURL,
		SetZoom,
		DurationStart,
		GetDuration,
		GetElementScreenshot,
		WaitForChanges,
		GetElementInnerHtml,
		AcceptAlert,
		DismissAlert,
		CheckImageAvailability;
		

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
		case GetElementValue :     return new GetElementValue();
		case GetElementAttribute : return new GetElementAttribute();
		case GetDynamicTable :     return new GetDynamicTable();
		case ScrollTo :            return new ScrollTo();
		case ScrollDivTo :         return new ScrollDivTo();
		case ScrollDivUntil:       return new ScrollDivUntil();
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
		case KeyAction:
		case PressKey:             return new KeyAction();
		case GetFormFields:        return new GetFormFields();
		case GetScreenshot:        return new GetScreenshot();
		case SwitchWindow:         return new SwitchWindow();
		case UploadFile:           return new UploadFile();
		case CloseWindow:          return new CloseWindow();
		case Select:               return new Select();
		case DownloadFile:         return new DownloadFile();
		case GetCurrentURL:        return new GetCurrentURL();
		case SetZoom:              return new SetZoom();
		case DurationStart:        return new DurationStart();
		case GetDuration:          return new GetDuration();
		case GetElementScreenshot: return new GetElementScreenshot();
		case WaitForChanges:       return new WaitForChanges();
		case GetElementInnerHtml:  return new GetElementInnerHtml();
		case AcceptAlert:          return new AcceptAlert();
		case DismissAlert:         return new DismissAlert();
		case CheckImageAvailability: return new CheckImageAvailability();
		default : throw new ScriptCompileException("Unknown action name '" + actionName + "'");
		}
	}
}
