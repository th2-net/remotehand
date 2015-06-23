////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand;

import com.exactprosystems.remotehand.actions.ClearElement;
import com.exactprosystems.remotehand.actions.Click;
import com.exactprosystems.remotehand.actions.GetDynamicTable;
import com.exactprosystems.remotehand.actions.GetElement;
import com.exactprosystems.remotehand.actions.ScrollTo;
import com.exactprosystems.remotehand.actions.Open;
import com.exactprosystems.remotehand.actions.Output;
import com.exactprosystems.remotehand.actions.PageSource;
import com.exactprosystems.remotehand.actions.Refresh;
import com.exactprosystems.remotehand.actions.SendKeys;
import com.exactprosystems.remotehand.actions.Wait;
import com.exactprosystems.remotehand.actions.WaitForElement;
import com.exactprosystems.remotehand.actions.WaitForNew;

public class WebActionsMapping
{
	private static WebActionsMapping mapping = new WebActionsMapping();

	public static WebActionsMapping getInstance()
	{
		return mapping;
	}

	private enum WebActionName
	{
		Click, Open, SendKeys, WaitForElement, WaitForNew, Wait, GetElement, GetDynamicTable, ScrollTo, PageSource, Refresh, ClearElement, Output;

		private static WebActionName getByLabel(String label) throws ScriptCompileException
		{
			for (WebActionName name : WebActionName.values())
				if (name.toString().equalsIgnoreCase(label))
					return name;

			throw new ScriptCompileException("Web action '" + label + "' not found in actions list");
		}
	};

	public WebAction getByName(String actionName) throws ScriptCompileException
	{
		switch (WebActionName.getByLabel(actionName))
		{
		case Open :	               return new Open();
		case Click :               return new Click();
		case SendKeys :	           return new SendKeys();
		case WaitForElement :	     return new WaitForElement();
		case WaitForNew :	         return new WaitForNew();
		case Wait :	               return new Wait();
		case GetElement :	         return new GetElement();
		case GetDynamicTable :     return new GetDynamicTable();
		case ScrollTo :            return new ScrollTo();
		case PageSource :          return new PageSource();
		case Refresh :             return new Refresh();
		case ClearElement :        return new ClearElement();
		case Output :              return new Output();
		default : throw new ScriptCompileException("Unknown action name '" + actionName + "'");
		}
	}
}
