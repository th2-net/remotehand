/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.grid;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.cli.CommandLine;

import com.exactprosystems.remotehand.DriverPoolProvider;
import com.exactprosystems.remotehand.IDriverManager;
import com.exactprosystems.remotehand.IRemoteHandManager;
import com.exactprosystems.remotehand.RemoteManagerType;
import com.exactprosystems.remotehand.grid.providers.BaseGridDriverPoolProvider;
import com.exactprosystems.remotehand.grid.providers.WebGridDriverPoolProvider;
import com.exactprosystems.remotehand.grid.providers.WindowsGridDriverPoolProvider;
import com.exactprosystems.remotehand.web.WebRemoteHandManager;
import com.exactprosystems.remotehand.windows.WindowsRemoteHandManager;

public class GridRemoteHandManager implements IDriverManager
{
	private final EnumMap<RemoteManagerType, IRemoteHandManager> managers;
	private final Map<String, String> sessionTargetUrls = new ConcurrentHashMap<>();
	private final List<BaseGridDriverPoolProvider<?>> driverPoolProviders = new ArrayList<>();


	public GridRemoteHandManager()
	{
		managers = new EnumMap<>(RemoteManagerType.class);
		managers.put(RemoteManagerType.WEB, createWebManager());
		managers.put(RemoteManagerType.WINDOWS, createWinManager());
	}


	public IRemoteHandManager getRemoteHandManager(RemoteManagerType managerType)
	{
		return managers.get(managerType);
	}

	public HttpHandler createLogonHandler()
	{
		return new GridLogonHandler(this);
	}

	public void saveSession(String sessionId, String sessionUrl)
	{
		sessionTargetUrls.put(sessionId, sessionUrl);
	}

	public void createConfigurations(CommandLine commandLine)
	{
		managers.forEach((managerType, remoteHandManager) -> remoteHandManager.createConfiguration(commandLine));
	}

	@Override
	public void initDriverPool()
	{
	
	}

	@Override
	public void clearDriverPool()
	{
		driverPoolProviders.forEach(DriverPoolProvider::clearDriverPool);
		driverPoolProviders.clear();
	}


	private IRemoteHandManager createWebManager()
	{
		WebGridDriverPoolProvider webGridDriverPoolProvider = new WebGridDriverPoolProvider(sessionTargetUrls);
		driverPoolProviders.add(webGridDriverPoolProvider);
		return new WebRemoteHandManager(webGridDriverPoolProvider);
	}

	private IRemoteHandManager createWinManager()
	{
		WindowsGridDriverPoolProvider winGridDriverPoolProvider = new WindowsGridDriverPoolProvider(sessionTargetUrls);
		driverPoolProviders.add(winGridDriverPoolProvider);
		return new WindowsRemoteHandManager(winGridDriverPoolProvider);
	}
}
