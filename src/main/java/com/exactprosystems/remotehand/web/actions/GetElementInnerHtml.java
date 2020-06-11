/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web.actions;

import org.openqa.selenium.WebElement;

public class GetElementInnerHtml extends GetElement
{
	public static final String INNER_HTML = "innerHTML";
	
	@Override
	protected String getElementHtml(WebElement element)
	{
		return element.getAttribute(INNER_HTML);
	}
}
