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

	public void createConfigurations(CommandLine commandLine, Map<String, String> options)
	{
		managers.forEach((managerType, remoteHandManager) -> remoteHandManager.createConfiguration(commandLine, options));
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
