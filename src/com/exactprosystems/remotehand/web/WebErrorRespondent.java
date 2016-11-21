////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.remotehand.web;

import com.exactprosystems.clearth.connectivity.data.rhdata.RhResponseCode;
import com.exactprosystems.clearth.connectivity.data.rhdata.RhScriptResult;
import com.exactprosystems.remotehand.http.ErrorRespondent;
import org.openqa.selenium.WebDriverException;

/**
 * Created by alexey.karpukhin on 10/21/16.
 */
public class WebErrorRespondent extends ErrorRespondent{

	public static synchronized ErrorRespondent getRespondent()
	{
		if (respondent == null)
			respondent = new WebErrorRespondent();
		return respondent;
	}

	@Override
	public RhScriptResult error(Exception ex)
	{
		if (ex instanceof WebDriverException)
		{
			RhScriptResult result = new RhScriptResult();
			result.setCode(RhResponseCode.WEBDRIVER_ERROR.getCode());
			result.setErrorMessage(ex.getMessage());
			return result;
		}
		else 
			return super.error(ex);
	}
}
