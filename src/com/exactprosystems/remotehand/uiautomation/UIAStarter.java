////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2017, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.uiautomation;

import com.exactprosystems.remotehand.Starter;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

/**
 * Created by alexey.karpukhin on 2/11/16.
 */
public class UIAStarter {

	public static void main(String[] args) {
//		ConsoleHandler handler = new ConsoleHandler();
//		handler.setLevel(Level.FINEST
		Starter.main(args, new UIARemoteHandManager());
	}
}
