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

package com.exactprosystems.remotehand;

public enum RemoteManagerType
{
	WEB("web"),
	WINDOWS("windows");

	private final String label;


	RemoteManagerType(String label)
	{
		this.label = label;
	}


	public String getLabel()
	{
		return label;
	}

	public static RemoteManagerType getByLabel(String label)
	{
		for (RemoteManagerType value : values())
		{
			if (value.getLabel().equals(label))
				return value;
		}

		return null;
	}
	
	public static String[] labels()
	{
		String[] result = new String[values().length];
		int i = 0;
		for (RemoteManagerType t : values())
			result[i++] = t.getLabel();
		return result;
	}
}