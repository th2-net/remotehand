/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web;

import com.exactprosystems.remotehand.Starter;

/**
 * Created by alexey.karpukhin on 2/3/16.
 */
public class WebRhStarter {

	public static void main(String[] args) {
		Starter.main(args, new WebRemoteHandManager());
	}


}
