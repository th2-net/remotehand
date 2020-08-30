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

import com.exactprosystems.remotehand.windows.locator.ByAccessibilityId;
import io.appium.java_client.windows.WindowsDriver;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElementSearcher {
	
	
	private List<Pair<String, String>> processFrom(Map<String, String> str000) {
		
		int ind = 1;
		String locator, matcher;
		List<Pair<String, String>> l = new ArrayList<>();
		do {
			locator = str000.get("locator" + (ind == 1 ? "" : ind));
			matcher = str000.get("matcher" + (ind == 1 ? "" : ind));
			ind++;
			if (locator != null && matcher != null) {
				l.add(new ImmutablePair<>(locator, matcher));
			}
			
		} while (locator != null && matcher != null);
		
		return l;

	}
	
	private By parseBy (String using, String id) {

		switch (using.toLowerCase()) {
			case "accessibilityid" : return new ByAccessibilityId(id);
			case "name" : return new By.ByName(id);
			case "tagname" : return new By.ByTagName(id);
		}
		throw new IllegalArgumentException("unknown using methods");
	}
	
	
	public WebElement searchElement(Map<String, String> map, WindowsDriver<?> driver) {
		List<Pair<String, String>> pairs = this.processFrom(map);

		WebElement we = null;
		for (Pair<String, String> pair : pairs) {
		
			By by = parseBy(pair.getKey(), pair.getValue());
			
			if (we == null) {
				we = driver.findElement(by);
			} else {
				we = we.findElement(by);
			}
			
		}
		
		return we;
	}
	
	
}
